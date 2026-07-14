package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * Ground-truth particle position at one frame.
 *
 * The particle can exist in the ground truth trajectory
 * even when it is not visible in the rendered image.
 */
public class SyntheticParticle {

    private final int particleId;

    private final int frame;

    private final double x;

    private final double y;

    private final double intensity;

    private final boolean visible;

    private final String visibilityReason;

    private final MotionMode motionMode;

    private final double diffusionCoefficientUm2PerSecond;

    private final double driftVelocityXUmPerSecond;

    private final double driftVelocityYUmPerSecond;

    private final double confinementRadiusPixel;

    private final String stateLabel;

    public SyntheticParticle(
            int particleId,
            int frame,
            double x,
            double y,
            double intensity
    ) {
        this(
                particleId,
                frame,
                x,
                y,
                intensity,
                true,
                "NONE"
        );
    }

    public SyntheticParticle(
            int particleId,
            int frame,
            double x,
            double y,
            double intensity,
            boolean visible,
            String visibilityReason
    ) {
        this(
                particleId,
                frame,
                x,
                y,
                intensity,
                visible,
                visibilityReason,
                null,
                0.0,
                0.0,
                0.0,
                0.0,
                "unknown"
        );
    }

    public SyntheticParticle(
            int particleId,
            int frame,
            double x,
            double y,
            double intensity,
            boolean visible,
            String visibilityReason,
            MotionMode motionMode,
            double diffusionCoefficientUm2PerSecond,
            double driftVelocityXUmPerSecond,
            double driftVelocityYUmPerSecond,
            double confinementRadiusPixel,
            String stateLabel
    ) {
        this.particleId = particleId;
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.intensity = intensity;
        this.visible = visible;
        this.visibilityReason =
                visibilityReason == null
                        ? "NONE"
                        : visibilityReason;

        this.motionMode = motionMode;
        this.diffusionCoefficientUm2PerSecond =
                diffusionCoefficientUm2PerSecond;
        this.driftVelocityXUmPerSecond =
                driftVelocityXUmPerSecond;
        this.driftVelocityYUmPerSecond =
                driftVelocityYUmPerSecond;
        this.confinementRadiusPixel =
                confinementRadiusPixel;
        this.stateLabel =
                stateLabel == null
                        ? "unknown"
                        : stateLabel;
    }

    public int getParticleId() {
        return particleId;
    }

    public int getFrame() {
        return frame;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getIntensity() {
        return intensity;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getVisibilityReason() {
        return visibilityReason;
    }

    public MotionMode getMotionMode() {
        return motionMode;
    }

    public String getMotionModeName() {
        if (motionMode == null) {
            return "UNKNOWN";
        }

        return motionMode.name();
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

    public String getStateLabel() {
        return stateLabel;
    }
}