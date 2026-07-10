package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 标准自由空间布朗运动数据集预设。
 *
 * 用于生成符合 Einstein 扩散模型的 benchmark 数据集：
 *
 * Δx ~ N(0, 2DΔt)
 * Δy ~ N(0, 2DΔt)
 * MSD(τ) = 4Dτ
 */
public final class StandardBrownianBatchPreset {

    private StandardBrownianBatchPreset() {
    }

    public static DatasetBatchConfig createDefault() {
        DatasetBatchConfig batchConfig =
                DatasetBatchConfig.defaultConfig();

        batchConfig.datasetName =
                "standard_brownian_einstein_dataset";

        //三个FOV视野
        batchConfig.fovCountPerExperiment =
                3;

        SimulationConfig baseConfig =
                SimulationConfig.defaultConfig();

        baseConfig.width = 256;
        baseConfig.height = 256;
        baseConfig.frames = 100;
        baseConfig.pixelSizeUm = 0.1;//像素物理尺寸 0.1 μm/像素
        baseConfig.useDensity = true;// 开启「按粒子密度自动计算粒子总数」模式，不手动填粒子数
        baseConfig.motionMode = MotionMode.FREE_BROWNIAN;
        baseConfig.psfSigma = 2.0;// 荧光光斑PSF高斯核σ
        baseConfig.amplitude = 180.0;// 单分子荧光亮度幅值
        baseConfig.background = 20.0;
        baseConfig.randomSeed = 12345L;
        //以上参数全局固定不变

        baseConfig.title = "Standard Brownian Motion Dataset";

        batchConfig.baseConfig = baseConfig;

        batchConfig.motionModeList = new ArrayList<>(Arrays.asList(MotionMode.FREE_BROWNIAN));

        //扩散系数4
        batchConfig.diffusionCoefficientList = new ArrayList<>(Arrays.asList(0.01,0.05,0.10,0.50));

        //粒子密度3
        batchConfig.densityList = new ArrayList<>(Arrays.asList(0.01,0.05,0.10));

        //帧率2
        batchConfig.frameRateList = new ArrayList<>(Arrays.asList(10.0,30.0));

        //噪声强度3
        batchConfig.noiseSigmaList = new ArrayList<>(Arrays.asList(4.0,8.0,16.0));

        return batchConfig;
    }
}

