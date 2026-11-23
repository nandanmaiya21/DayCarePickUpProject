package com.example.daycarepickupproject;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.daycarepickupproject.Face.BlinkDetector;
import com.example.daycarepickupproject.Face.FaceMatcher;
import com.example.daycarepickupproject.Face.FaceMath;

import org.junit.Test;

public class FaceLogicTest {

    private static float[] unit(float... v) {
        return FaceMath.normalize(v);
    }

    @Test
    public void normalizeGivesLengthOne() {
        float[] v = unit(3f, 4f);
        assertEquals(0.6f, v[0], 1e-6);
        assertEquals(0.8f, v[1], 1e-6);
    }

    @Test
    public void cosineOfSameVectorIsOne() {
        float[] v = unit(1f, 2f, 3f);
        assertEquals(1f, FaceMath.cosine(v, v), 1e-5);
    }

    @Test
    public void bytesRoundTrip() {
        float[] v = {0.25f, -1.5f, 3.75f};
        assertArrayEquals(v, FaceMath.fromBytes(FaceMath.toBytes(v)), 0f);
    }

    @Test
    public void matcherAcceptsCloseFace() {
        FaceMatcher m = new FaceMatcher(0.7f, 0.05f);
        m.add(1, unit(1f, 0f, 0f));
        m.add(2, unit(0f, 1f, 0f));
        FaceMatcher.Result r = m.match(unit(0.95f, 0.1f, 0f));
        assertTrue(r.isMatch());
        assertEquals(1, r.guardianId);
    }

    @Test
    public void matcherRejectsUnknownFace() {
        FaceMatcher m = new FaceMatcher(0.7f, 0.05f);
        m.add(1, unit(1f, 0f, 0f));
        FaceMatcher.Result r = m.match(unit(0f, 0f, 1f));
        assertFalse(r.isMatch());
        assertEquals(-1, r.guardianId);
    }

    @Test
    public void matcherRejectsAmbiguousFace() {
        FaceMatcher m = new FaceMatcher(0.7f, 0.05f);
        m.add(1, unit(1f, 0.02f, 0f));
        m.add(2, unit(1f, -0.02f, 0f));
        // Equally close to both guardians -> not accepted
        assertFalse(m.match(unit(1f, 0f, 0f)).isMatch());
    }

    @Test
    public void matcherUsesBestSampleOfGuardian() {
        FaceMatcher m = new FaceMatcher(0.7f, 0.05f);
        m.add(1, unit(0f, 1f, 0f));
        m.add(1, unit(1f, 0f, 0f));
        assertEquals(1, m.match(unit(1f, 0f, 0f)).guardianId);
    }

    @Test
    public void blinkNeedsOpenClosedOpen() {
        BlinkDetector b = new BlinkDetector(0.6f, 0.25f);
        assertFalse(b.onFrame(1, 0.9f, 0.9f));
        assertFalse(b.onFrame(1, 0.1f, 0.1f));
        assertTrue(b.onFrame(1, 0.9f, 0.9f));
        assertEquals(BlinkDetector.State.BLINKED, b.getState());
    }

    @Test
    public void photoWithEyesAlwaysOpenNeverBlinks() {
        BlinkDetector b = new BlinkDetector(0.6f, 0.25f);
        for (int i = 0; i < 100; i++) {
            assertFalse(b.onFrame(1, 0.95f, 0.95f));
        }
    }

    @Test
    public void blinkRestartsWhenFaceChanges() {
        BlinkDetector b = new BlinkDetector(0.6f, 0.25f);
        b.onFrame(1, 0.9f, 0.9f);
        b.onFrame(1, 0.1f, 0.1f);
        // A different tracked face opens its eyes: must not count as person 1's blink
        assertFalse(b.onFrame(2, 0.9f, 0.9f));
        assertEquals(BlinkDetector.State.EYES_OPEN, b.getState());
    }

    @Test
    public void oneEyeClosedIsNotABlink() {
        BlinkDetector b = new BlinkDetector(0.6f, 0.25f);
        b.onFrame(1, 0.9f, 0.9f);
        b.onFrame(1, 0.1f, 0.9f); // wink
        assertFalse(b.onFrame(1, 0.9f, 0.9f));
    }
}
