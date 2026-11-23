package com.example.daycarepickupproject.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.daycarepickupproject.LockScreen;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.Models.Session;
import com.example.daycarepickupproject.R;

import java.util.Calendar;
import java.util.Date;

/** Small shared helpers for screens. */
public final class Ui {

    public static final long DAY_MS = 24L * 60 * 60 * 1000;

    private Ui() {
    }

    /** Pads the root view so content isn't hidden behind the status / navigation bars. */
    public static void applySystemBarInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    /**
     * Sends the user back to the lock screen if nobody is signed in
     * (e.g. after Android restarted the app). Returns false in that case.
     */
    public static boolean ensureSignedIn(Activity activity) {
        if (Session.isSignedIn()) {
            return true;
        }
        Intent intent = new Intent(activity, LockScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        return false;
    }

    /** Styles a badge for VERIFIED / OVERRIDE / REJECTED. */
    public static void applyBadge(TextView badge, String outcome) {
        Context c = badge.getContext();
        if (PickupLog.VERIFIED.equals(outcome)) {
            badge.setBackgroundResource(R.drawable.bg_badge_verified);
            badge.setTextColor(ContextCompat.getColor(c, R.color.status_verified_text));
            badge.setText(R.string.status_verified);
        } else if (PickupLog.OVERRIDE.equals(outcome)) {
            badge.setBackgroundResource(R.drawable.bg_badge_override);
            badge.setTextColor(ContextCompat.getColor(c, R.color.status_override_text));
            badge.setText(R.string.status_override);
        } else {
            badge.setBackgroundResource(R.drawable.bg_badge_rejected);
            badge.setTextColor(ContextCompat.getColor(c, R.color.status_rejected_text));
            badge.setText(R.string.status_rejected);
        }
    }

    /** Neutral grey badge, e.g. "Still here". */
    public static void applyNeutralBadge(TextView badge, int textRes) {
        badge.setBackgroundResource(R.drawable.bg_badge_neutral);
        badge.setTextColor(ContextCompat.getColor(badge.getContext(), R.color.text_secondary));
        badge.setText(textRes);
    }

    /** "3:42 PM" (or 24-hour, following the device setting). */
    public static String time(Context context, long timestamp) {
        return DateFormat.getTimeFormat(context).format(new Date(timestamp));
    }

    /** Midnight at the start of the day containing the timestamp. */
    public static long startOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long startOfToday() {
        return startOfDay(System.currentTimeMillis());
    }

    /** End (exclusive) of the day that starts at dayStart; handles daylight-saving changes. */
    public static long endOfDay(long dayStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayStart);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    /** Match score (0..1) as "97.8%". */
    public static String percent(Context context, float score) {
        return context.getString(R.string.score_percent, score * 100f);
    }
}
