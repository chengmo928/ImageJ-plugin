package io.github.zhengfangfang0304.particletracking.simulation;

import ij.ImagePlus;

import java.io.File;
import java.io.IOException;

/**
 * 批量模拟数据集生成器。
 *
 * 负责遍历 DatasetBatchConfig 中的参数列表，
 * 自动生成多个 Experiment 和多个 FOV。
 */
public class DatasetBatchGenerator {

    public GenerationSummary generate(
            DatasetBatchConfig batchConfig,
            File outputParentDirectory
    ) throws IOException {

        if (batchConfig == null) {
            throw new IllegalArgumentException("DatasetBatchConfig 不能为 null。");
        }

        File datasetRoot =
                DatasetFolderWriter.createDatasetRoot(
                        outputParentDirectory,
                        batchConfig.datasetName
                );

        int experimentIndex = 1;
        int generatedExperimentCount = 0;
        int generatedFovCount = 0;

        for (MotionMode motionMode : batchConfig.motionModeList) {
            for (double diffusionCoefficient : batchConfig.diffusionCoefficientList) {
                for (double density : batchConfig.densityList) {
                    for (double frameRate : batchConfig.frameRateList) {
                        for (double noiseSigma : batchConfig.noiseSigmaList) {

                            SimulationConfig experimentConfig =
                                    copySimulationConfig(
                                            batchConfig.baseConfig
                                    );

                            experimentConfig.motionMode =
                                    motionMode;

                            experimentConfig.diffusionCoefficientUm2PerSecond =
                                    diffusionCoefficient;

                            experimentConfig.useDensity =
                                    true;

                            experimentConfig.particleDensityPerUm2 =
                                    density;

                            experimentConfig.frameRateFps =
                                    frameRate;

                            experimentConfig.noiseSigma =
                                    noiseSigma;

                            experimentConfig.title =
                                    "Synthetic "
                                            + motionMode.name()
                                            + " EXP "
                                            + experimentIndex;

                            File experimentDirectory =
                                    DatasetFolderWriter.createExperimentDirectory(
                                            datasetRoot,
                                            experimentIndex,
                                            experimentConfig
                                    );

                            DatasetFolderWriter.writeExperimentConfig(
                                    experimentDirectory,
                                    experimentIndex,
                                    experimentConfig,
                                    batchConfig
                            );

                            for (int fovIndex = 1;
                                 fovIndex <= batchConfig.fovCountPerExperiment;
                                 fovIndex++) {

                                SimulationConfig fovConfig =
                                        copySimulationConfig(
                                                experimentConfig
                                        );

                                fovConfig.randomSeed =
                                        experimentConfig.randomSeed
                                                + experimentIndex * 100000L
                                                + fovIndex * 1000L;

                                fovConfig.title =
                                        "Synthetic "
                                                + motionMode.name()
                                                + " EXP "
                                                + experimentIndex
                                                + " FOV "
                                                + fovIndex;

                                SyntheticDatasetGenerator generator =
                                        new SyntheticDatasetGenerator();

                                SyntheticDataset dataset =
                                        generator.generate(fovConfig);

                                GaussianSpotRenderer renderer =
                                        new GaussianSpotRenderer();

                                ImagePlus image =
                                        renderer.render(
                                                dataset,
                                                fovConfig
                                        );

                                DatasetFolderWriter.writeFovData(
                                        image,
                                        dataset,
                                        fovConfig,
                                        experimentDirectory,
                                        fovIndex
                                );

                                image.close();

                                generatedFovCount++;
                            }

                            generatedExperimentCount++;
                            experimentIndex++;
                        }
                    }
                }
            }
        }

        return new GenerationSummary(
                datasetRoot,
                generatedExperimentCount,
                generatedFovCount
        );
    }

    private SimulationConfig copySimulationConfig(
            SimulationConfig source
    ) {
        SimulationConfig target =
                SimulationConfig.defaultConfig();

        target.width =
                source.width;

        target.height =
                source.height;

        target.frames =
                source.frames;

        target.particleCount =
                source.particleCount;

        target.useDensity =
                source.useDensity;

        target.particleDensityPerUm2 =
                source.particleDensityPerUm2;

        target.pixelSizeUm =
                source.pixelSizeUm;

        target.frameRateFps =
                source.frameRateFps;

        target.diffusionCoefficientUm2PerSecond =
                source.diffusionCoefficientUm2PerSecond;

        target.motionMode =
                source.motionMode;

        target.psfSigma =
                source.psfSigma;

        target.amplitude =
                source.amplitude;

        target.background =
                source.background;

        target.noiseSigma =
                source.noiseSigma;

        target.margin =
                source.margin;

        target.boundaryMargin =
                source.boundaryMargin;

        target.maxInitialSpeed =
                source.maxInitialSpeed;

        target.confinementRadius =
                source.confinementRadius;

        target.randomSeed =
                source.randomSeed;

        target.title =
                source.title;

        return target;
    }

    /**
     * 批量生成结果摘要。
     */
    public static class GenerationSummary {

        private final File datasetRoot;
        private final int experimentCount;
        private final int fovCount;

        public GenerationSummary(
                File datasetRoot,
                int experimentCount,
                int fovCount
        ) {
            this.datasetRoot = datasetRoot;
            this.experimentCount = experimentCount;
            this.fovCount = fovCount;
        }

        public File getDatasetRoot() {
            return datasetRoot;
        }

        public int getExperimentCount() {
            return experimentCount;
        }

        public int getFovCount() {
            return fovCount;
        }
    }
}