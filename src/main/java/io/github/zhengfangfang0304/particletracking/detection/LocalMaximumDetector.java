package io.github.zhengfangfang0304.particletracking.detection;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.util.DistanceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LocalMaximumDetector
        implements ParticleDetector {

    @Override
    public String getName() {
        return "Local Maximum 局部极大值";
    }

    @Override
    public List<Detection> detect(
            ImagePlus image,
            DetectionParameters parameters
    ) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "输入图像不能为null。"
            );
        }

        if (parameters == null) {
            throw new IllegalArgumentException(
                  "检测参数不能为null。"
            );
        }
        List<Detection> allDetections =
                new ArrayList<>();

        ImageStack stack = image.getStack();

        for (int frame = 1;
             frame <= stack.getSize();
             frame++) {

            ImageProcessor processor =
                    stack.getProcessor(frame);

            FloatProcessor floatProcessor =
                    processor.convertToFloatProcessor();

            List<Detection> candidates =
                    findLocalMaxima(
                            floatProcessor,
                            frame,
                            parameters.threshold(),
                            parameters.localMaximumRadius()
                    );

            candidates.sort(
                    Comparator.comparingDouble(
                            (Detection detection) ->
                                    detection.intensity
                    ).reversed()
            );

            List<Detection> accepted =
                    suppressNearbyDetections(
                            candidates,
                            parameters.minimumDistance()
                    );

            allDetections.addAll(accepted);

            IJ.showProgress(
                    frame,
                    stack.getSize()
            );
        }

        return allDetections;
    }

    private List<Detection> findLocalMaxima(
            FloatProcessor fp,
            int frame,
            double threshold,
            int radius
    ) {
        List<Detection> detections = new ArrayList<>();

        int width = fp.getWidth();
        int height = fp.getHeight();

        for (int y = radius; y < height - radius; y++) {
            for (int x = radius; x < width - radius; x++) {

                float centerValue = fp.getf(x, y);

                // 当前像素没有达到阈值，不作为候选颗粒。
                if (centerValue < threshold) {
                    continue;
                }

                boolean isLocalMaximum = true;

                /*
                * 检查以当前像素为中心、
                * 半径为 radius 的邻域。
                */
                for (int yy = y - radius;
                    yy <= y + radius;
                    yy++) {

                    for (int xx = x - radius;
                        xx <= x + radius;
                        xx++) {

                        // 不需要拿中心像素和自己比较。
                        if (xx == x && yy == y) {
                            continue;
                        }

                        /*
                        * 只要邻域中有像素比中心像素更亮，
                        * 当前像素就不是局部极大值。
                        */
                        if (fp.getf(xx, yy) > centerValue) {
                            isLocalMaximum = false;
                            break;
                        }
                    }

                    if (!isLocalMaximum) {
                        break;
                    }
                }

                if (isLocalMaximum) {
                    detections.add(
                            new Detection(
                                    frame,
                                    x,
                                    y,
                                    centerValue
                            )
                    );
                }
            }
        }

        return detections;
    }

    private List<Detection> suppressNearbyDetections(
            List<Detection> candidates,
            double minimumDistance
    ) {
        List<Detection> accepted =
                new ArrayList<>();

        for (Detection candidate : candidates) {
            boolean tooClose = false;

            for (Detection previous : accepted) {
                double distance =
                        DistanceUtils.euclidean(
                                candidate.x,
                                candidate.y,
                                previous.x,
                                previous.y
                        );

                if (distance < minimumDistance) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                accepted.add(candidate);
            }
        }

        return accepted;
    }
}
