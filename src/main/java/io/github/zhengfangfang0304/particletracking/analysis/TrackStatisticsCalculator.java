package io.github.zhengfangfang0304.particletracking.analysis;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.model.TrackStatistics;
import io.github.zhengfangfang0304.particletracking.util.DistanceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 轨迹统计计算器。
 *
 * 负责计算每条轨迹的：
 * 1. 起始帧；
 * 2. 结束帧；
 * 3. 点数；
 * 4. 持续帧数；
 * 5. 净位移；
 * 6. 路径长度；
 * 7. 平均步长；
 * 8. 平均速度；
 * 9. 平均强度。
 */
public final class TrackStatisticsCalculator {

    private TrackStatisticsCalculator() {
    }

    public static List<TrackStatistics> calculate(
            List<Track> tracks
    ) {
        if (tracks == null) {
            throw new IllegalArgumentException(
                    "轨迹列表不能为null。"
            );
        }

        List<TrackStatistics> results =
                new ArrayList<>();

        for (Track track : tracks) {

            if (track == null
                    || track.detections == null
                    || track.detections.isEmpty()) {
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

            Detection first =
                    points.get(0);

            Detection last =
                    points.get(points.size() - 1);

            int startFrame =
                    first.frame;

            int endFrame =
                    last.frame;

            int numberOfPoints =
                    points.size();

            int durationFrames =
                    endFrame - startFrame + 1;

            double displacement =
                    DistanceUtils.euclidean(
                            first.x,
                            first.y,
                            last.x,
                            last.y
                    );

            double pathLength =
                    0.0;

            double intensitySum =
                    0.0;

            for (Detection detection : points) {
                intensitySum += detection.intensity;
            }

            for (int i = 1;
                 i < points.size();
                 i++) {

                Detection previous =
                        points.get(i - 1);

                Detection current =
                        points.get(i);

                pathLength +=
                        DistanceUtils.euclidean(
                                previous.x,
                                previous.y,
                                current.x,
                                current.y
                        );
            }

            double meanIntensity =
                    intensitySum / numberOfPoints;

            double meanStep =
                    0.0;

            if (numberOfPoints > 1) {
                meanStep =
                        pathLength / (numberOfPoints - 1);
            }

            double meanSpeed =
                    0.0;

            if (durationFrames > 1) {
                meanSpeed =
                        pathLength / (durationFrames - 1);
            }

            TrackStatistics statistics =
                    new TrackStatistics(
                            track.id,
                            startFrame,
                            endFrame,
                            numberOfPoints,
                            durationFrames,
                            first.x,
                            first.y,
                            last.x,
                            last.y,
                            displacement,
                            pathLength,
                            meanStep,
                            meanSpeed,
                            meanIntensity
                    );

            results.add(statistics);
        }

        return results;
    }
}
