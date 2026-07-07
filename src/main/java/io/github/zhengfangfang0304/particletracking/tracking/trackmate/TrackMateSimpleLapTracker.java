package io.github.zhengfangfang0304.particletracking.tracking.trackmate;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.tracking.ParticleTracker;
import io.github.zhengfangfang0304.particletracking.tracking.TrackingParameters;

import java.util.List;

/**
 * TrackMate Simple LAP追踪器。
 * 后续补充具体实现。
 */
public final class TrackMateSimpleLapTracker
        implements ParticleTracker {

    @Override
    public String getName() {
        return "TrackMate Simple LAP";
    }

    @Override
    public List<Track> track(
            List<Detection> detections,
            ImagePlus image,
            TrackingParameters parameters
    ) {
        throw new UnsupportedOperationException(
                "TrackMate Simple LAP尚未实现。"
        );
    }
}