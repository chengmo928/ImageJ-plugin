package io.github.zhengfangfang0304.particletracking.tracking;

public record TrackingParameters(
        double maximumLinkingDistance,
        int maximumFrameGap
) {
    public TrackingParameters {
        if (maximumLinkingDistance <= 0) {
            throw new IllegalArgumentException(
                    "最大连接距离必须大于0。"
            );
        }

        if (maximumFrameGap < 0) {
            throw new IllegalArgumentException(
                    "最大间隔帧数不能小于0。"
            );
        }
    }// 关闭 TrackingParameters 构造器
}// 关闭 TrackingParameters record
