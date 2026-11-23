package com.example.daycarepickupproject.Face;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

/**
 * Cuts the face out of a camera frame, rotated so the eyes are level,
 * and scaled to the square size the embedding model expects.
 */
public final class FaceAligner {

    /** Extra space around the detected face box (fraction of its size). */
    private static final float MARGIN = 0.05f;

    private FaceAligner() {
    }

    public static Bitmap align(Bitmap frame, Face face, int size) {
        Rect box = face.getBoundingBox();
        float cx = box.exactCenterX();
        float cy = box.exactCenterY();
        float faceSize = Math.max(box.width(), box.height()) * (1f + MARGIN);

        float angle = 0f;
        FaceLandmark left = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark right = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (left != null && right != null) {
            PointF l = left.getPosition();
            PointF r = right.getPosition();
            // ML Kit's "left eye" is the subject's left, which is on the right of the image
            angle = (float) Math.toDegrees(Math.atan2(l.y - r.y, l.x - r.x));
        }

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cx, -cy);
        matrix.postRotate(-angle);
        float scale = size / faceSize;
        matrix.postScale(scale, scale);
        matrix.postTranslate(size / 2f, size / 2f);

        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(frame, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
        return out;
    }
}
