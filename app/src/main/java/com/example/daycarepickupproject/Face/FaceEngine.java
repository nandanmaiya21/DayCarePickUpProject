package com.example.daycarepickupproject.Face;

import android.content.Context;

import java.io.IOException;

/** Keeps a single FaceEmbedder for the whole app (loading the model takes a moment). */
public final class FaceEngine {

    private static FaceEmbedder embedder;

    private FaceEngine() {
    }

    /** Must be called off the main thread the first time. */
    public static synchronized FaceEmbedder embedder(Context context) throws IOException {
        if (embedder == null) {
            embedder = new FaceEmbedder(context.getApplicationContext());
        }
        return embedder;
    }
}
