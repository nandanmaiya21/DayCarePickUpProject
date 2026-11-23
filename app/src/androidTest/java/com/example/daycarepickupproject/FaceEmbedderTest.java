package com.example.daycarepickupproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.daycarepickupproject.Face.FaceEmbedder;
import com.example.daycarepickupproject.Face.FaceMath;

import org.junit.Test;
import org.junit.runner.RunWith;

/** Checks the bundled face model loads and produces normalised embeddings quickly. */
@RunWith(AndroidJUnit4.class)
public class FaceEmbedderTest {

    @Test
    public void modelProducesNormalisedEmbedding() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FaceEmbedder embedder = new FaceEmbedder(context);
        int size = embedder.getInputSize();
        assertEquals(112, size);

        Bitmap a = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        a.eraseColor(Color.rgb(200, 150, 120));
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        b.eraseColor(Color.rgb(30, 60, 200));

        embedder.embed(a); // warm-up
        long start = SystemClock.elapsedRealtime();
        float[] ea = embedder.embed(a);
        long ms = SystemClock.elapsedRealtime() - start;
        float[] eb = embedder.embed(b);
        Log.i("FaceEmbedderTest", "embedding size=" + ea.length + " time=" + ms + "ms");

        assertTrue(ea.length >= 128);
        assertEquals(1f, FaceMath.cosine(ea, ea), 1e-3);
        assertEquals(1f, FaceMath.cosine(ea, embedder.embed(a)), 1e-4);
        assertTrue(FaceMath.cosine(ea, eb) < 0.999f);
        assertTrue("embedding took " + ms + " ms", ms < 1000);
        embedder.close();
    }
}
