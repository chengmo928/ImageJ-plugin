package io.github.zhengfangfang0304.particletracking.simulation;

//导入 Fiji/ImageJ 核心 API，ImagePlus：ImageJ 体系里代表一张图像 / 图像序列栈的对象；
import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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

        if (image == null) {
            throw new IllegalArgumentException("ImagePlus 不能为 null。");
        }

        if (dataset == null) {
            throw new IllegalArgumentException("SyntheticDataset 不能为 null。");
        }

        if (config == null) {
            throw new IllegalArgumentException("SimulationConfig 不能为 null。");
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

        exportGroundTruthDetections(
                dataset,
                new File(
                        outputDirectory,
                        "ground_truth_detections.csv"
                )
        );

        exportConfig(
                config,
                new File(
                        outputDirectory,
                        "simulation_config.json"
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

    private static void exportGroundTruthDetections(
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
