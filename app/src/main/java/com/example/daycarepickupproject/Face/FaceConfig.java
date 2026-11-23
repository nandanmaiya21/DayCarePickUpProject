package com.example.daycarepickupproject.Face;

/**
 * Tunable numbers for face recognition and the blink check, in one place.
 * Re-tune MATCH_THRESHOLD against the team's test dataset: raising it lowers the
 * false-acceptance rate, lowering it reduces false rejections.
 */
public final class FaceConfig {

    private FaceConfig() {
    }

    /** Cosine similarity a face must reach against a guardian's samples to count as a match. */
    public static final float MATCH_THRESHOLD = 0.70f;

    /**
     * The best guardian must beat the second-best guardian by this much, so two
     * similar-looking guardians are never confused with each other.
     */
    public static final float MATCH_MARGIN = 0.05f;

    /** Face photos taken when enrolling a guardian. */
    public static final int ENROL_MIN_PHOTOS = 3;
    public static final int ENROL_MAX_PHOTOS = 5;

    /** Minimum time between two automatic enrolment photos. */
    public static final long ENROL_CAPTURE_INTERVAL_MS = 700;

    /** Eye-open probability (from ML Kit) above which an eye counts as open / below which closed. */
    public static final float EYE_OPEN = 0.60f;
    public static final float EYE_CLOSED = 0.25f;

    /** How long the guardian has to blink before the attempt fails the liveness check. */
    public static final long BLINK_TIMEOUT_MS = 8000;

    /** Face must be turned less than this many degrees (left/right and tilt) to be used. */
    public static final float MAX_HEAD_ANGLE = 18f;

    /** Face box must be at least this fraction of the image width (i.e. close enough). */
    public static final float MIN_FACE_WIDTH_RATIO = 0.25f;
}
