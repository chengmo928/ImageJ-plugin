package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 生成模拟粒子的真实轨迹坐标。
 */
public class SyntheticDatasetGenerator {

    public SyntheticDataset generate(SimulationConfig config) {
        Random random = new Random(config.randomSeed);

        //自动区分密度计算数量和固定粒子数量
        int particleCount = config.getResolvedParticleCount();

        double[] x = new double[particleCount];
        double[] y = new double[particleCount];
        double[] vx = new double[particleCount];
        double[] vy = new double[particleCount];
        //vx是X方向初速度，vy是Y方向初速度，都仅匀速模式使用

        initializeParticles(config, random, particleCount, x, y, vx, vy);
        //初始化所有粒子初始位置 + 初始速度
        List<SyntheticParticle> particles = new ArrayList<>();
        //创建集合容器，用来存放每一个粒子在每一帧的位置记录
        for (int frame = 1; frame <= config.frames; frame++) {
            // 遍历当前帧每一个粒子
            for (int p = 0; p < particleCount; p++) {
            // 先把当前帧坐标存入记录
                
                particles.add(
                        new SyntheticParticle(
                                p + 1,// 粒子编号从1开始
                                frame,// 当前帧数
                                x[p],// 当前帧X坐标
                                y[p],// 当前帧Y坐标
                                config.amplitude  // 当前帧荧光亮度
                        )
                );

                // 更新坐标，计算下一帧位置
                updatePosition(config, random, x, y, vx, vy, p);
            }
        }

        return new SyntheticDataset(particles);
    }

    private void initializeParticles(
            SimulationConfig config,
            Random random,
            int particleCount,
            double[] x,
            double[] y,
            double[] vx,
            double[] vy
    ) {
        for (int i = 0; i < particleCount; i++) {
            if (config.motionMode == MotionMode.CONFINED_BROWNIAN) {
                initializeInsideCircle(config, random, x, y, i);
            } else {
                // 普通模式：距离画布四边留出margin边距，随机生成坐标
                x[i] = config.margin
                        + random.nextDouble() * (config.width - 2.0 * config.margin);

                y[i] = config.margin
                        + random.nextDouble() * (config.height - 2.0 * config.margin);
            }

            //公式等价：下限 + 随机数 * 区间总长，速度正负代表方向。
            //该速度仅 CONSTANT_VELOCITY 匀速模式会使用，布朗运动不会读取 vx、vy。
            vx[i] = -config.maxInitialSpeed
                    + random.nextDouble() * 2.0 * config.maxInitialSpeed;

            vy[i] = -config.maxInitialSpeed
                    + random.nextDouble() * 2.0 * config.maxInitialSpeed;
        }
    }

    private void updatePosition(
            SimulationConfig config,
            Random random,
            double[] x,
            double[] y,
            double[] vx,
            double[] vy,
            int p
    ) {
        if (config.motionMode == MotionMode.CONSTANT_VELOCITY) {
            updateConstantVelocity(config, x, y, vx, vy, p);
            return;
        }

        if (config.motionMode == MotionMode.FREE_BROWNIAN) {
            updateFreeBrownian(config, random, x, y, p);
            return;
        }

        if (config.motionMode == MotionMode.CONFINED_BROWNIAN) {
            updateConfinedBrownian(config, random, x, y, p);
        }
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
        }

        if (y[p] < config.boundaryMargin
                || y[p] > config.height - config.boundaryMargin) {
            vy[p] = -vy[p];
        }
    }

    private void updateFreeBrownian(
            SimulationConfig config,
            Random random,
            double[] x,
            double[] y,
            int p
    ) {
        double dt = config.getFrameIntervalSeconds();

        double stepSigmaPixel =
                Math.sqrt(
                        2.0
                                * config.diffusionCoefficientUm2PerSecond
                                * dt
                ) / config.pixelSizeUm;

        x[p] += stepSigmaPixel * random.nextGaussian();
        y[p] += stepSigmaPixel * random.nextGaussian();

        keepInsideImage(config, x, y, p);
    }

    private void updateConfinedBrownian(
            SimulationConfig config,
            Random random,
            double[] x,
            double[] y,
            int p
    ) {
        double oldX = x[p];
        double oldY = y[p];

        double dt = config.getFrameIntervalSeconds();

        double stepSigmaPixel =
                Math.sqrt(
                        2.0
                                * config.diffusionCoefficientUm2PerSecond
                                * dt
                ) / config.pixelSizeUm;

        double newX = oldX + stepSigmaPixel * random.nextGaussian();
        double newY = oldY + stepSigmaPixel * random.nextGaussian();

        if (isInsideConfinement(config, newX, newY)) {
            x[p] = newX;
            y[p] = newY;
        } else {
            x[p] = oldX;
            y[p] = oldY;
        }
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
            int index
    ) {
        double centerX = config.width / 2.0;
        double centerY = config.height / 2.0;

        while (true) {
            double candidateX =
                    centerX
                            - config.confinementRadius
                            + random.nextDouble() * 2.0 * config.confinementRadius;

            double candidateY =
                    centerY
                            - config.confinementRadius
                            + random.nextDouble() * 2.0 * config.confinementRadius;

            // isInsideConfinement () 判断点是否在圆内，圆方程判定
            if (isInsideConfinement(config, candidateX, candidateY)) {
                x[index] = candidateX;
                y[index] = candidateY;
                return;
            }
        }
    }

    private boolean isInsideConfinement(
            SimulationConfig config,
            double x,
            double y
    ) {
        double centerX = config.width / 2.0;
        double centerY = config.height / 2.0;

        double dx = x - centerX;
        double dy = y - centerY;

        return dx * dx + dy * dy
                <= config.confinementRadius * config.confinementRadius;
    }
}