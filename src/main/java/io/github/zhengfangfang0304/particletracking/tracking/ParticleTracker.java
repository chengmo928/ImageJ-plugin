package io.github.zhengfangfang0304.particletracking.tracking;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.util.List;

public interface ParticleTracker {

    String getName();

    List<Track> track(
            List<Detection> detections,
            ImagePlus image,
            TrackingParameters parameters
    );
}
//统一追踪接口的定义，只规定“追踪器必须提供哪些功能”，本身不包含具体追踪算法