package com.example.daycarepickupproject.Face;

/**
 * Simple liveness check: the same tracked face must go eyes-open -> eyes-closed -> eyes-open.
 * A printed photo or a still image on a screen cannot do this.
 * Pure Java (no Android types) so it can be unit-tested.
 */
public class BlinkDetector {

    public enum State {
        /** Waiting to see the eyes open. */
        WAITING_FOR_OPEN,
        /** Eyes seen open; waiting for them to close. */
        EYES_OPEN,
        /** Eyes seen closed; waiting for them to open again. */
        EYES_CLOSED,
        /** A full blink was seen. */
        BLINKED
    }

    private final float openThreshold;
    private final float closedThreshold;

    private State state = State.WAITING_FOR_OPEN;
    private Integer trackingId;

    public BlinkDetector(float openThreshold, float closedThreshold) {
        this.openThreshold = openThreshold;
        this.closedThreshold = closedThreshold;
    }

    public void reset() {
        state = State.WAITING_FOR_OPEN;
        trackingId = null;
    }

    public State getState() {
        return state;
    }

    /**
     * Feeds one frame. Probabilities may be null when the detector could not judge the eyes.
     * A different trackingId means a different face, which restarts the check.
     *
     * @return true if this frame completed the blink.
     */
    public boolean onFrame(Integer faceTrackingId, Float leftOpen, Float rightOpen) {
        if (state == State.BLINKED) {
            return false;
        }
        if (faceTrackingId != null && trackingId != null && !faceTrackingId.equals(trackingId)) {
            reset();
        }
        if (faceTrackingId != null) {
            trackingId = faceTrackingId;
        }
        if (leftOpen == null || rightOpen == null) {
            return false;
        }
        boolean open = leftOpen > openThreshold && rightOpen > openThreshold;
        boolean closed = leftOpen < closedThreshold && rightOpen < closedThreshold;

        switch (state) {
            case WAITING_FOR_OPEN:
                if (open) {
                    state = State.EYES_OPEN;
                }
                break;
            case EYES_OPEN:
                if (closed) {
                    state = State.EYES_CLOSED;
                }
                break;
            case EYES_CLOSED:
                if (open) {
                    state = State.BLINKED;
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    public boolean eyesOpen(Float leftOpen, Float rightOpen) {
        return leftOpen != null && rightOpen != null
                && leftOpen > openThreshold && rightOpen > openThreshold;
    }
}
