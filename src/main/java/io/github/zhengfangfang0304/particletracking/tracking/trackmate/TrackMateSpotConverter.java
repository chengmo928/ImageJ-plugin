package io.github.zhengfangfang0304.particletracking.tracking.trackmate;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;

import io.github.zhengfangfang0304.particletracking.model.Detection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将本程序的Detection转换为TrackMate Spot。
 */
public final class TrackMateSpotConverter {

    /**
     * 工具类不需要创建对象。
     */
    private TrackMateSpotConverter() {
    }

    /**
     * 同时保存SpotCollection和Spot到Detection的对应关系。
     */
    public record ConversionResult(
            SpotCollection spots,
            Map<Spot, Detection> spotToDetection
    ) {
    }

    /**
     * 将Detection列表转换成TrackMate输入数据。
     *
     * @param detections  本程序识别得到的检测点
     * @param totalFrames 图像总帧数
     * @param spotRadius  TrackMate Spot半径
     * @return 转换结果
     */
    public static ConversionResult convert(
            List<Detection> detections,
            int totalFrames,
            double spotRadius
    ) {
        if (detections == null) {
            throw new IllegalArgumentException(
                    "检测点列表不能为null。"
            );
        }

        if (totalFrames < 1) {
            throw new IllegalArgumentException(
                    "图像总帧数必须大于0。"
            );
        }

        if (spotRadius <= 0) {
            throw new IllegalArgumentException(
                    "Spot半径必须大于0。"
            );
        }

        SpotCollection spots =
                new SpotCollection();

        Map<Spot, Detection> spotToDetection =
                new HashMap<>();

        /*
         * 将所有帧加入SpotCollection。
         * 即使某一帧没有检测点，也保留空帧。
         */
        for (int frame0 = 0;
             frame0 < totalFrames;
             frame0++) {

            spots.put(
                    frame0,
                    Collections.emptyList()
            );
        }

        for (Detection detection : detections) {

            /*
             * 本程序的帧编号从1开始；
             * TrackMate的帧编号从0开始。
             */
            int trackMateFrame =
                    detection.frame - 1;

            if (trackMateFrame < 0
                    || trackMateFrame >= totalFrames) {
                continue;
            }

            Spot spot = new Spot(
                    detection.x,
                    detection.y,
                    0.0,
                    spotRadius,
                    detection.intensity
            );

            spot.putFeature(
                    Spot.POSITION_T,
                    (double) trackMateFrame
            );

            spots.add(
                    spot,
                    trackMateFrame
            );

            spotToDetection.put(
                    spot,
                    detection
            );
        }

        return new ConversionResult(
                spots,
                spotToDetection
        );
    }
}