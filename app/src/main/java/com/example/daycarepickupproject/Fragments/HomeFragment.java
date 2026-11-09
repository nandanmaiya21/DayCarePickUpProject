package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    }
}
