package com.example.daycarepickupproject.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Small face thumbnail per guardian so staff can see who is enrolled.
 * Stored in the app's private storage only (never backed up or shared).
 */
public final class GuardianPhotos {

    private GuardianPhotos() {
    }

    private static File file(Context context, long guardianId) {
        File dir = new File(context.getFilesDir(), "faces");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return new File(dir, "guardian_" + guardianId + ".jpg");
    }

    public static void save(Context context, long guardianId, Bitmap bitmap) {
        try (FileOutputStream out = new FileOutputStream(file(context, guardianId))) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        } catch (IOException ignored) {
            // A missing thumbnail only means the placeholder silhouette is shown
        }
    }

    @Nullable
    public static Bitmap load(Context context, long guardianId) {
        File f = file(context, guardianId);
        return f.exists() ? BitmapFactory.decodeFile(f.getAbsolutePath()) : null;
    }

    public static void delete(Context context, long guardianId) {
        //noinspection ResultOfMethodCallIgnored
        file(context, guardianId).delete();
    }
}
