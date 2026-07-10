package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 批量模拟数据集生成参数。
 *
 * 这个类用于保存一组参数列表。
 * DatasetBatchGenerator 会遍历这些列表，自动生成多个实验条件。
 */
public class DatasetBatchConfig {

    public String datasetName = "synthetic_benchmark_dataset";

    /**
     * 每个实验条件下生成多少个 FOV。
     */
    public int fovCountPerExperiment = 3;

    /**
     * 基础配置。
     * 图像大小、帧数、PSF、背景、随机种子等默认参数从这里读取。
     */
    public SimulationConfig baseConfig = SimulationConfig.defaultConfig();

    /**
     * 要扫描的扩散系数列表，单位 μm²/s。
     */
    public List<Double> diffusionCoefficientList =
            new ArrayList<>(
                    Arrays.asList(
                            0.01,
                            0.05,
                            0.10
                    )
            );

    /**
     * 要扫描的粒子密度列表，单位 particles/μm²。
     */
    public List<Double> densityList =
            new ArrayList<>(
                    Arrays.asList(
                            0.01,
                            0.05,
                            0.10
                    )
            );

    /**
     * 要扫描的帧率列表，单位 fps。
     */
    public List<Double> frameRateList =
            new ArrayList<>(
                    Arrays.asList(
                            10.0,
                            30.0
                    )
            );

    /**
     * 要扫描的噪声 sigma 列表。
     */
    public List<Double> noiseSigmaList =
            new ArrayList<>(
                    Arrays.asList(
                            4.0,
                            8.0,
                            16.0
                    )
            );

    /**
     * 要生成的运动模式。
     */
    public List<MotionMode> motionModeList =
            new ArrayList<>(
                    Arrays.asList(
                            MotionMode.FREE_BROWNIAN,
                            MotionMode.CONFINED_BROWNIAN
                    )
            );

    public static DatasetBatchConfig defaultConfig() {
        return new DatasetBatchConfig();
    }

    public int getTotalExperimentCount() {
        return diffusionCoefficientList.size()
                * densityList.size()
                * frameRateList.size()
                * noiseSigmaList.size()
                * motionModeList.size();
    }

    public int getTotalFovCount() {
        return getTotalExperimentCount() * fovCountPerExperiment;
    }
}
