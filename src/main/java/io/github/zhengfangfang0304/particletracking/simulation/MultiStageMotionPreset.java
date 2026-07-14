package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * Preset scenario for testing multi-stage particle motion.
 *
 * Timeline:
 * frames 1-30   : free Brownian motion
 * frames 31-60  : confined Brownian motion
 * frames 61-75  : immobile / trapped state
 * frames 76-100 : directed Brownian motion
 */
public final class MultiStageMotionPreset {

    private MultiStageMotionPreset() {
    }

    public static SimulationScenario createDefaultScenario() {
        SimulationConfig config =
                SimulationConfig.defaultConfig();

        config.width = 256;
        config.height = 256;
        config.frames = 100;

        config.particleCount = 12;
        config.useDensity = false;

        config.pixelSizeUm = 0.1;
        config.frameRateFps = 10.0;

        config.psfSigma = 2.0;
        config.amplitude = 180.0;
        config.background = 20.0;
        config.noiseSigma = 8.0;

        config.margin = 30.0;
        config.boundaryMargin = 10.0;
        config.randomSeed = 12345L;

        config.title = "Multi-stage Motion Test Dataset";

        SimulationScenario scenario =
                new SimulationScenario(config);

        scenario.addMotionSegment(
                MotionSegment.freeBrownian(
                        1,
                        30,
                        0.05
                )
        );

        scenario.addMotionSegment(
                MotionSegment.confinedBrownian(
                        31,
                        60,
                        0.02,
                        80.0
                )
        );

        scenario.addMotionSegment(
                MotionSegment.immobile(
                        61,
                        75
                )
        );

        scenario.addMotionSegment(
                new MotionSegment(
                        76,
                        100,
                        MotionMode.DIRECTED_BROWNIAN,
                        0.03,
                        0.20,
                        0.00,
                        0.0,
                        "directed_brownian"
                )
        );

        //第 1–100 帧：每个粒子每帧有 5% 概率闪烁不可见
        scenario.addVisibilityEvent(
                VisibilityEvent.blinking(
                        1,
                        100,
                        0.05
                )
        );
        //第 70–100 帧：每个粒子每帧有 1% 概率发生光漂白
        scenario.addVisibilityEvent(
                VisibilityEvent.photobleaching(
                        70,
                        100,
                        0.01
                )
        );
        scenario.validate();

        return scenario;
    }
}
