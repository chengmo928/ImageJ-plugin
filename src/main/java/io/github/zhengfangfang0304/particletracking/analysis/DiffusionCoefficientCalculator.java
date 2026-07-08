package io.github.zhengfangfang0304.particletracking.analysis;

import io.github.zhengfangfang0304.particletracking.model.DiffusionCoefficientResult;
import io.github.zhengfangfang0304.particletracking.model.EnsembleMsdResult;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 扩散系数计算器。
 *
 * 当前采用简单线性拟合：
 *
 * MSD = slope × lag + intercept
 *
 * 对二维普通扩散：
 *
 * MSD = 4D × lag
 *
 * 因此：
 *
 * D = slope / 4
 *
 * 当前lag单位是frame，
 * 因此D的单位是 pixel^2 / frame。
 */
public final class DiffusionCoefficientCalculator {

    private DiffusionCoefficientCalculator() {
    }

    /**
     * 从轨迹直接计算扩散系数。
     *
     * @param tracks           轨迹列表
     * @param maximumFitPoints 最多使用前几个MSD点进行拟合
     * @return 扩散系数拟合结果
     */
    public static DiffusionCoefficientResult calculateFromTracks(
            List<Track> tracks,
            int maximumFitPoints
    ) {
        if (tracks == null) {
            throw new IllegalArgumentException(
                    "轨迹列表不能为null。"
            );
        }

        List<EnsembleMsdResult> ensembleResults =
                MsdCalculator.calculateEnsemble(
                        tracks
                );

        return fitFromEnsembleMsd(
                ensembleResults,
                maximumFitPoints
        );
    }

    /**
     * 从Ensemble MSD结果拟合扩散系数。
     *
     * @param ensembleResults  Ensemble MSD结果
     * @param maximumFitPoints 最多使用前几个MSD点
     * @return 扩散系数拟合结果
     */
    public static DiffusionCoefficientResult fitFromEnsembleMsd(
            List<EnsembleMsdResult> ensembleResults,
            int maximumFitPoints
    ) {
        if (ensembleResults == null) {
            throw new IllegalArgumentException(
                    "Ensemble MSD结果不能为null。"
            );
        }

        if (maximumFitPoints < 2) {
            throw new IllegalArgumentException(
                    "拟合点数至少需要为2。"
            );
        }

        if (ensembleResults.size() < 2) {
            throw new IllegalArgumentException(
                    "MSD点数不足，无法拟合扩散系数。"
            );
        }

        List<EnsembleMsdResult> sortedResults =
                new ArrayList<>(
                        ensembleResults
                );

        sortedResults.sort(
                Comparator.comparingInt(
                        EnsembleMsdResult::lagFrames
                )
        );

        int fitPoints =
                Math.min(
                        maximumFitPoints,
                        sortedResults.size()
                );

        double sumX =
                0.0;

        double sumY =
                0.0;

        double sumXY =
                0.0;

        double sumXX =
                0.0;

        for (int i = 0;
             i < fitPoints;
             i++) {

            EnsembleMsdResult result =
                    sortedResults.get(i);

            double x =
                    result.lagFrames();

            double y =
                    result.ensembleMsd();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double n =
                fitPoints;

        double denominator =
                n * sumXX - sumX * sumX;

        if (Math.abs(denominator) < 1.0e-12) {
            throw new IllegalArgumentException(
                    "线性拟合失败：分母接近0。"
            );
        }

        double slope =
                (n * sumXY - sumX * sumY) / denominator;

        double intercept =
                (sumY - slope * sumX) / n;

        double diffusionCoefficient =
                slope / 4.0;

        return new DiffusionCoefficientResult(
                fitPoints,
                slope,
                intercept,
                diffusionCoefficient
        );
    }
}