package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * 模拟数据中的真实粒子点。
 *
 * frame 使用 1-based 编号，与 ImageJ 的 slice 编号保持一致。
 */
public class SyntheticParticle {

    private final int particleId;
    private final int frame;
    private final double x;
    private final double y;
    private final double intensity;

    public SyntheticParticle(
            int particleId,
            int frame,
            double x,
            double y,
            double intensity
    ) {
        this.particleId = particleId;
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.intensity = intensity;
    }

    public int getParticleId() {
        return particleId;
    }

    public int getFrame() {
        return frame;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getIntensity() {
        return intensity;
    }
}