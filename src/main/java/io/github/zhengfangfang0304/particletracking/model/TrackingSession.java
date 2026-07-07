package io.github.zhengfangfang0304.particletracking.model;

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存当前一次分析任务的图像、检测结果和轨迹结果。
 */
public final class TrackingSession {

    private ImagePlus image;

    private final List<Detection> detections =
            new ArrayList<>();

    private final List<Track> tracks =
            new ArrayList<>();

    public ImagePlus getImage() {
        return image;
    }

    public void setImage(ImagePlus image) {
        this.image = image;
        clearResults();
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setDetections(
            List<Detection> newDetections
    ) {
        detections.clear();
        detections.addAll(newDetections);

        // 检测结果改变后，旧轨迹不能继续使用。
        tracks.clear();
    }

    public void setTracks(
            List<Track> newTracks
    ) {
        tracks.clear();
        tracks.addAll(newTracks);
    }

    public void clearResults() {
        detections.clear();
        tracks.clear();
    }
}
