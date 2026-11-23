package com.example.daycarepickupproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.Face.CameraHelper;
import com.example.daycarepickupproject.Face.FaceAligner;
import com.example.daycarepickupproject.Face.FaceAnalyzer;
import com.example.daycarepickupproject.Face.FaceConfig;
import com.example.daycarepickupproject.Face.FaceEmbedder;
import com.example.daycarepickupproject.Face.FaceEngine;
import com.example.daycarepickupproject.Face.FaceMath;
import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.FaceEmbedding;
import com.example.daycarepickupproject.Models.Guardian;
import com.example.daycarepickupproject.Views.ChildDialog;
import com.example.daycarepickupproject.Views.FaceGuideView;
import com.example.daycarepickupproject.Views.GuardianPhotos;
import com.example.daycarepickupproject.Views.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Enrolment: the guardian's details and children, then 3-5 face photos
 * taken automatically by the front camera. Only face embeddings (numbers) and a
 * small thumbnail are saved, all on this device.
 */
public class EnrollmentScreen extends AppCompatActivity {

    /** Optional: child id to pre-select (when opened from a child's profile). */
    public static final String EXTRA_CHILD_ID = "childId";

    // Step 1 views
    private View formPage;
    private TextInputLayout tilName;
    private TextInputEditText etName;
    private TextInputEditText etRelation;
    private android.widget.LinearLayout childChecks;
    private TextView tvNoChildren;

    // Step 2 views
    private View cameraPage;
    private FaceGuideView faceGuide;
    private TextView tvProgress;
    private TextView tvHint;
    private LinearProgressIndicator progressBar;
    private MaterialButton btnSave;

    private final Set<Long> selectedChildIds = new HashSet<>();

    // Captured samples (written on the camera thread, read on the main thread)
    private final List<float[]> samples = new ArrayList<>();
    private Bitmap thumbnail;
    private long lastCaptureMs;
    private volatile boolean capturing;
    private boolean saving;

    private FaceAnalyzer analyzer;
    private ExecutorService cameraExecutor;
    private FaceEmbedder embedder;

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_needed, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Ui.ensureSignedIn(this)) {
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enrollment_screen);
        Ui.applySystemBarInsets(findViewById(R.id.main));

        formPage = findViewById(R.id.formPage);
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        etRelation = findViewById(R.id.etRelation);
        childChecks = findViewById(R.id.childChecks);
        tvNoChildren = findViewById(R.id.tvNoChildren);
        cameraPage = findViewById(R.id.cameraPage);
        faceGuide = findViewById(R.id.faceGuide);
        tvProgress = findViewById(R.id.tvProgress);
        tvHint = findViewById(R.id.tvHint);
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);

        ((TextView) findViewById(R.id.tvSubtitle)).setText(getString(R.string.enrol_subtitle,
                FaceConfig.ENROL_MIN_PHOTOS, FaceConfig.ENROL_MAX_PHOTOS));
        progressBar.setMax(FaceConfig.ENROL_MAX_PHOTOS);

        long preselect = getIntent().getLongExtra(EXTRA_CHILD_ID, -1);
        if (preselect >= 0) {
            selectedChildIds.add(preselect);
        }
        loadChildren();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddChild).setOnClickListener(v ->
                ChildDialog.show(this, null, child -> {
                    selectedChildIds.add(child.id);
                    loadChildren();
                }));
        findViewById(R.id.btnNext).setOnClickListener(v -> onNext());
        findViewById(R.id.btnCameraBack).setOnClickListener(v -> showForm());
        findViewById(R.id.btnRetake).setOnClickListener(v -> resetSamples());
        btnSave.setOnClickListener(v -> save());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (cameraPage.getVisibility() == View.VISIBLE) {
                    showForm();
                } else {
                    finish();
                }
            }
        });

        // Load the face model in the background so it's ready for the camera step
        Repository.run(this, dao -> {
            try {
                return FaceEngine.embedder(this);
            } catch (Exception e) {
                return null;
            }
        }, loaded -> {
            embedder = loaded;
            if (loaded == null) {
                Toast.makeText(this, R.string.model_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------- Step 1: details ----------

    private void loadChildren() {
        Repository.run(this, dao -> dao.getChildren(), this::showChildren);
    }

    private void showChildren(List<Child> children) {
        childChecks.removeAllViews();
        tvNoChildren.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
        childChecks.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
        for (Child child : children) {
            MaterialCheckBox box = new MaterialCheckBox(this, null);
            box.setTextAppearance(R.style.PickupName);
            box.setMinHeight((int) (56 * getResources().getDisplayMetrics().density));
            box.setButtonTintList(ContextCompat.getColorStateList(this, R.color.brand_blue));
            box.setText(child.room.isEmpty() ? child.name : getString(R.string.detail_dot, child.name, child.room));
            box.setChecked(selectedChildIds.contains(child.id));
            box.setOnCheckedChangeListener((b, checked) -> {
                if (checked) {
                    selectedChildIds.add(child.id);
                } else {
                    selectedChildIds.remove(child.id);
                }
            });
            childChecks.addView(box);
        }
    }

    private void onNext() {
        tilName.setError(null);
        if (text(etName).isEmpty()) {
            tilName.setError(getString(R.string.enrol_name_required));
            return;
        }
        if (selectedChildIds.isEmpty()) {
            Toast.makeText(this, R.string.enrol_child_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (embedder == null) {
            Toast.makeText(this, R.string.model_error, Toast.LENGTH_LONG).show();
            return;
        }
        formPage.setVisibility(View.GONE);
        cameraPage.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvGuardianName)).setText(text(etName));
        resetSamples();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void showForm() {
        capturing = false;
        if (analyzer != null) {
            analyzer.setPaused(true);
        }
        cameraPage.setVisibility(View.GONE);
        formPage.setVisibility(View.VISIBLE);
    }

    // ---------- Step 2: face photos ----------

    private void openCamera() {
        capturing = true;
        if (analyzer != null) {
            analyzer.setPaused(false);
            return;
        }
        analyzer = new FaceAnalyzer(this::onFrame);
        cameraExecutor = CameraHelper.start(this, this, findViewById(R.id.previewView), analyzer,
                () -> Toast.makeText(this, R.string.camera_error, Toast.LENGTH_LONG).show());
    }

    private void resetSamples() {
        synchronized (samples) {
            samples.clear();
            thumbnail = null;
            lastCaptureMs = 0;
        }
        capturing = true;
        updateProgress(R.string.enrol_hint_start, R.color.guide_idle);
    }

    /** Runs on the camera thread for every frame. */
    private void onFrame(@NonNull FaceAnalyzer.Frame frame) {
        if (!capturing) {
            return;
        }
        if (frame.status != FaceAnalyzer.FaceStatus.OK || frame.face == null) {
            runOnUiThread(() -> updateProgress(guidance(frame.status), R.color.guide_warn));
            return;
        }
        Float left = frame.face.getLeftEyeOpenProbability();
        Float right = frame.face.getRightEyeOpenProbability();
        if (left == null || right == null || left < FaceConfig.EYE_OPEN || right < FaceConfig.EYE_OPEN) {
            runOnUiThread(() -> updateProgress(R.string.face_open_eyes, R.color.guide_warn));
            return;
        }
        if (frame.timestampMs - lastCaptureMs < FaceConfig.ENROL_CAPTURE_INTERVAL_MS) {
            return;
        }

        Bitmap aligned = FaceAligner.align(frame.bitmap, frame.face, embedder.getInputSize());
        float[] embedding = embedder.embed(aligned);

        int hint;
        synchronized (samples) {
            // Every photo must be of the same person as the first one
            if (!samples.isEmpty()
                    && FaceMath.cosine(samples.get(0), embedding) < FaceConfig.MATCH_THRESHOLD - 0.15f) {
                runOnUiThread(() -> updateProgress(R.string.enrol_hint_same_person, R.color.guide_warn));
                return;
            }
            samples.add(embedding);
            if (thumbnail == null) {
                thumbnail = aligned;
            }
            lastCaptureMs = frame.timestampMs;
            if (samples.size() >= FaceConfig.ENROL_MAX_PHOTOS) {
                capturing = false;
                hint = R.string.enrol_hint_done;
            } else {
                hint = R.string.enrol_hint_move;
            }
        }
        runOnUiThread(() -> updateProgress(hint, R.color.guide_ok));
    }

    private void updateProgress(int hintRes, int guideColorRes) {
        int count;
        synchronized (samples) {
            count = samples.size();
        }
        tvProgress.setText(getString(R.string.enrol_progress,
                Math.min(count + (count < FaceConfig.ENROL_MAX_PHOTOS ? 1 : 0), FaceConfig.ENROL_MAX_PHOTOS),
                FaceConfig.ENROL_MAX_PHOTOS));
        progressBar.setProgressCompat(count, true);
        tvHint.setText(hintRes);
        faceGuide.setGuideColor(ContextCompat.getColor(this, guideColorRes));
        btnSave.setEnabled(count >= FaceConfig.ENROL_MIN_PHOTOS && !saving);
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

    private void save() {
        List<FaceEmbedding> embeddings = new ArrayList<>();
        Bitmap thumb;
        synchronized (samples) {
            if (samples.size() < FaceConfig.ENROL_MIN_PHOTOS) {
                Toast.makeText(this, getString(R.string.enrol_need_more, FaceConfig.ENROL_MIN_PHOTOS),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            long now = System.currentTimeMillis();
            for (float[] s : samples) {
                FaceEmbedding e = new FaceEmbedding();
                e.vector = FaceMath.toBytes(s);
                e.createdAt = now;
                embeddings.add(e);
            }
            thumb = thumbnail;
        }
        capturing = false;
        saving = true;
        btnSave.setEnabled(false);

        Guardian guardian = new Guardian();
        guardian.name = text(etName);
        guardian.relation = text(etRelation);
        guardian.photoCount = embeddings.size();
        guardian.createdAt = System.currentTimeMillis();
        List<Long> childIds = new ArrayList<>(selectedChildIds);
        Context app = getApplicationContext();

        Repository.run(this, dao -> {
            long id = dao.enrolGuardian(guardian, embeddings, childIds);
            if (thumb != null) {
                GuardianPhotos.save(app, id, thumb);
            }
            return id;
        }, id -> {
            Toast.makeText(this, getString(R.string.enrol_saved, guardian.name), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent().putExtra("guardianId", id));
            finish();
        });
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
