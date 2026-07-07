package io.github.zhengfangfang0304.particletracking.detection;

/**
 * 检测算法需要的参数。
 *
 * @param threshold          识别强度阈值
 * @param localMaximumRadius 局部极大值搜索半径，单位为像素
 * @param minimumDistance    检测点之间允许的最小距离，单位为像素
 */
public record DetectionParameters(
        double threshold,
        int localMaximumRadius,
        double minimumDistance
) {

    /**
     * 检查检测参数是否合法。
     */
    public DetectionParameters {

        if (!Double.isFinite(threshold)
                || threshold <= 0.0) {

            throw new IllegalArgumentException(
                    "识别阈值必须是大于0的有限数字。"
            );
        }

        if (localMaximumRadius < 1) {
            throw new IllegalArgumentException(
                    "局部极大半径必须至少为1。"
            );
        }

        if (!Double.isFinite(minimumDistance)
                || minimumDistance < 1.0) {

            throw new IllegalArgumentException(
                    "最小距离必须是大于或等于1的有限数字。"
            );
        }
    }
}