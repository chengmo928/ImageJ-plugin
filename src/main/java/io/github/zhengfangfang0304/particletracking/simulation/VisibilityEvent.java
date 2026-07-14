package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * A visibility event controls whether particles are rendered in the image.
 *
 * The physical ground truth trajectory can still exist even when the particle
 * is invisible in the rendered movie.
 */
public class VisibilityEvent {

    private final int startFrame;
    private final int endFrame;
    private final VisibilityEventType type;
    private final double probabilityPerFrame;
    private final String label;

    public VisibilityEvent(
            int startFrame,
            int endFrame,
            VisibilityEventType type,
            double probabilityPerFrame,
            String label
    ) {
        if (startFrame < 1) {
            throw new IllegalArgumentException("startFrame must be >= 1.");
        }

        if (endFrame < startFrame) {
            throw new IllegalArgumentException("endFrame must be >= startFrame.");
        }

        if (type == null) {
            throw new IllegalArgumentException("VisibilityEventType cannot be null.");
        }

        if (probabilityPerFrame < 0.0 || probabilityPerFrame > 1.0) {
            throw new IllegalArgumentException("probabilityPerFrame must be between 0 and 1.");
        }

        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.type = type;
        this.probabilityPerFrame = probabilityPerFrame;
        this.label = label == null ? "" : label;
    }

    public static VisibilityEvent blinking(
            int startFrame,
            int endFrame,
            double probabilityPerFrame
    ) {
        return new VisibilityEvent(
                startFrame,
                endFrame,
                VisibilityEventType.BLINKING,
                probabilityPerFrame,
                "blinking"
        );
    }

    public static VisibilityEvent photobleaching(
            int startFrame,
            int endFrame,
            double probabilityPerFrame
    ) {
        return new VisibilityEvent(
                startFrame,
                endFrame,
                VisibilityEventType.PHOTOBLEACHING,
                probabilityPerFrame,
                "photobleaching"
        );
    }

    public static VisibilityEvent forcedInvisible(
            int startFrame,
            int endFrame
    ) {
        return new VisibilityEvent(
                startFrame,
                endFrame,
                VisibilityEventType.FORCED_INVISIBLE,
                1.0,
                "forced_invisible"
        );
    }

    public boolean containsFrame(int frame) {
        return frame >= startFrame && frame <= endFrame;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    public VisibilityEventType getType() {
        return type;
    }

    public double getProbabilityPerFrame() {
        return probabilityPerFrame;
    }

    public String getLabel() {
        return label;
    }
}
