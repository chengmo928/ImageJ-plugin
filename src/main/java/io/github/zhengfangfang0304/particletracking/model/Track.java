package io.github.zhengfangfang0304.particletracking.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一条颗粒轨迹。
 */
public final class Track {

    public final int id;

    public final List<Detection> detections =
            new ArrayList<>();

    public Track(
            int id,
            Detection firstDetection
    ) {
        this.id = id;
        addDetection(firstDetection);
    }

    public void addDetection(
            Detection detection
    ) {
        detections.add(detection);
    }

    public Detection getLastDetection() {
        if (detections.isEmpty()) {
            throw new IllegalStateException(
                    "轨迹中没有检测点。"
            );
        }

        return detections.get(
                detections.size() - 1
        );
    }
}
