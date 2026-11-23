package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.HomeScreen;
import com.example.daycarepickupproject.Models.NameUtils;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.Models.Session;
import com.example.daycarepickupproject.Models.Staff;
import com.example.daycarepickupproject.R;
import com.example.daycarepickupproject.Views.ChildDialog;
import com.example.daycarepickupproject.Views.Ui;

import java.util.List;

/**
 * Home tab: greeting, today's numbers, quick actions and the latest pick-ups.
 */
public class HomeFragment extends Fragment {

    private static final int RECENT_COUNT = 3;

    /** Everything the Home tab shows, loaded in one go. */
    private static final class Summary {
        int pickedUp;
        int stillHere;
        int overrides;
        List<PickupLog> recent;
    }

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeScreen home = (HomeScreen) requireActivity();

        Staff staff = Session.getStaff();
        if (staff != null) {
            ((TextView) view.findViewById(R.id.tvStaffInitials)).setText(staff.initials());
            ((TextView) view.findViewById(R.id.tvGreeting))
                    .setText(getString(R.string.home_greeting, NameUtils.firstName(staff.name)));
        }

        view.findViewById(R.id.btnSignOut).setOnClickListener(v -> home.signOut());
        view.findViewById(R.id.btnStartFaceScan).setOnClickListener(v -> home.startVerify());
        view.findViewById(R.id.btnEnrolGuardian).setOnClickListener(v -> home.startEnrolment(-1));
        view.findViewById(R.id.btnAddChild).setOnClickListener(v ->
                ChildDialog.show(requireContext(), null, child -> load()));
        view.findViewById(R.id.btnSeeLog).setOnClickListener(v -> home.selectTab(R.id.nav_log));
    }

    @Override
    public void onResume() {
        super.onResume();
        load(); // refresh after returning from Verify / Enrol
    }

    private void load() {
        long since = Ui.startOfToday();
        Repository.run(requireContext(), dao -> {
            Summary s = new Summary();
            s.pickedUp = dao.getPickedUpChildIdsSince(since).size();
            s.stillHere = Math.max(0, dao.countChildren() - s.pickedUp);
            s.overrides = dao.countOutcomeSince(PickupLog.OVERRIDE, since);
            s.recent = dao.getRecentPickups(since, RECENT_COUNT);
            return s;
        }, this::show);
    }

    private void show(Summary s) {
        View view = getView();
        if (view == null) {
            return;
        }
        ((TextView) view.findViewById(R.id.tvPickedUpCount)).setText(String.valueOf(s.pickedUp));
        ((TextView) view.findViewById(R.id.tvStillHereCount)).setText(String.valueOf(s.stillHere));
        ((TextView) view.findViewById(R.id.tvOverridesCount)).setText(String.valueOf(s.overrides));

        LinearLayout list = view.findViewById(R.id.recentPickupsList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < s.recent.size(); i++) {
            PickupLog log = s.recent.get(i);
            if (i > 0) {
                inflater.inflate(R.layout.item_divider, list, true);
            }
            View row = inflater.inflate(R.layout.item_pickup_row, list, false);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(NameUtils.initials(log.childName));
            ((TextView) row.findViewById(R.id.tvName)).setText(log.childName);
            ((TextView) row.findViewById(R.id.tvDetail)).setText(getString(R.string.detail_dot,
                    log.guardianName, Ui.time(requireContext(), log.timestamp)));
            Ui.applyBadge(row.findViewById(R.id.tvBadge), log.outcome);
            list.addView(row);
        }
        boolean empty = s.recent.isEmpty();
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.tvRecentEmpty).setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
