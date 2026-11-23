package com.example.daycarepickupproject.Models;

import androidx.annotation.Nullable;

/** The staff member currently signed in (kept in memory only). */
public final class Session {

    @Nullable
    private static Staff currentStaff;

    private Session() {
    }

    public static void signIn(Staff staff) {
        currentStaff = staff;
    }

    public static void signOut() {
        currentStaff = null;
    }

    @Nullable
    public static Staff getStaff() {
        return currentStaff;
    }

    public static boolean isSignedIn() {
        return currentStaff != null;
    }
}
