package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * A motion segment describes the motion model used during a frame interval.
 *
 * Example:
 * frames 1-30: free Brownian motion
 * frames 31-60: confined Brownian motion
 * frames 61-80: immobile / trapped state
 */
public class MotionSegment {

    private final int startFrame;
    private final int endFrame;
    private final MotionMode motionMode;

    private final double diffusionCoefficientUm2PerSecond;

    private final double driftVelocityXUmPerSecond;
    private final double driftVelocityYUmPerSecond;

    private final double confinementRadiusPixel;

    private final String label;

    public MotionSegment(
            int startFrame,
            int endFrame,
            MotionMode motionMode,
            double diffusionCoefficientUm2PerSecond,
            double driftVelocityXUmPerSecond,
            double driftVelocityYUmPerSecond,
            double confinementRadiusPixel,
            String label
    ) {
        if (startFrame < 1) {
            throw new IllegalArgumentException("startFrame must be >= 1.");
        }

        if (endFrame < startFrame) {
            throw new IllegalArgumentException("endFrame must be >= startFrame.");
        }

        if (motionMode == null) {
            throw new IllegalArgumentException("motionMode cannot be null.");
        }

        if (diffusionCoefficientUm2PerSecond < 0) {
            throw new IllegalArgumentException("diffusionCoefficient must be >= 0.");
        }

        if (confinementRadiusPixel < 0) {
            throw new IllegalArgumentException("confinementRadiusPixel must be >= 0.");
        }

        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.motionMode = motionMode;
        this.diffusionCoefficientUm2PerSecond = diffusionCoefficientUm2PerSecond;
        this.driftVelocityXUmPerSecond = driftVelocityXUmPerSecond;
        this.driftVelocityYUmPerSecond = driftVelocityYUmPerSecond;
        this.confinementRadiusPixel = confinementRadiusPixel;
        this.label = label == null ? "" : label;
    }

    public static MotionSegment freeBrownian(
            int startFrame,
            int endFrame,
            double diffusionCoefficientUm2PerSecond
    ) {
        return new MotionSegment(
                startFrame,
                endFrame,
                MotionMode.FREE_BROWNIAN,
                diffusionCoefficientUm2PerSecond,
                0.0,
                0.0,
                0.0,
                "free_brownian"
        );
    }

    public static MotionSegment confinedBrownian(
            int startFrame,
            int endFrame,
            double diffusionCoefficientUm2PerSecond,
            double confinementRadiusPixel
    ) {
        return new MotionSegment(
                startFrame,
                endFrame,
                MotionMode.CONFINED_BROWNIAN,
                diffusionCoefficientUm2PerSecond,
                0.0,
                0.0,
                confinementRadiusPixel,
                "confined_brownian"
        );
    }

    public static MotionSegment immobile(
            int startFrame,
            int endFrame
    ) {
        return new MotionSegment(
                startFrame,
                endFrame,
                MotionMode.IMMOBILE,
                0.0,
                0.0,
                0.0,
                0.0,
                "immobile"
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

    public MotionMode getMotionMode() {
        return motionMode;
    }

    public double getDiffusionCoefficientUm2PerSecond() {
        return diffusionCoefficientUm2PerSecond;
    }

    public double getDriftVelocityXUmPerSecond() {
        return driftVelocityXUmPerSecond;
    }

    public double getDriftVelocityYUmPerSecond() {
        return driftVelocityYUmPerSecond;
    }

    public double getConfinementRadiusPixel() {
        return confinementRadiusPixel;
    }

    public String getLabel() {
        return label;
    }
}
