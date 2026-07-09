package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * 模拟粒子的运动模式。
 */
public enum MotionMode {

    CONSTANT_VELOCITY("简单匀速运动"),
    FREE_BROWNIAN("自由空间布朗运动"),
    CONFINED_BROWNIAN("受限布朗运动");
//enum枚举，第一部分写
    private final String displayName;

    MotionMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
