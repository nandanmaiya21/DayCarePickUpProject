package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    }
}
