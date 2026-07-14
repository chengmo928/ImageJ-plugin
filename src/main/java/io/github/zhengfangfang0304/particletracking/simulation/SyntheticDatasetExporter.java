package io.github.zhengfangfang0304.particletracking.simulation;

//导入 Fiji/ImageJ 核心 API，ImagePlus：ImageJ 体系里代表一张图像 / 图像序列栈的对象；
import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟数据导出器。
 *
 * 负责导出：
 * 1. 模拟图像 tif；
 * 2. ground truth 粒子坐标 csv；
 * 3. simulation config json。
 */
public final class SyntheticDatasetExporter {

    private SyntheticDatasetExporter() {
    }

    public static void exportAll(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            File outputDirectory
    ) throws IOException {

        SimulationScenario scenario =
                SimulationScenario.fromConfig(
                        config
                );

        exportAll(
                image,
                dataset,
                config,
                scenario,
                outputDirectory
        );
    }

    public static void exportAll(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            SimulationScenario scenario,
            File outputDirectory
    ) throws IOException {

        exportAll(
                image,
                dataset,
                config,
                scenario,
                SimulationExportOptions.exportAll(),
                outputDirectory
        );
    }

    public static void exportAll(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            SimulationScenario scenario,
            SimulationExportOptions exportOptions,
            File outputDirectory
    ) throws IOException {

        if (image == null) {
            throw new IllegalArgumentException("ImagePlus 不能为 null。");
        }

        if (dataset == null) {
            throw new IllegalArgumentException("SyntheticDataset 不能为 null。");
        }

        if (config == null) {
            throw new IllegalArgumentException("SimulationConfig 不能为 null。");
        }

        if (scenario == null) {
            throw new IllegalArgumentException("SimulationScenario 不能为 null。");
        }

        if (exportOptions == null) {
                throw new IllegalArgumentException("SimulationExportOptions 不能为 null。");
            }

        if (outputDirectory == null) {
            throw new IllegalArgumentException("输出文件夹不能为 null。");
        }

        if (!outputDirectory.exists()) {
            boolean created =
                    outputDirectory.mkdirs();

            if (!created) {
                throw new IOException(
                        "无法创建输出文件夹："
                                + outputDirectory.getAbsolutePath()
                );
            }
        }

        exportMovie(
                image,
                new File(
                        outputDirectory,
                        "simulation_movie.tif"
                )
        );

        exportGroundTruthDetectionsCsv(
                dataset,
                new File(
                        outputDirectory,
                        "ground_truth_detections.csv"
                )
        );

        exportGroundTruthTracksCsv(
                dataset,
                new File(
                        outputDirectory,
                        "ground_truth_tracks.csv"
                )
        );

        exportVisibilityCsv(
                dataset,
                new File(
                        outputDirectory,
                        "ground_truth_visibility.csv"
                )
        );

        exportVisibilityEventsCsv(
                scenario,
                new File(
                        outputDirectory,
                        "ground_truth_visibility_events.csv"
                )
        );

        exportMotionSegmentsCsv(
                scenario,
                new File(
                        outputDirectory,
                        "ground_truth_motion_segments.csv"
                )
        );

        exportConfig(
                config,
                new File(
                        outputDirectory,
                        "simulation_config.json"
                )
        );

        exportScenarioConfig(
                config,
                scenario,
                new File(
                        outputDirectory,
                        "scenario_config.json"
                )
        );

        exportTheoreticalMsdCsv(
                config,
                new File(
                        outputDirectory,
                        "theoretical_msd.csv"
                )
        );

        exportGroundTruthMsdCsv(
                dataset,
                config,
                new File(
                        outputDirectory,
                        "ground_truth_msd.csv"
                )
        );
    }

    private static void exportMovie(
            ImagePlus image,
            File outputFile
    ) {
        //ImageJ 内置工具方法，直接把图像对象存为 TIFF 文件。
        IJ.saveAsTiff(
                image,
                outputFile.getAbsolutePath()
        );
    }

    private static void exportGroundTruthDetectionsCsv(
            SyntheticDataset dataset,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(outputFile)
                        )
        ) {
            writer.println(
                    "frame,particle_id,x,y,intensity"
            );

            for (SyntheticParticle particle : dataset.getParticles()) {
                if (!particle.isVisible()) {
                    continue;
                }       

                writer.println(
                        particle.getFrame()
                                + ","
                                + particle.getParticleId()
                                + ","
                                + particle.getX()
                                + ","
                                + particle.getY()
                                + ","
                                + particle.getIntensity()
                );
            }
        }
    }

    
    private static void exportGroundTruthTracksCsv(
            SyntheticDataset dataset,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println(
                    "frame,particle_id,x,y,intensity,"
                            + "motion_mode,state_label,"
                            + "D_um2_per_s,"
                            + "vx_um_per_s,vy_um_per_s,"
                            + "confinement_radius_pixel,"
                            + "visible,visibility_reason"
            );

            for (SyntheticParticle particle : dataset.getParticles()) {
                writer.println(
                        particle.getFrame()
                                + ","
                                + particle.getParticleId()
                                + ","
                                + particle.getX()
                                + ","
                                + particle.getY()
                                + ","
                                + particle.getIntensity()
                                + ","
                                + particle.getMotionModeName()
                                + ","
                                + particle.getStateLabel()
                                + ","
                                + particle.getDiffusionCoefficientUm2PerSecond()
                                + ","
                                + particle.getDriftVelocityXUmPerSecond()
                                + ","
                                + particle.getDriftVelocityYUmPerSecond()
                                + ","
                                + particle.getConfinementRadiusPixel()
                                + ","
                                + particle.isVisible()
                                + ","
                                + particle.getVisibilityReason()
                );
            }
        }
    }

    private static void exportVisibilityCsv(
            SyntheticDataset dataset,
            File outputFile
    ) throws IOException {

    try (
            PrintWriter writer =
                    new PrintWriter(
                            new FileWriter(
                                    outputFile
                            )
                    )
    ) {
            writer.println(
                    "frame,particle_id,visible,visibility_reason"
            );

            for (SyntheticParticle particle : dataset.getParticles()) {
            writer.println(
                    particle.getFrame()
                            + ","
                            + particle.getParticleId()
                            + ","
                            + particle.isVisible()
                            + ","
                            + particle.getVisibilityReason()
            );
            }
    }
    }

    private static void exportVisibilityEventsCsv(
            SimulationScenario scenario,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println(
                    "start_frame,end_frame,event_type,probability_per_frame,label"
            );

            for (VisibilityEvent event : scenario.getVisibilityEvents()) {
                writer.println(
                        event.getStartFrame()
                                + ","
                                + event.getEndFrame()
                                + ","
                                + event.getType().name()
                                + ","
                                + event.getProbabilityPerFrame()
                                + ","
                                + event.getLabel()
                );
            }
        }
    }

    private static void exportMotionSegmentsCsv(
            SimulationScenario scenario,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println(
                    "start_frame,end_frame,motion_mode,"
                            + "D_um2_per_s,"
                            + "vx_um_per_s,vy_um_per_s,"
                            + "confinement_radius_pixel,"
                            + "state_label"
            );

            for (MotionSegment segment : scenario.getMotionSegments()) {
                writer.println(
                        segment.getStartFrame()
                                + ","
                                + segment.getEndFrame()
                                + ","
                                + segment.getMotionMode().name()
                                + ","
                                + segment.getDiffusionCoefficientUm2PerSecond()
                                + ","
                                + segment.getDriftVelocityXUmPerSecond()
                                + ","
                                + segment.getDriftVelocityYUmPerSecond()
                                + ","
                                + segment.getConfinementRadiusPixel()
                                + ","
                                + segment.getLabel()
                );
            }
        }
    }

    private static void exportScenarioConfig(
            SimulationConfig config,
            SimulationScenario scenario,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println("{");

            writer.println("  \"base_config\": {");
            writer.println("    \"width\": " + config.width + ",");
            writer.println("    \"height\": " + config.height + ",");
            writer.println("    \"frames\": " + config.frames + ",");
            writer.println("    \"particleCount\": " + config.getResolvedParticleCount() + ",");
            writer.println("    \"useDensity\": " + config.useDensity + ",");
            writer.println("    \"particleDensityPerUm2\": " + config.particleDensityPerUm2 + ",");
            writer.println("    \"pixelSizeUm\": " + config.pixelSizeUm + ",");
            writer.println("    \"frameRateFps\": " + config.frameRateFps + ",");
            writer.println("    \"frameIntervalSeconds\": " + config.getFrameIntervalSeconds() + ",");
            writer.println("    \"diffusionCoefficientUm2PerSecond\": " + config.diffusionCoefficientUm2PerSecond + ",");
            writer.println("    \"motionMode\": \"" + escapeJson(config.motionMode.name()) + "\",");
            writer.println("    \"psfSigma\": " + config.psfSigma + ",");
            writer.println("    \"amplitude\": " + config.amplitude + ",");
            writer.println("    \"background\": " + config.background + ",");
            writer.println("    \"noiseSigma\": " + config.noiseSigma + ",");
            writer.println("    \"confinementRadius\": " + config.confinementRadius + ",");
            writer.println("    \"randomSeed\": " + config.randomSeed);
            writer.println("  },");

            writer.println("  \"motion_segments\": [");

            for (
                    int i = 0;
                    i < scenario.getMotionSegments().size();
                    i++
            ) {
                MotionSegment segment =
                        scenario.getMotionSegments()
                                .get(i);

                writer.println("    {");
                writer.println("      \"startFrame\": " + segment.getStartFrame() + ",");
                writer.println("      \"endFrame\": " + segment.getEndFrame() + ",");
                writer.println("      \"motionMode\": \"" + escapeJson(segment.getMotionMode().name()) + "\",");
                writer.println("      \"diffusionCoefficientUm2PerSecond\": " + segment.getDiffusionCoefficientUm2PerSecond() + ",");
                writer.println("      \"driftVelocityXUmPerSecond\": " + segment.getDriftVelocityXUmPerSecond() + ",");
                writer.println("      \"driftVelocityYUmPerSecond\": " + segment.getDriftVelocityYUmPerSecond() + ",");
                writer.println("      \"confinementRadiusPixel\": " + segment.getConfinementRadiusPixel() + ",");
                writer.println("      \"label\": \"" + escapeJson(segment.getLabel()) + "\"");

                if (i < scenario.getMotionSegments().size() - 1) {
                    writer.println("    },");
                } else {
                    writer.println("    }");
                }
            }

            writer.println("  ],");

            writer.println("  \"visibility_events\": [");

            for (
                    int i = 0;
                    i < scenario.getVisibilityEvents().size();
                    i++
            ) {
                VisibilityEvent event =
                        scenario.getVisibilityEvents()
                                .get(i);

                writer.println("    {");
                writer.println("      \"startFrame\": " + event.getStartFrame() + ",");
                writer.println("      \"endFrame\": " + event.getEndFrame() + ",");
                writer.println("      \"eventType\": \"" + escapeJson(event.getType().name()) + "\",");
                writer.println("      \"probabilityPerFrame\": " + event.getProbabilityPerFrame() + ",");
                writer.println("      \"label\": \"" + escapeJson(event.getLabel()) + "\"");

                if (i < scenario.getVisibilityEvents().size() - 1) {
                    writer.println("    },");
                } else {
                    writer.println("    }");
                }
            }

            writer.println("  ]");

            writer.println("}");
        }
    }

    private static void exportTheoreticalMsdCsv(
            SimulationConfig config,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println(
                    "lag_frames,tau_seconds,theoretical_msd_um2,theoretical_msd_pixel2"
            );

            int maxLagFrames =
                    Math.max(
                            1,
                            config.frames - 1
                    );

            for (
                    int lagFrames = 1;
                    lagFrames <= maxLagFrames;
                    lagFrames++
            ) {
                double tauSeconds =
                        lagFrames
                                * config.getFrameIntervalSeconds();

                double theoreticalMsdUm2 =
                        4.0
                                * config.diffusionCoefficientUm2PerSecond
                                * tauSeconds;

                double theoreticalMsdPixel2 =
                        theoreticalMsdUm2
                                / (
                                        config.pixelSizeUm
                                                * config.pixelSizeUm
                                );

                writer.println(
                        lagFrames
                                + ","
                                + tauSeconds
                                + ","
                                + theoreticalMsdUm2
                                + ","
                                + theoreticalMsdPixel2
                );
            }
        }
    }

    private static void exportGroundTruthMsdCsv(
            SyntheticDataset dataset,
            SimulationConfig config,
            File outputFile
    ) throws IOException {

        Map<Integer, Map<Integer, SyntheticParticle>> particlesByIdAndFrame =
                new HashMap<>();

        for (SyntheticParticle particle : dataset.getParticles()) {
            particlesByIdAndFrame
                    .computeIfAbsent(
                            particle.getParticleId(),
                            id -> new HashMap<>()
                    )
                    .put(
                            particle.getFrame(),
                            particle
                    );
        }

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        outputFile
                                )
                        )
        ) {
            writer.println(
                    "lag_frames,tau_seconds,ground_truth_msd_pixel2,ground_truth_msd_um2,pair_count"
            );

            int maxLagFrames =
                    Math.max(
                            1,
                            config.frames - 1
                    );

            for (
                    int lagFrames = 1;
                    lagFrames <= maxLagFrames;
                    lagFrames++
            ) {
                double sumSquaredDisplacementPixel2 =
                        0.0;

                int pairCount =
                        0;

                for (
                        Map<Integer, SyntheticParticle> particleTrack
                        : particlesByIdAndFrame.values()
                ) {
                    for (
                            int frame = 1;
                            frame <= config.frames - lagFrames;
                            frame++
                    ) {
                        SyntheticParticle first =
                                particleTrack.get(
                                        frame
                                );

                        SyntheticParticle second =
                                particleTrack.get(
                                        frame + lagFrames
                                );

                        if (first == null || second == null) {
                            continue;
                        }

                        double dx =
                                second.getX()
                                        - first.getX();

                        double dy =
                                second.getY()
                                        - first.getY();

                        double squaredDisplacementPixel2 =
                                dx * dx
                                        + dy * dy;

                        sumSquaredDisplacementPixel2 +=
                                squaredDisplacementPixel2;

                        pairCount++;
                    }
                }

                double tauSeconds =
                        lagFrames
                                * config.getFrameIntervalSeconds();

                double groundTruthMsdPixel2 =
                        pairCount > 0
                                ? sumSquaredDisplacementPixel2 / pairCount
                                : Double.NaN;

                double groundTruthMsdUm2 =
                        groundTruthMsdPixel2
                                * config.pixelSizeUm
                                * config.pixelSizeUm;

                writer.println(
                        lagFrames
                                + ","
                                + tauSeconds
                                + ","
                                + groundTruthMsdPixel2
                                + ","
                                + groundTruthMsdUm2
                                + ","
                                + pairCount
                );
            }
        }
    }

    private static String escapeJson(
            String text
    ) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static void exportConfig(
            SimulationConfig config,
            File outputFile
    ) throws IOException {

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(outputFile)
                        )
        ) {
            writer.println("{");
            writer.println("  \"width\": " + config.width + ",");
            writer.println("  \"height\": " + config.height + ",");
            writer.println("  \"frames\": " + config.frames + ",");
            writer.println("  \"particleCount\": " + config.getResolvedParticleCount() + ",");
            writer.println("  \"useDensity\": " + config.useDensity + ",");
            writer.println("  \"particleDensityPerUm2\": " + config.particleDensityPerUm2 + ",");
            writer.println("  \"pixelSizeUm\": " + config.pixelSizeUm + ",");
            writer.println("  \"frameRateFps\": " + config.frameRateFps + ",");
            writer.println("  \"frameIntervalSeconds\": " + config.getFrameIntervalSeconds() + ",");
            writer.println("  \"diffusionCoefficientUm2PerSecond\": " + config.diffusionCoefficientUm2PerSecond + ",");
            writer.println("  \"motionMode\": \"" + config.motionMode + "\",");
            writer.println("  \"psfSigma\": " + config.psfSigma + ",");
            writer.println("  \"amplitude\": " + config.amplitude + ",");
            writer.println("  \"background\": " + config.background + ",");
            writer.println("  \"noiseSigma\": " + config.noiseSigma + ",");
            writer.println("  \"confinementRadius\": " + config.confinementRadius + ",");
            writer.println("  \"randomSeed\": " + config.randomSeed);
            writer.println("}");
        }
    }
}
