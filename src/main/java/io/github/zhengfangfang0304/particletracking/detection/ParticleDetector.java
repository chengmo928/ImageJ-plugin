package io.github.zhengfangfang0304.particletracking.detection;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;

import java.util.List;

/**
 * 所有颗粒检测算法都必须遵循的统一接口。
 */
public interface ParticleDetector {

    /**
     * 返回界面中显示的算法名称。
     */
    String getName();

    /**
     * 在图像中执行颗粒检测。
     */
    List<Detection> detect(
            ImagePlus image,
            DetectionParameters parameters
    );
}