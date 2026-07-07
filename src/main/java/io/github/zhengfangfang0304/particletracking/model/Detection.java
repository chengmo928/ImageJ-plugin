package io.github.zhengfangfang0304.particletracking.model;

/**
 * 表示某一帧中的一个颗粒检测结果。
 */
public final class Detection {

    public final int frame;
    public final double x;
    public final double y;
    public final double intensity;

    public Detection(
            int frame,
            double x,
            double y,
            double intensity
    ) {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.intensity = intensity;
    }
}
