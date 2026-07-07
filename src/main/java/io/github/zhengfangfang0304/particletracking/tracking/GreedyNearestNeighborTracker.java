package io.github.zhengfangfang0304.particletracking.tracking;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.util.DistanceUtils;

import java.util.List;

public final class GreedyNearestNeighborTracker
        implements ParticleTracker {

    @Override
    public String getName() {
        return "自编贪心最近邻";
    }

    @Override
    public List<Track> track(
            List<Detection> detections,
            ImagePlus image,
            TrackingParameters parameters
    ) {
        /*
         * 下一步把Simple_GUI中自编追踪的算法代码
         * 移动到这里。
         */
        throw new UnsupportedOperationException(
                "贪心最近邻追踪尚未实现。"
        );
    }
}
