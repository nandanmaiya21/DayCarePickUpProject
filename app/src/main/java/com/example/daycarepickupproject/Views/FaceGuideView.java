package com.example.daycarepickupproject.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.daycarepickupproject.R;

/** Darkens the camera preview except for an oval where the face should go. */
public class FaceGuideView extends View {

    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF oval = new RectF();

    public FaceGuideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scrimPaint.setColor(ContextCompat.getColor(context, R.color.camera_scrim));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(getResources().getDisplayMetrics().density * 4);
        strokePaint.setColor(ContextCompat.getColor(context, R.color.guide_idle));
    }

    /** Colour of the oval outline (e.g. green when the face is good). */
    public void setGuideColor(@ColorInt int color) {
        if (strokePaint.getColor() != color) {
            strokePaint.setColor(color);
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float ovalWidth = w * 0.68f;
        float ovalHeight = Math.min(ovalWidth * 1.3f, h * 0.55f);
        float cx = w / 2f;
        float cy = h * 0.40f;
        oval.set(cx - ovalWidth / 2, cy - ovalHeight / 2, cx + ovalWidth / 2, cy + ovalHeight / 2);
        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.addRect(0, 0, w, h, Path.Direction.CW);
        path.addOval(oval, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, scrimPaint);
        canvas.drawOval(oval, strokePaint);
    }
}
