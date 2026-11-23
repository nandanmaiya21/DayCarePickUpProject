package com.example.daycarepickupproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.daycarepickupproject.Fragments.ChildProfileFragment;
import com.example.daycarepickupproject.Fragments.ChildrenFragment;
import com.example.daycarepickupproject.Fragments.HomeFragment;
import com.example.daycarepickupproject.Fragments.LogFragment;
import com.example.daycarepickupproject.Models.Session;
import com.example.daycarepickupproject.Views.Ui;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Hosts the bottom navigation and swaps the fragment for the selected tab.
 * The Verify tab opens the camera screen.
 */
public class HomeScreen extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Ui.ensureSignedIn(this)) {
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        Ui.applySystemBarInsets(findViewById(R.id.main));

        bottomNav = findViewById(R.id.bottomNav);
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
            } else if (id == R.id.nav_verify) {
                startVerify();
                return false; // stay on the current tab
            }
            return false;
        });
        // Tapping the current tab again returns to its top level (e.g. child profile -> list)
        bottomNav.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.nav_verify) {
                startVerify();
            } else {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        // First launch: show the Home tab (skip on rotation, the fragment is restored)
        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }
    }

    /** Switches to another bottom-nav tab. */
    public void selectTab(int menuId) {
        bottomNav.setSelectedItemId(menuId);
    }

    public void startVerify() {
        startActivity(new Intent(this, VerifyScreen.class));
    }

    public void startEnrolment(long childId) {
        Intent intent = new Intent(this, EnrollmentScreen.class);
        if (childId >= 0) {
            intent.putExtra(EnrollmentScreen.EXTRA_CHILD_ID, childId);
        }
        startActivity(intent);
    }

    /** Opens a child's profile on top of the Children list (Back returns to the list). */
    public void openChildProfile(long childId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, ChildProfileFragment.newInstance(childId))
                .addToBackStack(null)
                .commit();
    }

    public void signOut() {
        Session.signOut();
        Intent intent = new Intent(this, LockScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
