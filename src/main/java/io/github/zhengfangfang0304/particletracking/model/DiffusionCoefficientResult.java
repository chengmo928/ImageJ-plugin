package io.github.zhengfangfang0304.particletracking.model;

/**
 * 扩散系数拟合结果。
 *
 * 当前单位：
 * slope: pixel^2 / frame
 * diffusionCoefficient: pixel^2 / frame
 *
 * 对二维普通扩散：
 * MSD = 4DΔt
 *
 * 如果时间间隔以帧为单位，
 * 则 D = slope / 4。
 */
public record DiffusionCoefficientResult(
        int fitPoints,
        double slope,
        double intercept,
        double diffusionCoefficient
) {
}