package io.github.zhengfangfang0304.particletracking.controller;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.detection.DetectionParameters;
import io.github.zhengfangfang0304.particletracking.detection.ParticleDetector;
import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.tracking.ParticleTracker;
import io.github.zhengfangfang0304.particletracking.tracking.TrackingParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * 单颗粒追踪程序的控制器。
 *
 * 负责：
 * 1. 保存当前图像；
 * 2. 调用检测器；
 * 3. 调用追踪器；
 * 4. 保存当前检测和追踪结果；
 * 5. 把结果返回给GUI显示。
 *
 * 不负责：
 * 1. 创建按钮和输入框；
 * 2. 显示ResultsTable或Overlay；
 * 3. 实现具体检测或追踪算法。
 */
public final class ParticleTrackingController {

    /**
     * 当前正在分析的图像。
     */
    private ImagePlus currentImage;

    /**
     * 当前图像的颗粒检测结果。
     */
    private final List<Detection> detections =
            new ArrayList<>();

    /**
     * 当前图像的追踪结果。
     */
    private final List<Track> tracks =
            new ArrayList<>();

    /**
     * 调用指定检测器执行颗粒检测。
     *
     * @param image      当前图像
     * @param detector   具体检测器
     * @param parameters 检测参数
     * @return 检测结果副本
     */
    public List<Detection> detect(
            ImagePlus image,
            ParticleDetector detector,
            DetectionParameters parameters
    ) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "检测图像不能为null。"
            );
        }

        if (detector == null) {
            throw new IllegalArgumentException(
                    "检测器不能为null。"
            );
        }

        if (parameters == null) {
            throw new IllegalArgumentException(
                    "检测参数不能为null。"
            );
        }

        List<Detection> result =
                detector.detect(
                        image,
                        parameters
                );

        if (result == null) {
            throw new IllegalStateException(
                    "检测器返回了null结果。"
            );
        }

        currentImage = image;

        detections.clear();
        detections.addAll(result);

        /*
         * 检测结果发生变化后，
         * 以前的追踪结果不再有效。
         */
        tracks.clear();

        return List.copyOf(detections);
    }

    /**
     * 调用指定追踪器执行颗粒连接。
     *
     * @param tracker    具体追踪器
     * @param parameters 追踪参数
     * @return 追踪结果副本
     */
    public List<Track> track(
            ParticleTracker tracker,
            TrackingParameters parameters
    ) {
        if (currentImage == null) {
            throw new IllegalStateException(
                    "还没有设置当前图像，请先执行颗粒检测。"
            );
        }

        if (detections.isEmpty()) {
            throw new IllegalStateException(
                    "还没有颗粒检测结果，请先执行颗粒检测。"
            );
        }

        if (tracker == null) {
            throw new IllegalArgumentException(
                    "追踪器不能为null。"
            );
        }

        if (parameters == null) {
            throw new IllegalArgumentException(
                    "追踪参数不能为null。"
            );
        }

        List<Track> result =
                tracker.track(
                        List.copyOf(detections),
                        currentImage,
                        parameters
                );
        if (result == null) {
            throw new IllegalStateException(
                    "追踪器返回了null结果。"
            );
        }

        tracks.clear();
        tracks.addAll(result);

        return List.copyOf(tracks);
    }

    /**
     * 返回当前图像。
     */
    public ImagePlus getCurrentImage() {
        return currentImage;
    }

    /**
     * 将外部导入的检测点和轨迹保存到Controller。
     *
     * 这个方法不负责读取CSV，
     * 只负责接收已经解析完成的数据。
     *
     * @param image              与导入结果对应的图像，可以为null
     * @param importedDetections 外部导入的检测点
     * @param importedTracks     外部导入的轨迹
     */
    public void loadImportedResults(
            ImagePlus image,
            List<Detection> importedDetections,
            List<Track> importedTracks
    ) {
        if (importedDetections == null) {
            throw new IllegalArgumentException(
                    "导入的检测结果不能为null。"
            );
        }

        if (importedTracks == null) {
            throw new IllegalArgumentException(
                    "导入的轨迹结果不能为null。"
            );
        }

        /*
        * 保存与CSV结果对应的图像。
        * 如果导入CSV时没有打开图像，这里允许为null。
        */
        currentImage = image;

        detections.clear();
        detections.addAll(importedDetections);

        tracks.clear();
        tracks.addAll(importedTracks);
    }

    /**
     * 返回当前检测结果副本。
     */
    public List<Detection> getDetections() {
        return List.copyOf(detections);
    }

    /**
     * 返回当前追踪结果副本。
     */
    public List<Track> getTracks() {
        return List.copyOf(tracks);
    }

    /**
     * 清空当前检测和追踪结果。
     */
    public void clearResults() {
        detections.clear();
        tracks.clear();
    }

    /**
     * 清空整个分析会话。
     */
    public void clearSession() {
        currentImage = null;
        clearResults();
    }
}
