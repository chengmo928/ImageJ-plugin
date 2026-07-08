package io.github.zhengfangfang0304.particletracking.model;

/**
 * 所有轨迹在某个时间延迟下的总体平均MSD结果。
 *
 * @param lagFrames   时间延迟，单位为帧
 * @param ensembleMsd 总体平均MSD，单位为 pixel^2
 * @param pairCount   用于计算该总体MSD的点对总数量
 */
public record EnsembleMsdResult(
        int lagFrames,
        double ensembleMsd,
        int pairCount
) {
}
