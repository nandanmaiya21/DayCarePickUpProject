package com.example.daycarepickupproject.Face;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

/**
 * CameraX analyzer: converts each camera frame to an upright bitmap, finds faces with
 * ML Kit (on-device), and hands the result to a listener. The listener runs on the
 * camera's background thread, so it may do heavy work (like computing an embedding).
 */
public class FaceAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "FaceAnalyzer";

    /** Why a frame can or can't be used. */
    public enum FaceStatus { OK, NO_FACE, MULTIPLE_FACES, TOO_FAR, NOT_FACING }

    public static final class Frame {
        public final Bitmap bitmap;
        @Nullable public final Face face;
        public final FaceStatus status;
        public final long timestampMs;

        Frame(Bitmap bitmap, @Nullable Face face, FaceStatus status, long timestampMs) {
            this.bitmap = bitmap;
            this.face = face;
            this.status = status;
            this.timestampMs = timestampMs;
        }
    }

    public interface Listener {
        /** Called on a background thread for every analysed frame. */
        void onFrame(@NonNull Frame frame);
    }

    private final FaceDetector detector;
    private final Listener listener;
    private volatile boolean paused;

    public FaceAnalyzer(Listener listener) {
        this.listener = listener;
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();
        detector = FaceDetection.getClient(options);
    }

    /** While paused, frames are dropped without running detection. */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        try {
            if (paused) {
                return;
            }
            Bitmap bitmap = upright(image);
            List<Face> faces = Tasks.await(detector.process(InputImage.fromBitmap(bitmap, 0)));
            Face face = null;
            FaceStatus status;
            if (faces.isEmpty()) {
                status = FaceStatus.NO_FACE;
            } else if (faces.size() > 1) {
                status = FaceStatus.MULTIPLE_FACES;
            } else {
                face = faces.get(0);
                status = check(face, bitmap.getWidth());
            }
            listener.onFrame(new Frame(bitmap, face, status, System.currentTimeMillis()));
        } catch (Exception e) {
            Log.w(TAG, "Frame analysis failed", e);
        } finally {
            image.close();
        }
    }

    public void close() {
        detector.close();
    }

    private static FaceStatus check(Face face, int imageWidth) {
        Rect box = face.getBoundingBox();
        if (box.width() < imageWidth * FaceConfig.MIN_FACE_WIDTH_RATIO) {
            return FaceStatus.TOO_FAR;
        }
        if (Math.abs(face.getHeadEulerAngleY()) > FaceConfig.MAX_HEAD_ANGLE
                || Math.abs(face.getHeadEulerAngleZ()) > FaceConfig.MAX_HEAD_ANGLE) {
            return FaceStatus.NOT_FACING;
        }
        return FaceStatus.OK;
    }

    private static Bitmap upright(ImageProxy image) {
        Bitmap bitmap = image.toBitmap();
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
