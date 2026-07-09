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
     * 像素尺寸，单位：μm / pixel，像素物理尺寸，一个像素=0.1 μm。
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

    public MotionMode motionMode = MotionMode.FREE_BROWNIAN;

    //点扩散函数标准差，用来模拟显微镜成像光斑大小
    public double psfSigma = 2.0;
    //粒子荧光信号亮度幅值
    public double amplitude = 180.0;
    public double background = 20.0;
    //高斯噪声强度，模拟相机拍摄噪声，单位：灰度值
    public double noiseSigma = 8.0;

    //粒子初始放置边距，粒子诞生位置会强制图片四条边缘至少留出margin像素，只在粒子第一次随机生成坐标时生效。
    public double margin = 30.0;
    //粒子运动边界，粒子运动时不能超过图片边缘margin像素的范围，只在粒子运动时生效。
    public double boundaryMargin = 10.0;
    //每一帧画面，粒子单次位移的最大距离不超过 1.5 个像素（匀速运动模式专属，每帧移动像素上限）
    public double maxInitialSpeed = 1.5;

    /**
     * 受限运动区域半径，单位：pixel。
     * 第一版先用一个圆形区域表示受限空间。
     */
    public double confinementRadius = 80.0;

    public long randomSeed = 12345L;

    public String title = "Synthetic Single Particle Movie";

    public static SimulationConfig defaultConfig() {
        return new SimulationConfig();
    }

    //计算帧间隔时间
    public double getFrameIntervalSeconds() {
        return 1.0 / frameRateFps;
    }

    // 如果不开密度模式，直接返回固定粒子数
    public int getResolvedParticleCount() {
        if (!useDensity) {
            return particleCount;
        }

         // 把像素宽高换算成物理微米尺寸
        double widthUm = width * pixelSizeUm;
        double heightUm = height * pixelSizeUm;
        double areaUm2 = widthUm * heightUm;

        //密度 × 总面积 = 总粒子数，四舍五入取整
        int calculatedParticles =
                (int) Math.round(particleDensityPerUm2 * areaUm2);

        // 最少不能少于1个粒子，防止算出来0或负数
        return Math.max(1, calculatedParticles);
    }
}