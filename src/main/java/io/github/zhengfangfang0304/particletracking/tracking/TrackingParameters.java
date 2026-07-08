package io.github.zhengfangfang0304.particletracking.tracking;

/**
 * 追踪算法需要的参数。
 *
 * @param maximumLinkingDistance 最大连接距离，单位为像素
 * @param maximumFrameGap        最大允许间隔帧数
 */
public record TrackingParameters(
        double maximumLinkingDistance,
        int maximumFrameGap
) {

    /**
     * 检查追踪参数是否合法。
     */
    public TrackingParameters {

        if (!Double.isFinite(maximumLinkingDistance)
                || maximumLinkingDistance <= 0.0) {

            throw new IllegalArgumentException(
                    "最大连接距离必须是大于0的有限数字。"
            );
        }

        if (maximumFrameGap < 0) {
            throw new IllegalArgumentException(
                    "最大间隔帧数不能小于0。"
            );
        }
    }
}