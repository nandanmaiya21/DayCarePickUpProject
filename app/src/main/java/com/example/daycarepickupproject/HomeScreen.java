package com.example.daycarepickupproject;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.Fragments.ChildrenFragment;
import com.example.daycarepickupproject.Fragments.HomeFragment;
import com.example.daycarepickupproject.Fragments.LogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Hosts the bottom navigation and swaps the fragment for the selected tab.
 */
public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_children) {
                showFragment(new ChildrenFragment());
                return true;
            } else if (id == R.id.nav_log) {
                showFragment(new LogFragment());
                return true;
            }
            // Verify tab doesn't have a fragment yet
            return false;
        });

        // First launch: show the Home tab (skip on rotation, the fragment is restored)
        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
