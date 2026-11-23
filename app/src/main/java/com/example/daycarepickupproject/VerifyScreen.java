package com.example.daycarepickupproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.Face.BlinkDetector;
import com.example.daycarepickupproject.Face.CameraHelper;
import com.example.daycarepickupproject.Face.FaceAligner;
import com.example.daycarepickupproject.Face.FaceAnalyzer;
import com.example.daycarepickupproject.Face.FaceConfig;
import com.example.daycarepickupproject.Face.FaceEmbedder;
import com.example.daycarepickupproject.Face.FaceEngine;
import com.example.daycarepickupproject.Face.FaceMatcher;
import com.example.daycarepickupproject.Face.FaceMath;
import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.FaceEmbedding;
import com.example.daycarepickupproject.Models.Guardian;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.Models.PinHasher;
import com.example.daycarepickupproject.Models.Session;
import com.example.daycarepickupproject.Models.Staff;
import com.example.daycarepickupproject.Views.FaceGuideView;
import com.example.daycarepickupproject.Views.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Pick-up verification: the guardian looks at the camera and blinks (liveness check),
 * then one frame is compared on-device against every enrolled guardian.
 * Every attempt is written to the pick-up log; staff can override with their PIN.
 */
public class VerifyScreen extends AppCompatActivity {

    private enum Phase { LOADING, SCANNING, CHECKING, RESULT }

    /** No face for this long restarts the blink check. */
    private static final long FACE_LOST_RESET_MS = 1500;

    private FaceGuideView faceGuide;
    private View statusCard;
    private TextView tvStatus;
    private TextView tvStatusSub;
    private View resultSheet;
    private TextView tvResultAvatar;
    private TextView tvResultBadge;
    private TextView tvResultTitle;
    private TextView tvResultDetail;
    private TextView tvChildrenLabel;
    private LinearLayout childChecks;
    private MaterialButton btnPrimary;
    private MaterialButton btnSecondary;
    private MaterialButton btnTertiary;

    private volatile Phase phase = Phase.LOADING;
    private FaceEmbedder embedder;
    private FaceMatcher matcher;
    private final BlinkDetector blink = new BlinkDetector(FaceConfig.EYE_OPEN, FaceConfig.EYE_CLOSED);

    // Camera-thread state for the current attempt
    private long scanStartMs;
    private long lastFaceMs;
    private boolean blinked;
    private int lastStatusRes;

    private FaceAnalyzer analyzer;
    private ExecutorService cameraExecutor;

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_needed, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Ui.ensureSignedIn(this)) {
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_screen);
        Ui.applySystemBarInsets(findViewById(R.id.main));

        faceGuide = findViewById(R.id.faceGuide);
        statusCard = findViewById(R.id.statusCard);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusSub = findViewById(R.id.tvStatusSub);
        resultSheet = findViewById(R.id.resultSheet);
        tvResultAvatar = findViewById(R.id.tvResultAvatar);
        tvResultBadge = findViewById(R.id.tvResultBadge);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        tvChildrenLabel = findViewById(R.id.tvChildrenLabel);
        childChecks = findViewById(R.id.childChecks);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnSecondary = findViewById(R.id.btnSecondary);
        btnTertiary = findViewById(R.id.btnTertiary);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Load the model and all enrolled faces, then start scanning
        Repository.run(this, dao -> {
            FaceMatcher m = new FaceMatcher(FaceConfig.MATCH_THRESHOLD, FaceConfig.MATCH_MARGIN);
            for (FaceEmbedding e : dao.getAllEmbeddings()) {
                m.add(e.guardianId, FaceMath.fromBytes(e.vector));
            }
            try {
                embedder = FaceEngine.embedder(this);
            } catch (Exception e) {
                embedder = null;
            }
            return m;
        }, m -> {
            matcher = m;
            if (embedder == null) {
                showMessage(getString(R.string.model_error));
            } else if (m.guardianCount() == 0) {
                showMessage(getString(R.string.verify_no_guardians));
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void startCamera() {
        analyzer = new FaceAnalyzer(this::onFrame);
        cameraExecutor = CameraHelper.start(this, this, findViewById(R.id.previewView), analyzer,
                () -> showMessage(getString(R.string.camera_error)));
        restartScan();
    }

    /** Back to scanning for a fresh attempt. */
    private void restartScan() {
        resultSheet.setVisibility(View.GONE);
        statusCard.setVisibility(View.VISIBLE);
        setStatus(R.string.face_none, 0, R.color.guide_idle);
        if (cameraExecutor == null) {
            phase = Phase.SCANNING;
            return;
        }
        // Reset the per-attempt state on the camera thread, where it is used
        cameraExecutor.execute(() -> {
            blink.reset();
            blinked = false;
            scanStartMs = 0;
            lastFaceMs = 0;
            phase = Phase.SCANNING;
        });
        if (analyzer != null) {
            analyzer.setPaused(false);
        }
    }

    // ---------- Camera thread ----------

    private void onFrame(@NonNull FaceAnalyzer.Frame frame) {
        if (phase != Phase.SCANNING) {
            return;
        }
        long now = frame.timestampMs;

        if (frame.face == null) {
            // No face (or several): restart the blink check if the face has been gone a while
            if (lastFaceMs != 0 && now - lastFaceMs > FACE_LOST_RESET_MS) {
                blink.reset();
                blinked = false;
                scanStartMs = 0;
            }
            postStatus(frame.status == FaceAnalyzer.FaceStatus.MULTIPLE_FACES
                    ? R.string.face_multiple : R.string.face_none, 0, R.color.guide_idle);
            return;
        }
        lastFaceMs = now;
        if (scanStartMs == 0) {
            scanStartMs = now;
        }

        Float left = frame.face.getLeftEyeOpenProbability();
        Float right = frame.face.getRightEyeOpenProbability();
        if (blink.onFrame(frame.face.getTrackingId(), left, right)) {
            blinked = true;
        }

        if (!blinked) {
            if (now - scanStartMs > FaceConfig.BLINK_TIMEOUT_MS) {
                phase = Phase.RESULT;
                runOnUiThread(this::onLivenessFailed);
                return;
            }
            if (frame.status == FaceAnalyzer.FaceStatus.OK) {
                postStatus(R.string.verify_blink, R.string.verify_blink_sub, R.color.guide_ok);
            } else {
                postStatus(guidance(frame.status), 0, R.color.guide_warn);
            }
            return;
        }

        // Blink seen: recognise from the first good, eyes-open frame
        if (frame.status != FaceAnalyzer.FaceStatus.OK || !blink.eyesOpen(left, right)) {
            postStatus(frame.status == FaceAnalyzer.FaceStatus.OK
                    ? R.string.face_open_eyes : guidance(frame.status), 0, R.color.guide_warn);
            return;
        }
        phase = Phase.CHECKING;
        postStatus(R.string.verify_checking, 0, R.color.guide_ok);

        long start = SystemClock.elapsedRealtime();
        Bitmap aligned = FaceAligner.align(frame.bitmap, frame.face, embedder.getInputSize());
        float[] embedding = embedder.embed(aligned);
        FaceMatcher.Result result = matcher.match(embedding);
        long elapsed = SystemClock.elapsedRealtime() - start;

        phase = Phase.RESULT;
        runOnUiThread(() -> onRecognised(result, elapsed));
    }

    private void postStatus(int titleRes, int subRes, int colorRes) {
        if (titleRes == lastStatusRes) {
            return;
        }
        lastStatusRes = titleRes;
        runOnUiThread(() -> setStatus(titleRes, subRes, colorRes));
    }

    private static int guidance(FaceAnalyzer.FaceStatus status) {
        switch (status) {
            case MULTIPLE_FACES:
                return R.string.face_multiple;
            case TOO_FAR:
                return R.string.face_too_far;
            case NOT_FACING:
                return R.string.face_not_facing;
            default:
                return R.string.face_none;
        }
    }

    // ---------- Main thread ----------

    private void setStatus(int titleRes, int subRes, int colorRes) {
        tvStatus.setText(titleRes);
        if (subRes != 0) {
            tvStatusSub.setText(subRes);
            tvStatusSub.setVisibility(View.VISIBLE);
        } else {
            tvStatusSub.setVisibility(View.GONE);
        }
        faceGuide.setGuideColor(ContextCompat.getColor(this, colorRes));
    }

    private void onLivenessFailed() {
        lastStatusRes = 0;
        if (analyzer != null) {
            analyzer.setPaused(true);
        }
        logRejected(-1f, getString(R.string.note_liveness_failed));
        showRejected(getString(R.string.verify_liveness_failed),
                getString(R.string.verify_liveness_failed_detail));
    }

    private void onRecognised(FaceMatcher.Result result, long elapsedMs) {
        lastStatusRes = 0;
        if (analyzer != null) {
            analyzer.setPaused(true);
        }
        if (!result.isMatch()) {
            logRejected(result.bestScore, getString(R.string.note_no_match));
            showRejected(getString(R.string.verify_not_recognised),
                    getString(R.string.verify_not_recognised_detail, Ui.percent(this, result.bestScore)));
            return;
        }
        long since = Ui.startOfToday();
        Repository.run(this, dao -> {
            Guardian g = dao.getGuardian(result.guardianId);
            List<Child> children = dao.getChildrenOfGuardian(result.guardianId);
            Set<Long> pickedUp = new HashSet<>(dao.getPickedUpChildIdsSince(since));
            return new Object[]{g, children, pickedUp};
        }, data -> {
            Guardian g = (Guardian) data[0];
            if (g == null) { // removed while scanning
                restartScan();
                return;
            }
            @SuppressWarnings("unchecked") List<Child> children = (List<Child>) data[1];
            @SuppressWarnings("unchecked") Set<Long> pickedUp = (Set<Long>) data[2];
            showVerified(g, children, pickedUp, result.bestScore, elapsedMs);
        });
    }

    private void showVerified(Guardian guardian, List<Child> children, Set<Long> pickedUp,
                              float score, long elapsedMs) {
        statusCard.setVisibility(View.GONE);
        resultSheet.setVisibility(View.VISIBLE);
        tvResultAvatar.setText(guardian.initials());
        Ui.applyBadge(tvResultBadge, PickupLog.VERIFIED);
        tvResultTitle.setText(guardian.name);
        String match = getString(R.string.verify_match_detail, Ui.percent(this, score), (int) elapsedMs);
        tvResultDetail.setText(guardian.relation.isEmpty() ? match
                : getString(R.string.detail_dot, guardian.relation, match));

        childChecks.removeAllViews();
        List<MaterialCheckBox> boxes = new ArrayList<>();
        int available = 0;
        for (Child child : children) {
            boolean done = pickedUp.contains(child.id);
            MaterialCheckBox box = new MaterialCheckBox(this);
            box.setTextAppearance(R.style.PickupName);
            box.setMinHeight((int) (56 * getResources().getDisplayMetrics().density));
            box.setButtonTintList(ContextCompat.getColorStateList(this, R.color.brand_blue));
            box.setText(done ? getString(R.string.verify_already_picked, child.name) : child.name);
            box.setEnabled(!done);
            box.setChecked(!done);
            box.setTag(child);
            childChecks.addView(box);
            boxes.add(box);
            if (!done) {
                available++;
            }
        }
        tvChildrenLabel.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
        if (children.isEmpty() || available == 0) {
            TextView note = new TextView(this);
            note.setTextAppearance(R.style.PickupDetail);
            note.setText(children.isEmpty() ? R.string.verify_no_children : R.string.verify_all_picked);
            childChecks.addView(note);
        }

        btnPrimary.setText(R.string.verify_confirm);
        btnPrimary.setEnabled(available > 0);
        btnPrimary.setOnClickListener(v -> confirmPickup(guardian, boxes, score));
        btnSecondary.setVisibility(View.GONE);
        btnTertiary.setText(R.string.action_cancel);
        btnTertiary.setOnClickListener(v -> restartScan());
    }

    private void showRejected(String title, String detail) {
        statusCard.setVisibility(View.GONE);
        resultSheet.setVisibility(View.VISIBLE);
        tvResultAvatar.setText("?");
        Ui.applyBadge(tvResultBadge, PickupLog.REJECTED);
        tvResultTitle.setText(title);
        tvResultDetail.setText(detail);
        tvChildrenLabel.setVisibility(View.GONE);
        childChecks.removeAllViews();

        btnPrimary.setText(R.string.verify_try_again);
        btnPrimary.setEnabled(true);
        btnPrimary.setOnClickListener(v -> restartScan());
        btnSecondary.setVisibility(View.VISIBLE);
        btnSecondary.setOnClickListener(v -> showOverrideDialog());
        btnTertiary.setText(R.string.action_close);
        btnTertiary.setOnClickListener(v -> finish());
    }

    /** A problem that stops scanning (no guardians, no model, camera error). */
    private void showMessage(String message) {
        showRejected(getString(R.string.verify_title), message);
        tvResultBadge.setVisibility(View.GONE);
        tvResultAvatar.setVisibility(View.GONE);
        btnPrimary.setText(R.string.action_close);
        btnPrimary.setOnClickListener(v -> finish());
        btnTertiary.setVisibility(View.GONE);
    }

    private void confirmPickup(Guardian guardian, List<MaterialCheckBox> boxes, float score) {
        Staff staff = Session.getStaff();
        List<PickupLog> logs = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (MaterialCheckBox box : boxes) {
            if (box.isEnabled() && box.isChecked()) {
                Child child = (Child) box.getTag();
                PickupLog log = newLog(PickupLog.VERIFIED, now, staff);
                log.childId = child.id;
                log.childName = child.name;
                log.guardianId = guardian.id;
                log.guardianName = guardian.name;
                log.score = score;
                logs.add(log);
            }
        }
        if (logs.isEmpty()) {
            Toast.makeText(this, R.string.verify_pick_child, Toast.LENGTH_SHORT).show();
            return;
        }
        btnPrimary.setEnabled(false);
        Repository.run(this, dao -> {
            dao.insertLogs(logs);
            return true;
        }, ok -> {
            Toast.makeText(this, R.string.verify_recorded, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void logRejected(float score, String note) {
        PickupLog log = newLog(PickupLog.REJECTED, System.currentTimeMillis(), Session.getStaff());
        log.score = score;
        log.note = note;
        Repository.run(this, dao -> dao.insertLog(log));
    }

    private static PickupLog newLog(String outcome, long time, @Nullable Staff staff) {
        PickupLog log = new PickupLog();
        log.outcome = outcome;
        log.timestamp = time;
        if (staff != null) {
            log.staffId = staff.id;
            log.staffName = staff.name;
        }
        return log;
    }

    // ---------- Staff override ----------

    private void showOverrideDialog() {
        long since = Ui.startOfToday();
        Repository.run(this, dao -> {
            Set<Long> pickedUp = new HashSet<>(dao.getPickedUpChildIdsSince(since));
            List<Child> remaining = new ArrayList<>();
            for (Child c : dao.getChildren()) {
                if (!pickedUp.contains(c.id)) {
                    remaining.add(c);
                }
            }
            return remaining;
        }, this::showOverrideDialog);
    }

    private void showOverrideDialog(List<Child> children) {
        if (children.isEmpty()) {
            Toast.makeText(this, R.string.override_no_children, Toast.LENGTH_LONG).show();
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_override, null);
        AutoCompleteTextView acChild = view.findViewById(R.id.acChild);
        TextInputEditText etPerson = view.findViewById(R.id.etPerson);
        TextInputEditText etReason = view.findViewById(R.id.etReason);
        TextInputEditText etPin = view.findViewById(R.id.etPin);
        TextInputLayout tilPin = view.findViewById(R.id.tilPin);

        List<String> names = new ArrayList<>();
        for (Child c : children) {
            names.add(c.room.isEmpty() ? c.name : getString(R.string.detail_dot, c.name, c.room));
        }
        acChild.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        final int[] selected = {-1};
        acChild.setOnItemClickListener((parent, v, position, id) -> selected[0] = position);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.override_title)
                .setView(view)
                .setPositiveButton(R.string.override_confirm, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String person = text(etPerson);
            String reason = text(etReason);
            String pin = text(etPin);
            tilPin.setError(null);
            if (selected[0] < 0 || person.isEmpty() || reason.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, R.string.override_fill_all, Toast.LENGTH_SHORT).show();
                return;
            }
            Child child = children.get(selected[0]);
            String hash = PinHasher.hash(pin);
            Repository.run(this, dao -> {
                Staff staff = dao.findStaffByPinHash(hash);
                if (staff == null) {
                    return false;
                }
                PickupLog log = newLog(PickupLog.OVERRIDE, System.currentTimeMillis(), staff);
                log.childId = child.id;
                log.childName = child.name;
                log.guardianName = person;
                log.note = reason;
                dao.insertLog(log);
                return true;
            }, ok -> {
                if (!ok) {
                    tilPin.setError(getString(R.string.override_wrong_pin));
                    return;
                }
                dialog.dismiss();
                Toast.makeText(this, R.string.verify_recorded, Toast.LENGTH_SHORT).show();
                finish();
            });
        }));
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (analyzer != null) {
            analyzer.close();
        }
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
