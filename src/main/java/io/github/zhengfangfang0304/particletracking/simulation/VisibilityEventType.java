package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * Visibility event types for synthetic particle data.
 */
public enum VisibilityEventType {

    /**
     * Temporary on/off blinking.
     * The particle still exists in the ground truth trajectory,
     * but it may not be rendered in the image at some frames.
     */
    BLINKING("Blinking"),

    /**
     * Permanent disappearance after photobleaching.
     */
    PHOTOBLEACHING("Photobleaching"),

    /**
     * Force particles to be invisible during a specified frame interval.
     */
    FORCED_INVISIBLE("Forced Invisible");

    private final String displayName;

    VisibilityEventType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
