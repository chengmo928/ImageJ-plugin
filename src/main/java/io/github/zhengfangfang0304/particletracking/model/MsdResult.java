package io.github.zhengfangfang0304.particletracking.model;

/**
 * 单条轨迹在某个时间延迟下的MSD结果。
 *
 * @param trackId   轨迹编号
 * @param lagFrames 时间延迟，单位为帧
 * @param msd       均方位移，单位为 pixel^2
 * @param pairCount 用于计算该MSD的点对数量
 */
public record MsdResult(
        int trackId,
        int lagFrames,
        double msd,
        int pairCount
) {
}