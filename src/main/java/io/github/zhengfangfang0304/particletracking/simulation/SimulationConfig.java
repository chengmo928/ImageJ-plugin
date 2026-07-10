package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * 模拟数据生成参数。
 *
 * 这个类只保存参数，不负责生成轨迹，也不负责渲染图像。
 */
public class SimulationConfig {

    public int width = 256;
    public int height = 256;
    public int frames = 30;

    public int particleCount = 8;

    /**
     * 是否根据粒子密度自动计算粒子数量。
     */
    public boolean useDensity = false;

    /**
     * 粒子密度，单位：particles / μm²。
     */
    public double particleDensityPerUm2 = 0.5;

    /**
     * 像素尺寸，单位：μm / pixel。
     * 例如：0.1 表示一个像素对应 0.1 μm。
     */
    public double pixelSizeUm = 0.1;

    /**
     * 帧率，单位：frames / second。
     */
    public double frameRateFps = 10.0;

    /**
     * 扩散系数，单位：μm² / second。
     */
    public double diffusionCoefficientUm2PerSecond = 0.05;

    /**
     * 粒子运动模式。
     */
    public MotionMode motionMode = MotionMode.FREE_BROWNIAN;

    /**
     * 点扩散函数标准差，用来模拟显微镜成像光斑大小。
     * 单位：pixel。
     */
    public double psfSigma = 2.0;

    /**
     * 粒子荧光信号亮度幅值。
     */
    public double amplitude = 180.0;

    /**
     * 背景强度。
     */
    public double background = 20.0;

    /**
     * 高斯噪声强度，模拟相机拍摄噪声。
     * 单位：灰度值。
     */
    public double noiseSigma = 8.0;

    /**
     * 粒子初始放置边距。
     *
     * 粒子诞生位置会强制距离图片四条边缘至少 margin 像素。
     * 只在粒子第一次随机生成坐标时生效。
     */
    public double margin = 30.0;

    /**
     * 粒子运动边界。
     *
     * 粒子运动时不能超过距离图片边缘 boundaryMargin 像素的范围。
     * 只在粒子运动时生效。
     */
    public double boundaryMargin = 10.0;

    /**
     * 匀速运动模式下的最大初始速度。
     *
     * 单位：pixel / frame。
     */
    public double maxInitialSpeed = 1.5;

    /**
     * 受限运动区域半径。
     *
     * 单位：pixel。
     * 第一版先用一个圆形区域表示受限空间。
     */
    public double confinementRadius = 80.0;

    /**
     * 随机种子。
     */
    public long randomSeed = 12345L;

    /**
     * 生成图像标题。
     */
    public String title = "Synthetic Single Particle Movie";

    public static SimulationConfig defaultConfig() {
        return new SimulationConfig();
    }

    /**
     * 获取相邻两帧之间的时间间隔。
     *
     * 单位：second。
     */
    public double getFrameIntervalSeconds() {
        return 1.0 / frameRateFps;
    }

    /**
     * 根据当前模式得到实际使用的粒子数量。
     *
     * 如果 useDensity = false，则使用手动输入的 particleCount。
     * 如果 useDensity = true，则根据粒子密度和视野面积自动计算粒子数。
     */
    public int getResolvedParticleCount() {
        if (!useDensity) {
            return particleCount;
        }

        double areaUm2 = getFieldAreaUm2();

        int calculatedParticleCount =
                (int) Math.round(
                        particleDensityPerUm2 * areaUm2
                );

        return Math.max(1, calculatedParticleCount);
    }

    /**
     * 当前视野面积。
     *
     * 单位：μm²。
     */
    public double getFieldAreaUm2() {
        double widthUm =
                width * pixelSizeUm;

        double heightUm =
                height * pixelSizeUm;

        return widthUm * heightUm;
    }

    /**
     * 实际粒子密度。
     *
     * 单位：particles / μm²。
     */
    public double getActualParticleDensityPerUm2() {
        return getResolvedParticleCount()
                / getFieldAreaUm2();
    }

    /**
     * FidlTrack / 扩散模拟中常用的特征长度。
     *
     * l = sqrt(D / f)
     *
     * D：扩散系数，单位 μm²/s。
     * f：帧率，单位 frame/s。
     * l：单位 μm。
     */
    public double getCharacteristicLengthUm() {
        return Math.sqrt(
                diffusionCoefficientUm2PerSecond
                        / frameRateFps
        );
    }

    /**
     * 布朗运动中每个坐标方向的单帧位移标准差。
     *
     * Δx ~ N(0, 2DΔt)
     *
     * 返回单位：pixel / frame。
     */
    public double getBrownianCoordinateSigmaPixel() {
        double dt =
                getFrameIntervalSeconds();

        double sigmaUm =
                Math.sqrt(
                        2.0
                                * diffusionCoefficientUm2PerSecond
                                * dt
                );

        return sigmaUm / pixelSizeUm;
    }
    /* 加理论 MSD 方法,为了后面验证你的数据是不是标准布朗运动。理论 MSD。
     * 对二维自由布朗运动：MSD(τ) = 4Dτ
     * 返回单位：μm²。
     * @param lagFrames 延迟帧数
     */
    public double getTheoreticalMsdUm2(int lagFrames) {
        double tauSeconds =
                lagFrames * getFrameIntervalSeconds();

        return 4.0
                * diffusionCoefficientUm2PerSecond
                * tauSeconds;
    }

    /* 理论 MSD。
     * 返回单位：pixel²。
     * @param lagFrames 延迟帧数
     */
    public double getTheoreticalMsdPixel2(int lagFrames) {
        double msdUm2 =
                getTheoreticalMsdUm2(lagFrames);

        return msdUm2
                / (pixelSizeUm * pixelSizeUm);
    }
}