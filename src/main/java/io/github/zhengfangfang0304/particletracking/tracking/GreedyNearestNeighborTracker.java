package io.github.zhengfangfang0304.particletracking.tracking;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.util.DistanceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自编贪心最近邻追踪器。
 *
 * 基本思想：
 * 1. 将检测点按照帧号分组；
 * 2. 从第一帧开始建立轨迹；
 * 3. 对后续每一帧，计算已有轨迹末端点与当前检测点的距离；
 * 4. 只保留距离不超过最大连接距离的候选连接；
 * 5. 按距离从小到大排序；
 * 6. 使用贪心策略完成一对一连接；
 * 7. 未被连接的检测点开启新轨迹。
 */
public final class GreedyNearestNeighborTracker
        implements ParticleTracker {

    @Override
    public String getName() {
        return "Greedy Nearest Neighbor 自编最近邻";
    }

    @Override
    public List<Track> track(
            List<Detection> detections,
            ImagePlus image,
            TrackingParameters parameters
    ) {
        if (detections == null) {
            throw new IllegalArgumentException(
                    "检测结果不能为null。"
            );
        }

        if (parameters == null) {
            throw new IllegalArgumentException(
                    "追踪参数不能为null。"
            );
        }

        if (detections.isEmpty()) {
            return new ArrayList<>();
        }

        double maximumLinkingDistance =
                parameters.maximumLinkingDistance();

        int maximumFrameGap =
                parameters.maximumFrameGap();

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

        Map<Integer, List<Detection>> detectionsByFrame =
                groupDetectionsByFrame(detections);

        List<Integer> frameNumbers =
                new ArrayList<>(detectionsByFrame.keySet());

        frameNumbers.sort(Integer::compareTo);

        List<Track> allTracks =
                new ArrayList<>();

        int nextTrackId = 0;

        /*
         * 从最早出现的帧开始。
         */
        int firstFrame =
                frameNumbers.get(0);

        List<Detection> firstFrameDetections =
                detectionsByFrame.get(firstFrame);

        for (Detection detection : firstFrameDetections) {
            Track track =
                    new Track(
                            nextTrackId,
                            detection
                    );

            allTracks.add(track);
            nextTrackId++;
        }

        /*
         * 从第二个有检测点的帧开始逐帧连接。
         */
        for (int frameIndex = 1;
             frameIndex < frameNumbers.size();
             frameIndex++) {

            int currentFrame =
                    frameNumbers.get(frameIndex);

            List<Detection> currentDetections =
                    detectionsByFrame.get(currentFrame);

            List<Track> candidateTracks =
                    findCandidateTracks(
                            allTracks,
                            currentFrame,
                            maximumFrameGap
                    );

            List<LinkCandidate> candidates =
                    buildLinkCandidates(
                            candidateTracks,
                            currentDetections,
                            maximumLinkingDistance
                    );

            candidates.sort(
                    Comparator.comparingDouble(
                            candidate -> candidate.distance
                    )
            );

            Set<Track> matchedTracks =
                    new HashSet<>();

            Set<Detection> matchedDetections =
                    new HashSet<>();

            for (LinkCandidate candidate : candidates) {

                if (matchedTracks.contains(candidate.track)) {
                    continue;
                }

                if (matchedDetections.contains(candidate.detection)) {
                    continue;
                }

                candidate.track.addDetection(
                        candidate.detection
                );

                matchedTracks.add(candidate.track);
                matchedDetections.add(candidate.detection);
            }

            /*
             * 没有被任何旧轨迹连接的检测点，
             * 作为新轨迹的起点。
             */
            for (Detection detection : currentDetections) {

                if (matchedDetections.contains(detection)) {
                    continue;
                }

                Track newTrack =
                        new Track(
                                nextTrackId,
                                detection
                        );

                allTracks.add(newTrack);
                nextTrackId++;
            }
        }

        return allTracks;
    }

    /**
     * 按帧号对检测点分组。
     */
    private Map<Integer, List<Detection>> groupDetectionsByFrame(
            List<Detection> detections
    ) {
        Map<Integer, List<Detection>> detectionsByFrame =
                new HashMap<>();

        for (Detection detection : detections) {
            detectionsByFrame
                    .computeIfAbsent(
                            detection.frame,
                            key -> new ArrayList<>()
                    )
                    .add(detection);
        }

        return detectionsByFrame;
    }

    /**
     * 找出当前帧之前、仍允许连接的轨迹。
     *
     * maximumFrameGap = 0 表示只允许连接到上一帧；
     * maximumFrameGap = 1 表示允许中间漏掉1帧；
     * 依此类推。
     */
    private List<Track> findCandidateTracks(
            List<Track> allTracks,
            int currentFrame,
            int maximumFrameGap
    ) {
        List<Track> candidateTracks =
                new ArrayList<>();

        int maximumAllowedFrameDifference =
                maximumFrameGap + 1;

        for (Track track : allTracks) {

            Detection lastDetection =
                    track.getLastDetection();

            int frameDifference =
                    currentFrame - lastDetection.frame;

            if (frameDifference >= 1
                    && frameDifference <= maximumAllowedFrameDifference) {

                candidateTracks.add(track);
            }
        }

        return candidateTracks;
    }

    /**
     * 生成所有满足距离条件的候选连接。
     */
    private List<LinkCandidate> buildLinkCandidates(
            List<Track> candidateTracks,
            List<Detection> currentDetections,
            double maximumLinkingDistance
    ) {
        List<LinkCandidate> candidates =
                new ArrayList<>();

        for (Track track : candidateTracks) {

            Detection lastDetection =
                    track.getLastDetection();

            for (Detection detection : currentDetections) {

                double distance =
                        DistanceUtils.euclidean(
                                lastDetection.x,
                                lastDetection.y,
                                detection.x,
                                detection.y
                        );

                if (distance <= maximumLinkingDistance) {

                    candidates.add(
                            new LinkCandidate(
                                    track,
                                    detection,
                                    distance
                            )
                    );
                }
            }
        }

        return candidates;
    }

    /**
     * 候选连接。
     */
    private static final class LinkCandidate {

        private final Track track;
        private final Detection detection;
        private final double distance;

        private LinkCandidate(
                Track track,
                Detection detection,
                double distance
        ) {
            this.track = track;
            this.detection = detection;
            this.distance = distance;
        }
    }
}