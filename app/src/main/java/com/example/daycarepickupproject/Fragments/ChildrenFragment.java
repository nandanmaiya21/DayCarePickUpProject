package com.example.daycarepickupproject.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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

    private ScrollView childScroll;
    private ImageButton btnBack;
    private TextView btnEdit, tvChildInitials, tvChildName, tvChildRoomAge, tvChildStatus;
    private LinearLayout guardiansList, weekList;
    private TextView btnEnrolGuardian3;
    private FrameLayout btnAddGuardian;

    private void bindViews(View view) {
        childScroll = view.findViewById(R.id.childScroll);
        btnBack = view.findViewById(R.id.btnBack);
        btnEdit = view.findViewById(R.id.btnEdit);
        tvChildInitials = view.findViewById(R.id.tvChildInitials);
        tvChildName = view.findViewById(R.id.tvChildName);
        tvChildRoomAge = view.findViewById(R.id.tvChildRoomAge);
        tvChildStatus = view.findViewById(R.id.tvChildStatus);
        guardiansList = view.findViewById(R.id.guardiansList);
        btnEnrolGuardian3 = view.findViewById(R.id.btnEnrolGuardian3);
        btnAddGuardian = view.findViewById(R.id.btnAddGuardian);
        weekList = view.findViewById(R.id.weekList);
    }

    public ChildrenFragment() {
        super(R.layout.fragment_children);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
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
