package com.example.daycarepickupproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.EnrollmentScreen;
import com.example.daycarepickupproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Home tab. Data binding will be added once the models exist.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // "See log" opens the Log tab
        view.findViewById(R.id.btnSeeLog).setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
            nav.setSelectedItemId(R.id.nav_log);
        });

        // "Enrol guardian" and "Add child" both open the enrollment screen
        view.findViewById(R.id.btnEnrolGuardian).setOnClickListener(v ->
                openEnrollment(EnrollmentScreen.MODE_GUARDIAN));
        view.findViewById(R.id.btnAddChild).setOnClickListener(v ->
                openEnrollment(EnrollmentScreen.MODE_CHILD));
    }

    private void openEnrollment(String mode) {
        Intent intent = new Intent(requireContext(), EnrollmentScreen.class);
        intent.putExtra(EnrollmentScreen.EXTRA_MODE, mode);
        startActivity(intent);
    }
}