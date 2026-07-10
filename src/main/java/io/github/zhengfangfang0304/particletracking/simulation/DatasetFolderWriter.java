package io.github.zhengfangfang0304.particletracking.simulation;

import ij.ImagePlus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 数据集文件夹写入器。
 *
 * 负责按照 EXP/FOV 层级保存批量生成的数据。
 */
public final class DatasetFolderWriter {

    private DatasetFolderWriter() {
    }

    public static File createDatasetRoot(
            File outputParentDirectory,
            String datasetName
    ) throws IOException {

        if (outputParentDirectory == null) {
            throw new IllegalArgumentException("输出父文件夹不能为 null。");
        }

        if (datasetName == null || datasetName.isBlank()) {
            throw new IllegalArgumentException("数据集名称不能为空。");
        }

        File datasetRoot =
                new File(
                        outputParentDirectory,
                        sanitizeFileName(datasetName)
                );

        if (!datasetRoot.exists()) {
            boolean created =
                    datasetRoot.mkdirs();

            if (!created) {
                throw new IOException(
                        "无法创建数据集文件夹："
                                + datasetRoot.getAbsolutePath()
                );
            }
        }

        return datasetRoot;
    }

    public static File createExperimentDirectory(
            File datasetRoot,
            int experimentIndex,
            SimulationConfig config
    ) throws IOException {

        String folderName =
                String.format(
                        "EXP_%03d_%s_D%.4f_den%.4f_fps%.1f_noise%.1f",
                        experimentIndex,
                        config.motionMode.name(),
                        config.diffusionCoefficientUm2PerSecond,
                        config.getActualParticleDensityPerUm2(),
                        config.frameRateFps,
                        config.noiseSigma
                );

        File experimentDirectory =
                new File(
                        datasetRoot,
                        sanitizeFileName(folderName)
                );

        if (!experimentDirectory.exists()) {
            boolean created =
                    experimentDirectory.mkdirs();

            if (!created) {
                throw new IOException(
                        "无法创建实验文件夹："
                                + experimentDirectory.getAbsolutePath()
                );
            }
        }

        return experimentDirectory;
    }

    public static File createFovDirectory(
            File experimentDirectory,
            int fovIndex
    ) throws IOException {

        File fovDirectory =
                new File(
                        experimentDirectory,
                        String.format(
                                "FOV_%03d",
                                fovIndex
                        )
                );

        if (!fovDirectory.exists()) {
            boolean created =
                    fovDirectory.mkdirs();

            if (!created) {
                throw new IOException(
                        "无法创建FOV文件夹："
                                + fovDirectory.getAbsolutePath()
                );
            }
        }

        return fovDirectory;
    }

    public static void writeFovData(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            File experimentDirectory,
            int fovIndex
    ) throws IOException {

        File fovDirectory =
                createFovDirectory(
                        experimentDirectory,
                        fovIndex
                );

        SyntheticDatasetExporter.exportAll(
                image,
                dataset,
                config,
                fovDirectory
        );
    }

    public static void writeExperimentConfig(
            File experimentDirectory,
            int experimentIndex,
            SimulationConfig config,
            DatasetBatchConfig batchConfig
    ) throws IOException {

        File outputFile =
                new File(
                        experimentDirectory,
                        "experiment_config.json"
                );

        try (
                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(outputFile)
                        )
        ) {
            writer.println("{");
            writer.println("  \"experimentIndex\": " + experimentIndex + ",");
            writer.println("  \"motionMode\": \"" + config.motionMode.name() + "\",");
            writer.println("  \"motionModeDisplayName\": \"" + config.motionMode + "\",");
            writer.println("  \"diffusionCoefficientUm2PerSecond\": "
                    + config.diffusionCoefficientUm2PerSecond + ",");
            writer.println("  \"frameRateFps\": " + config.frameRateFps + ",");
            writer.println("  \"frameIntervalSeconds\": "
                    + config.getFrameIntervalSeconds() + ",");
            writer.println("  \"characteristicLengthUm\": "
                    + config.getCharacteristicLengthUm() + ",");
            writer.println("  \"pixelSizeUm\": " + config.pixelSizeUm + ",");
            writer.println("  \"width\": " + config.width + ",");
            writer.println("  \"height\": " + config.height + ",");
            writer.println("  \"frames\": " + config.frames + ",");
            writer.println("  \"useDensity\": " + config.useDensity + ",");
            writer.println("  \"particleDensityPerUm2\": "
                    + config.particleDensityPerUm2 + ",");
            writer.println("  \"resolvedParticleCount\": "
                    + config.getResolvedParticleCount() + ",");
            writer.println("  \"actualParticleDensityPerUm2\": "
                    + config.getActualParticleDensityPerUm2() + ",");
            writer.println("  \"noiseSigma\": " + config.noiseSigma + ",");
            writer.println("  \"fovCountPerExperiment\": "
                    + batchConfig.fovCountPerExperiment);
            writer.println("}");
        }
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll(
                "[\\\\/:*?\"<>|]",
                "_"
        );
    }
}
