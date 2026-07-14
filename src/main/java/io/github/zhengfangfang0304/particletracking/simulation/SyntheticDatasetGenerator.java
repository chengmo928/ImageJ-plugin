package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate synthetic ground-truth particle trajectories.
 *
 * This generator supports two modes:
 *
 * 1. generate(SimulationConfig config)
 *    Uses a single motion mode for the whole movie.
 *
 * 2. generate(SimulationScenario scenario)
 *    Uses a motion timeline, so different frame intervals can use
 *    different motion models.
 */
public class SyntheticDatasetGenerator {

    /**
     * Backward-compatible method.
     *
     * It converts the old single-config design into a default scenario
     * with one motion segment covering all frames.
     */
    public SyntheticDataset generate(SimulationConfig config) {
        SimulationScenario scenario =
                SimulationScenario.fromConfig(config);

        return generate(scenario);
    }

    /**
     * Generate synthetic trajectories from a full simulation scenario.
     */
    public SyntheticDataset generate(SimulationScenario scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException("SimulationScenario cannot be null.");
        }

        scenario.validate();

        SimulationConfig config =
                scenario.getBaseConfig();

        Random random =
                new Random(config.randomSeed);

        Random visibilityRandom =
                new Random(config.randomSeed + 1000003L);

        int particleCount =
                config.getResolvedParticleCount();

        double[] x =
                new double[particleCount];

        double[] y =
                new double[particleCount];

        double[] vx =
                new double[particleCount];

        double[] vy =
                new double[particleCount];

        boolean[] photobleached =
                new boolean[particleCount];

        initializeParticles(
                scenario,
                config,
                random,
                particleCount,
                x,
                y,
                vx,
                vy
        );

        List<SyntheticParticle> particles =
                new ArrayList<>();

        for (int frame = 1; frame <= config.frames; frame++) {
            MotionSegment segment =
                    scenario.getMotionSegmentForFrame(frame);

            List<VisibilityEvent> activeVisibilityEvents =
                    scenario.getVisibilityEventsForFrame(frame);

            for (int p = 0; p < particleCount; p++) {
                VisibilityDecision visibilityDecision =
                        determineVisibility(
                                activeVisibilityEvents,
                                visibilityRandom,
                                photobleached,
                                p
                        );

                particles.add(
                        new SyntheticParticle(
                                p + 1,
                                frame,
                                x[p],
                                y[p],
                                config.amplitude,
                                visibilityDecision.visible,
                                visibilityDecision.reason,
                                segment.getMotionMode(),
                                segment.getDiffusionCoefficientUm2PerSecond(),
                                segment.getDriftVelocityXUmPerSecond(),
                                segment.getDriftVelocityYUmPerSecond(),
                                segment.getConfinementRadiusPixel(),
                                segment.getLabel()
                        )
                );

                updatePosition(
                        config,
                        segment,
                        random,
                        x,
                        y,
                        vx,
                        vy,
                        p
                );
            }
        }

        return new SyntheticDataset(particles);
    }

    private void initializeParticles(
            SimulationScenario scenario,
            SimulationConfig config,
            Random random,
            int particleCount,
            double[] x,
            double[] y,
            double[] vx,
            double[] vy
    ) {
        MotionSegment firstSegment =
                scenario.getMotionSegmentForFrame(1);

        for (int i = 0; i < particleCount; i++) {
            if (firstSegment.getMotionMode() == MotionMode.CONFINED_BROWNIAN) {
                initializeInsideCircle(
                        config,
                        random,
                        x,
                        y,
                        i,
                        firstSegment.getConfinementRadiusPixel()
                );
            } else {
                x[i] =
                        config.margin
                                + random.nextDouble()
                                * (config.width - 2.0 * config.margin);

                y[i] =
                        config.margin
                                + random.nextDouble()
                                * (config.height - 2.0 * config.margin);
            }

            vx[i] =
                    -config.maxInitialSpeed
                            + random.nextDouble()
                            * 2.0
                            * config.maxInitialSpeed;

            vy[i] =
                    -config.maxInitialSpeed
                            + random.nextDouble()
                            * 2.0
                            * config.maxInitialSpeed;
        }
    }

    private VisibilityDecision determineVisibility(
            List<VisibilityEvent> activeEvents,
            Random visibilityRandom,
            boolean[] photobleached,
            int particleIndex
    ) {
        if (photobleached[particleIndex]) {
            return new VisibilityDecision(
                    false,
                    "PHOTOBLEACHED"
            );
        }

        for (VisibilityEvent event : activeEvents) {
            if (event.getType() == VisibilityEventType.PHOTOBLEACHING) {
                if (visibilityRandom.nextDouble()
                        < event.getProbabilityPerFrame()) {

                    photobleached[particleIndex] =
                            true;

                    return new VisibilityDecision(
                            false,
                            "PHOTOBLEACHING"
                    );
                }
            }
        }

        for (VisibilityEvent event : activeEvents) {
            if (event.getType() == VisibilityEventType.FORCED_INVISIBLE) {
                return new VisibilityDecision(
                        false,
                        "FORCED_INVISIBLE"
                );
            }
        }

        for (VisibilityEvent event : activeEvents) {
            if (event.getType() == VisibilityEventType.BLINKING) {
                if (visibilityRandom.nextDouble()
                        < event.getProbabilityPerFrame()) {

                    return new VisibilityDecision(
                            false,
                            "BLINKING"
                    );
                }
            }
        }

        return new VisibilityDecision(
                true,
                "NONE"
        );
    }

    private static class VisibilityDecision {

        private final boolean visible;

        private final String reason;

        private VisibilityDecision(
                boolean visible,
                String reason
        ) {
            this.visible = visible;
            this.reason = reason;
        }
    }

    private void updatePosition(
            SimulationConfig config,
            MotionSegment segment,
            Random random,
            double[] x,
            double[] y,
            double[] vx,
            double[] vy,
            int p
    ) {
        MotionMode mode =
                segment.getMotionMode();

        if (mode == MotionMode.CONSTANT_VELOCITY) {
            updateConstantVelocity(
                    config,
                    x,
                    y,
                    vx,
                    vy,
                    p
            );
            return;
        }

        if (mode == MotionMode.FREE_BROWNIAN) {
            updateFreeBrownian(
                    config,
                    segment,
                    random,
                    x,
                    y,
                    p
            );
            return;
        }

        if (mode == MotionMode.CONFINED_BROWNIAN) {
            updateConfinedBrownian(
                    config,
                    segment,
                    random,
                    x,
                    y,
                    p
            );
            return;
        }

        if (mode == MotionMode.DIRECTED_MOTION) {
            updateDirectedMotion(
                    config,
                    segment,
                    x,
                    y,
                    p
            );
            return;
        }

        if (mode == MotionMode.DIRECTED_BROWNIAN) {
            updateDirectedBrownian(
                    config,
                    segment,
                    random,
                    x,
                    y,
                    p
            );
            return;
        }

        if (mode == MotionMode.IMMOBILE) {
            updateImmobile();
            return;
        }

        throw new IllegalArgumentException(
                "Unsupported motion mode: " + mode
        );
    }

    private void updateConstantVelocity(
            SimulationConfig config,
            double[] x,
            double[] y,
            double[] vx,
            double[] vy,
            int p
    ) {
        x[p] += vx[p];
        y[p] += vy[p];

        if (x[p] < config.boundaryMargin
                || x[p] > config.width - config.boundaryMargin) {
            vx[p] = -vx[p];
            x[p] += vx[p];
        }

        if (y[p] < config.boundaryMargin
                || y[p] > config.height - config.boundaryMargin) {
            vy[p] = -vy[p];
            y[p] += vy[p];
        }
    }

    private void updateFreeBrownian(
            SimulationConfig config,
            MotionSegment segment,
            Random random,
            double[] x,
            double[] y,
            int p
    ) {
        double stepSigmaPixel =
                calculateBrownianCoordinateSigmaPixel(
                        config,
                        segment.getDiffusionCoefficientUm2PerSecond()
                );

        x[p] += stepSigmaPixel * random.nextGaussian();
        y[p] += stepSigmaPixel * random.nextGaussian();

        keepInsideImage(config, x, y, p);
    }

    private void updateConfinedBrownian(
            SimulationConfig config,
            MotionSegment segment,
            Random random,
            double[] x,
            double[] y,
            int p
    ) {
        double oldX =
                x[p];

        double oldY =
                y[p];

        double stepSigmaPixel =
                calculateBrownianCoordinateSigmaPixel(
                        config,
                        segment.getDiffusionCoefficientUm2PerSecond()
                );

        double newX =
                oldX + stepSigmaPixel * random.nextGaussian();

        double newY =
                oldY + stepSigmaPixel * random.nextGaussian();

        if (isInsideConfinement(
                config,
                newX,
                newY,
                segment.getConfinementRadiusPixel()
        )) {
            x[p] = newX;
            y[p] = newY;
        } else {
            x[p] = oldX;
            y[p] = oldY;
        }
    }

    private void updateDirectedMotion(
            SimulationConfig config,
            MotionSegment segment,
            double[] x,
            double[] y,
            int p
    ) {
        double dt =
                config.getFrameIntervalSeconds();

        double dxPixel =
                segment.getDriftVelocityXUmPerSecond()
                        * dt
                        / config.pixelSizeUm;

        double dyPixel =
                segment.getDriftVelocityYUmPerSecond()
                        * dt
                        / config.pixelSizeUm;

        x[p] += dxPixel;
        y[p] += dyPixel;

        keepInsideImage(config, x, y, p);
    }

    private void updateDirectedBrownian(
            SimulationConfig config,
            MotionSegment segment,
            Random random,
            double[] x,
            double[] y,
            int p
    ) {
        double dt =
                config.getFrameIntervalSeconds();

        double dxPixel =
                segment.getDriftVelocityXUmPerSecond()
                        * dt
                        / config.pixelSizeUm;

        double dyPixel =
                segment.getDriftVelocityYUmPerSecond()
                        * dt
                        / config.pixelSizeUm;

        double stepSigmaPixel =
                calculateBrownianCoordinateSigmaPixel(
                        config,
                        segment.getDiffusionCoefficientUm2PerSecond()
                );

        x[p] += dxPixel
                + stepSigmaPixel * random.nextGaussian();

        y[p] += dyPixel
                + stepSigmaPixel * random.nextGaussian();

        keepInsideImage(config, x, y, p);
    }

    private void updateImmobile() {
        // No position update.
        // This represents a trapped, bound, or immobile state.
    }

    private double calculateBrownianCoordinateSigmaPixel(
            SimulationConfig config,
            double diffusionCoefficientUm2PerSecond
    ) {
        double dt =
                config.getFrameIntervalSeconds();

        double sigmaUm =
                Math.sqrt(
                        2.0
                                * diffusionCoefficientUm2PerSecond
                                * dt
                );

        return sigmaUm / config.pixelSizeUm;
    }

    private void keepInsideImage(
            SimulationConfig config,
            double[] x,
            double[] y,
            int p
    ) {
        if (x[p] < config.boundaryMargin) {
            x[p] = config.boundaryMargin;
        }

        if (x[p] > config.width - config.boundaryMargin) {
            x[p] = config.width - config.boundaryMargin;
        }

        if (y[p] < config.boundaryMargin) {
            y[p] = config.boundaryMargin;
        }

        if (y[p] > config.height - config.boundaryMargin) {
            y[p] = config.height - config.boundaryMargin;
        }
    }

    private void initializeInsideCircle(
            SimulationConfig config,
            Random random,
            double[] x,
            double[] y,
            int index,
            double radiusPixel
    ) {
        double centerX =
                config.width / 2.0;

        double centerY =
                config.height / 2.0;

        while (true) {
            double candidateX =
                    centerX
                            - radiusPixel
                            + random.nextDouble()
                            * 2.0
                            * radiusPixel;

            double candidateY =
                    centerY
                            - radiusPixel
                            + random.nextDouble()
                            * 2.0
                            * radiusPixel;

            if (isInsideConfinement(
                    config,
                    candidateX,
                    candidateY,
                    radiusPixel
            )) {
                x[index] = candidateX;
                y[index] = candidateY;
                return;
            }
        }
    }

    private boolean isInsideConfinement(
            SimulationConfig config,
            double x,
            double y,
            double radiusPixel
    ) {
        double centerX =
                config.width / 2.0;

        double centerY =
                config.height / 2.0;

        double dx =
                x - centerX;

        double dy =
                y - centerY;

        return dx * dx + dy * dy
                <= radiusPixel * radiusPixel;
    }
}