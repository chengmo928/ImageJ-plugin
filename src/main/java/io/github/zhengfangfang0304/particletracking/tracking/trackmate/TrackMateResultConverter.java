package io.github.zhengfangfang0304.particletracking.tracking.trackmate;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 将TrackMate追踪结果转换为本程序的Track对象。
 */
public final class TrackMateResultConverter {

    private TrackMateResultConverter() {
    }

    public static List<Track> convert(
            Model model,
            Map<Spot, Detection> spotToDetection
    ) {
        if (model == null) {
            throw new IllegalArgumentException(
                    "TrackMate Model不能为null。"
            );
        }

        if (spotToDetection == null) {
            throw new IllegalArgumentException(
                    "Spot对应关系不能为null。"
            );
        }

        TrackModel trackModel =
                model.getTrackModel();

        List<Integer> trackIds =
                new ArrayList<>(
                        trackModel.trackIDs(false)
                );

        trackIds.sort(Integer::compareTo);

        List<Track> convertedTracks =
                new ArrayList<>();

        for (Integer trackId : trackIds) {

            List<Spot> trackSpots =
                    new ArrayList<>(
                            trackModel.trackSpots(trackId)
                    );

            trackSpots.sort(
                    Comparator.comparingDouble(
                            spot -> spot.getFeature(
                                    Spot.FRAME
                            )
                    )
            );

            List<Detection> trackDetections =
                    new ArrayList<>();

            for (Spot spot : trackSpots) {

                Detection detection =
                        spotToDetection.get(spot);

                /*
                 * 正常情况下都能直接找到。
                 * 这里保留备用转换逻辑。
                 */
                if (detection == null) {

                    Double frameFeature =
                            spot.getFeature(
                                    Spot.FRAME
                            );

                    Double quality =
                            spot.getFeature(
                                    Spot.QUALITY
                            );

                    int frame =
                            frameFeature == null
                                    ? 1
                                    : frameFeature.intValue() + 1;

                    detection = new Detection(
                            frame,
                            spot.getDoublePosition(0),
                            spot.getDoublePosition(1),
                            quality == null
                                    ? 0.0
                                    : quality
                    );
                }

                trackDetections.add(detection);
            }

            if (trackDetections.isEmpty()) {
                continue;
            }

            trackDetections.sort(
                    Comparator.comparingInt(
                            detection -> detection.frame
                    )
            );

            Track convertedTrack =
                    new Track(
                            trackId,
                            trackDetections.get(0)
                    );

            for (int i = 1;
                 i < trackDetections.size();
                 i++) {

                convertedTrack.addDetection(
                        trackDetections.get(i)
                );
            }

            convertedTracks.add(convertedTrack);
        }

        return convertedTracks;
    }
}