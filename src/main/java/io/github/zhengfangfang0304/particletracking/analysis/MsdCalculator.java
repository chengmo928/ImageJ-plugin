package io.github.zhengfangfang0304.particletracking.analysis;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.EnsembleMsdResult;
import io.github.zhengfangfang0304.particletracking.model.MsdResult;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MSD计算器。
 *
 * MSD = mean(Δx^2 + Δy^2)
 *
 * 这里的lagFrames表示时间延迟，单位是帧。
 */
public final class MsdCalculator {

    private MsdCalculator() {
    }

    /**
     * 计算每条轨迹在不同lag下的MSD。
     *
     * @param tracks 轨迹列表
     * @return 每条轨迹的MSD结果
     */
    public static List<MsdResult> calculatePerTrack(
            List<Track> tracks
    ) {
        if (tracks == null) {
            throw new IllegalArgumentException(
                    "轨迹列表不能为null。"
            );
        }

        List<MsdResult> results =
                new ArrayList<>();

        for (Track track : tracks) {

            if (track == null
                    || track.detections == null
                    || track.detections.size() < 2) {
                continue;
            }

            List<Detection> points =
                    new ArrayList<>(
                            track.detections
                    );

            points.sort(
                    Comparator.comparingInt(
                            detection -> detection.frame
                    )
            );

            int maximumLag =
                    points.size() - 1;

            for (int lag = 1;
                 lag <= maximumLag;
                 lag++) {

                double sumSquaredDisplacement =
                        0.0;

                int pairCount =
                        0;

                for (int index = 0;
                     index < points.size() - lag;
                     index++) {

                    Detection first =
                            points.get(index);

                    Detection second =
                            points.get(index + lag);

                    double dx =
                            second.x - first.x;

                    double dy =
                            second.y - first.y;

                    double squaredDisplacement =
                            dx * dx + dy * dy;

                    sumSquaredDisplacement +=
                            squaredDisplacement;

                    pairCount++;
                }

                if (pairCount > 0) {

                    double msd =
                            sumSquaredDisplacement / pairCount;

                    results.add(
                            new MsdResult(
                                    track.id,
                                    lag,
                                    msd,
                                    pairCount
                            )
                    );
                }
            }
        }

        return results;
    }

    /**
     * 计算所有轨迹的总体平均MSD。
     *
     * 注意：
     * 总体平均MSD需要按照点对数量加权，
     * 不能简单对每条轨迹的MSD取平均。
     *
     * @param tracks 轨迹列表
     * @return 总体平均MSD结果
     */
    public static List<EnsembleMsdResult> calculateEnsemble(
            List<Track> tracks
    ) {
        List<MsdResult> perTrackResults =
                calculatePerTrack(tracks);

        Map<Integer, Double> weightedSumByLag =
                new HashMap<>();

        Map<Integer, Integer> pairCountByLag =
                new HashMap<>();

        for (MsdResult result : perTrackResults) {

            int lag =
                    result.lagFrames();

            double weightedSum =
                    result.msd() * result.pairCount();

            weightedSumByLag.put(
                    lag,
                    weightedSumByLag.getOrDefault(
                            lag,
                            0.0
                    ) + weightedSum
            );

            pairCountByLag.put(
                    lag,
                    pairCountByLag.getOrDefault(
                            lag,
                            0
                    ) + result.pairCount()
            );
        }

        List<Integer> lags =
                new ArrayList<>(
                        weightedSumByLag.keySet()
                );

        lags.sort(Integer::compareTo);

        List<EnsembleMsdResult> ensembleResults =
                new ArrayList<>();

        for (Integer lag : lags) {

            double weightedSum =
                    weightedSumByLag.get(lag);

            int pairCount =
                    pairCountByLag.get(lag);

            if (pairCount <= 0) {
                continue;
            }

            double ensembleMsd =
                    weightedSum / pairCount;

            ensembleResults.add(
                    new EnsembleMsdResult(
                            lag,
                            ensembleMsd,
                            pairCount
                    )
            );
        }

        return ensembleResults;
    }
}