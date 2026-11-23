package com.example.daycarepickupproject.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.HomeScreen;
import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.Guardian;
import com.example.daycarepickupproject.Models.NameUtils;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.R;
import com.example.daycarepickupproject.Views.ChildDialog;
import com.example.daycarepickupproject.Views.GuardianPhotos;
import com.example.daycarepickupproject.Views.Ui;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Child profile: details, authorised guardians (tap one to remove) and the last 7 days of pick-ups.
 */
public class ChildProfileFragment extends Fragment {

    private static final String ARG_CHILD_ID = "childId";

    private long childId;
    @Nullable private Child child;

    private static final class Profile {
        Child child;
        List<Guardian> guardians;
        List<PickupLog> week;
    }

    public ChildProfileFragment() {
        super(R.layout.fragment_child_profile);
    }

    public static ChildProfileFragment newInstance(long childId) {
        ChildProfileFragment f = new ChildProfileFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHILD_ID, childId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        childId = requireArguments().getLong(ARG_CHILD_ID);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            if (child == null) {
                return;
            }
            ChildDialog.show(requireContext(), child, new ChildDialog.Listener() {
                @Override
                public void onSaved(Child saved) {
                    load();
                }

                @Override
                public void onDeleted(Child deleted) {
                    getParentFragmentManager().popBackStack();
                }
            });
        });
        view.findViewById(R.id.btnAddGuardian).setOnClickListener(v ->
                ((HomeScreen) requireActivity()).startEnrolment(childId));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        long weekStart = Ui.startOfToday() - 6 * Ui.DAY_MS;
        Repository.run(requireContext(), dao -> {
            Profile p = new Profile();
            p.child = dao.getChild(childId);
            if (p.child != null) {
                p.guardians = dao.getGuardiansOfChild(childId);
                p.week = dao.getPickupsOfChildSince(childId, weekStart);
            }
            return p;
        }, this::show);
    }

    private void show(Profile p) {
        View view = getView();
        if (view == null) {
            return;
        }
        if (p.child == null) { // deleted elsewhere
            getParentFragmentManager().popBackStack();
            return;
        }
        child = p.child;
        ((TextView) view.findViewById(R.id.tvChildInitials)).setText(child.initials());
        ((TextView) view.findViewById(R.id.tvChildName)).setText(child.name);
        TextView roomAge = view.findViewById(R.id.tvChildRoomAge);
        if (!child.room.isEmpty() && child.age > 0) {
            roomAge.setText(getString(R.string.child_room_age, child.room, child.age));
        } else if (!child.room.isEmpty()) {
            roomAge.setText(child.room);
        } else if (child.age > 0) {
            roomAge.setText(getString(R.string.child_age_only, child.age));
        } else {
            roomAge.setText(R.string.child_no_room);
        }

        TextView status = view.findViewById(R.id.tvChildStatus);
        PickupLog latest = p.week.isEmpty() ? null : p.week.get(0);
        if (latest != null && latest.timestamp >= Ui.startOfToday()) {
            Ui.applyBadge(status, latest.outcome);
            status.setText(getString(R.string.child_picked_up_at, Ui.time(requireContext(), latest.timestamp)));
        } else {
            Ui.applyNeutralBadge(status, R.string.status_still_here);
        }

        showGuardians(view, p.guardians);
        showWeek(view, p.week);
    }

    private void showGuardians(View view, List<Guardian> guardians) {
        LinearLayout list = view.findViewById(R.id.guardiansList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Guardian g : guardians) {
            View card = inflater.inflate(R.layout.item_guardian_card, list, false);
            ((TextView) card.findViewById(R.id.tvName)).setText(g.name);
            String relation = g.relation.isEmpty() ? getString(R.string.guardian_enrolled) : g.relation;
            ((TextView) card.findViewById(R.id.tvDetail))
                    .setText(getString(R.string.guardian_photos, relation, g.photoCount));
            Bitmap photo = GuardianPhotos.load(requireContext(), g.id);
            if (photo != null) {
                ImageView iv = card.findViewById(R.id.ivPhoto);
                iv.setImageBitmap(photo);
                iv.setImageTintList(null);
                iv.setPadding(0, 0, 0, 0);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setClipToOutline(true);
            }
            card.setOnClickListener(v -> confirmRemove(g));
            list.addView(card);
        }
        view.findViewById(R.id.tvGuardiansEmpty).setVisibility(guardians.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmRemove(Guardian g) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.guardian_remove_title, g.name))
                .setMessage(R.string.guardian_remove_message)
                .setPositiveButton(R.string.action_remove, (d, w) -> {
                    android.content.Context app = requireContext().getApplicationContext();
                    Repository.run(app, dao -> {
                        dao.deleteGuardian(g); // face embeddings and links are deleted with it
                        GuardianPhotos.delete(app, g.id);
                        return true;
                    }, ok -> load());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showWeek(View view, List<PickupLog> week) {
        LinearLayout list = view.findViewById(R.id.weekList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        long today = Ui.startOfToday();
        for (int i = 0; i < week.size(); i++) {
            PickupLog log = week.get(i);
            if (i > 0) {
                inflater.inflate(R.layout.item_divider, list, true);
            }
            View row = inflater.inflate(R.layout.item_week_row, list, false);
            String day = log.timestamp >= today ? getString(R.string.child_today)
                    : DateUtils.formatDateTime(requireContext(), log.timestamp,
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY);
            ((TextView) row.findViewById(R.id.tvDay)).setText(day);
            ((TextView) row.findViewById(R.id.tvGuardian)).setText(" · " + NameUtils.firstName(log.guardianName)
                    + " · " + Ui.time(requireContext(), log.timestamp));
            TextView status = row.findViewById(R.id.tvStatus);
            boolean override = PickupLog.OVERRIDE.equals(log.outcome);
            status.setText(override ? R.string.status_override : R.string.status_verified);
            status.setTextColor(ContextCompat.getColor(requireContext(),
                    override ? R.color.status_override_text : R.color.status_verified_text));
            list.addView(row);
        }
        list.setVisibility(week.isEmpty() ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.tvWeekEmpty).setVisibility(week.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
