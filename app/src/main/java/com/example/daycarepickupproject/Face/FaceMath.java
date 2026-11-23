package com.example.daycarepickupproject.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Vector helpers for face embeddings. Pure Java so it can be unit-tested. */
public final class FaceMath {

    private FaceMath() {
    }

    /** Scales the vector to length 1 (in place) and returns it. */
    public static float[] normalize(float[] v) {
        double sum = 0;
        for (float x : v) {
            sum += x * x;
        }
        double norm = Math.sqrt(sum);
        if (norm > 0) {
            for (int i = 0; i < v.length; i++) {
                v[i] = (float) (v[i] / norm);
            }
        }
        return v;
    }

    /** Cosine similarity of two L2-normalised vectors (their dot product), in -1..1. */
    public static float cosine(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Embedding sizes differ: " + a.length + " vs " + b.length);
        }
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return (float) dot;
    }

    public static byte[] toBytes(float[] v) {
        ByteBuffer buffer = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float x : v) {
            buffer.putFloat(x);
        }
        return buffer.array();
    }

    public static float[] fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] v = new float[bytes.length / 4];
        for (int i = 0; i < v.length; i++) {
            v[i] = buffer.getFloat();
        }
        return v;
    }
}
