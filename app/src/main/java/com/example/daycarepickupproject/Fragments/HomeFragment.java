package com.example.daycarepickupproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.EnrollmentScreen;
import com.example.daycarepickupproject.AddChildScreen;
import com.example.daycarepickupproject.LockScreen;
import com.example.daycarepickupproject.R;
import com.example.daycarepickupproject.VerifyScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Home tab. Data binding will be added once the models exist.
 */
public class HomeFragment extends Fragment {

    private TextView tvStaffInitials;
    private TextView tvGreeting;
    private ImageButton btnSignOut;
    private TextView tvOfflineReady;
    private MaterialButton btnStartFaceScan;
    private TextView tvPickedUpCount;
    private TextView tvStillHereCount;
    private TextView tvOverridesCount;
    private TextView btnEnrolGuardian;
    private TextView btnAddChild;
    private TextView btnSeeLog;
    private LinearLayout recentPickupsList;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // "See log" opens the Log tab
        btnSeeLog.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
            nav.setSelectedItemId(R.id.nav_log);
        });

        // Sign out: back to the lock screen; Back must not return to Home
        btnSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LockScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnStartFaceScan.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), VerifyScreen.class)));

        btnEnrolGuardian.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EnrollmentScreen.class)));

        btnAddChild.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddChildScreen.class)));
    }
}