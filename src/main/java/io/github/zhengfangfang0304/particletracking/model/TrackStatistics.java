package io.github.zhengfangfang0304.particletracking.model;

/**
 * 一条轨迹的统计结果。
 */
public record TrackStatistics(
        int trackId,
        int startFrame,
        int endFrame,
        int numberOfPoints,
        int durationFrames,
        double startX,
        double startY,
        double endX,
        double endY,
        double displacement,
        double pathLength,
        double meanStep,
        double meanSpeed,
        double meanIntensity
) {
}
