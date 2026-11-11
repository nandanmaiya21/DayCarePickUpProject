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
 * Children tab: shows a child's profile, guardians and this week's pick-ups.
 */
public class ChildrenFragment extends Fragment {

    public ChildrenFragment() {
        super(R.layout.fragment_children);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back arrow returns to the Home tab
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
            nav.setSelectedItemId(R.id.nav_home);
        });

        // Enrol guardian 3 and Add guardian both open the enrollment screen in guardian mode
        view.findViewById(R.id.btnEnrolGuardian3).setOnClickListener(v ->
                openEnrollment());
        view.findViewById(R.id.btnAddGuardian).setOnClickListener(v ->
                openEnrollment());
    }

    private void openEnrollment() {
        Intent intent = new Intent(requireContext(), EnrollmentScreen.class);
        intent.putExtra(EnrollmentScreen.EXTRA_MODE, EnrollmentScreen.MODE_GUARDIAN);
        startActivity(intent);
    }
}
