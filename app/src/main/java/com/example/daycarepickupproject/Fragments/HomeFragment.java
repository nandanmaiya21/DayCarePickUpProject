package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Home tab. Data binding will be added once the models exist.
 */
public class HomeFragment extends Fragment {

    private ScrollView homeScroll;
    private TextView tvStaffInitials, tvGreeting, tvOfflineReady;
    private ImageButton btnSignOut;
    private MaterialButton btnStartFaceScan;
    private TextView tvPickedUpCount, tvStillHereCount, tvOverridesCount;
    private TextView btnEnrolGuardian, btnAddChild, btnSeeLog;
    private LinearLayout recentPickupsList;

    private void bindViews(View view) {
        homeScroll = view.findViewById(R.id.homeScroll);
        tvStaffInitials = view.findViewById(R.id.tvStaffInitials);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        tvOfflineReady = view.findViewById(R.id.tvOfflineReady);
        btnStartFaceScan = view.findViewById(R.id.btnStartFaceScan);
        tvPickedUpCount = view.findViewById(R.id.tvPickedUpCount);
        tvStillHereCount = view.findViewById(R.id.tvStillHereCount);
        tvOverridesCount = view.findViewById(R.id.tvOverridesCount);
        btnEnrolGuardian = view.findViewById(R.id.btnEnrolGuardian);
        btnAddChild = view.findViewById(R.id.btnAddChild);
        btnSeeLog = view.findViewById(R.id.btnSeeLog);
        recentPickupsList = view.findViewById(R.id.recentPickupsList);
    }

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        // "See log" opens the Log tab
        view.findViewById(R.id.btnSeeLog).setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
            nav.setSelectedItemId(R.id.nav_log);
        });
    }
}
