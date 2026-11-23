package com.example.daycarepickupproject.Face;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compares one face embedding against every enrolled guardian.
 * A guardian's score is the best similarity over their enrolled photos.
 */
public class FaceMatcher {

    /** Outcome of a comparison. guardianId is -1 when nobody matched. */
    public static final class Result {
        public final long guardianId;
        /** Best similarity found (whether or not it was accepted), 0..1. */
        public final float bestScore;
        public final long bestGuardianId;
        public final float secondScore;

        Result(long guardianId, float bestScore, long bestGuardianId, float secondScore) {
            this.guardianId = guardianId;
            this.bestScore = bestScore;
            this.bestGuardianId = bestGuardianId;
            this.secondScore = secondScore;
        }

        public boolean isMatch() {
            return guardianId >= 0;
        }
    }

    private final Map<Long, List<float[]>> gallery = new HashMap<>();
    private final float threshold;
    private final float margin;

    public FaceMatcher(float threshold, float margin) {
        this.threshold = threshold;
        this.margin = margin;
    }

    public void add(long guardianId, float[] embedding) {
        List<float[]> list = gallery.get(guardianId);
        if (list == null) {
            list = new ArrayList<>();
            gallery.put(guardianId, list);
        }
        list.add(embedding);
    }

    public int guardianCount() {
        return gallery.size();
    }

    public Result match(float[] probe) {
        long bestId = -1;
        float best = -1f;
        float second = -1f;
        for (Map.Entry<Long, List<float[]>> entry : gallery.entrySet()) {
            float score = -1f;
            for (float[] sample : entry.getValue()) {
                score = Math.max(score, FaceMath.cosine(probe, sample));
            }
            if (score > best) {
                second = best;
                best = score;
                bestId = entry.getKey();
            } else if (score > second) {
                second = score;
            }
        }
        float bestClamped = Math.max(0f, best);
        boolean accepted = bestId >= 0
                && best >= threshold
                && (second < 0 || best - second >= margin);
        return new Result(accepted ? bestId : -1, bestClamped, bestId, Math.max(0f, second));
    }
}
