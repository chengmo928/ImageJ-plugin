package io.github.zhengfangfang0304.particletracking.detection;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.util.DistanceUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * 基于阈值连通区域的强度加权质心检测器。
 *
 * 处理流程：
 * 1. 找出所有强度大于阈值的像素；
 * 2. 使用四邻域连通规则，将相邻像素划分成区域；
 * 3. 对每个区域计算强度加权质心；
 * 4. 使用最小距离参数去除彼此过近的检测结果。
 */
public final class CentroidDetector implements ParticleDetector {

    /**
     * 返回检测器在界面中显示的名称。
     */
    @Override
    public String getName() {
        return "Centroid 质心定位";
    }

    /**
     * 对整个图像序列逐帧执行质心检测。
     *
     * @param image      需要检测的ImageJ图像或图像序列
     * @param parameters 检测参数
     * @return 所有帧中的颗粒检测结果
     */
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
        int totalFrames = stack.getSize();

        for (int frame = 1;
             frame <= totalFrames;
             frame++) {

            ImageProcessor processor =
                    stack.getProcessor(frame);

            FloatProcessor floatProcessor =
                    processor.convertToFloatProcessor();

            /*
             * 找出当前帧中的所有质心候选点。
             */
            List<Detection> candidates =
                    findCentroidDetections(
                            floatProcessor,
                            frame,
                            parameters.threshold()
                    );

            /*
             * 按强度从高到低排列。
             *
             * 当两个检测点距离过近时，
             * 优先保留强度更高的检测结果。
             */
            candidates.sort(
                    Comparator.comparingDouble(
                            (Detection detection) ->
                                    detection.intensity
                    ).reversed()
            );

            /*
             * 删除距离过近的重复检测点。
             */
            List<Detection> accepted =
                    suppressNearbyDetections(
                            candidates,
                            parameters.minimumDistance()
                    );

            allDetections.addAll(accepted);

            IJ.showProgress(frame, totalFrames);
        }

        return allDetections;
    }

    /**
     * 在单帧图像中寻找所有阈值连通区域，
     * 并计算每个区域的强度加权质心。
     */
    private List<Detection> findCentroidDetections(
            FloatProcessor processor,
            int frame,
            double threshold
    ) {
        List<Detection> detections =
                new ArrayList<>();

        int width = processor.getWidth();
        int height = processor.getHeight();

        /*
         * visited[x][y]表示该像素是否已经被检查。
         */
        boolean[][] visited =
                new boolean[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (visited[x][y]) {
                    continue;
                }

                float value =
                        processor.getf(x, y);

                /*
                 * 低于阈值的像素不属于颗粒区域。
                 */
                if (value < threshold) {
                    visited[x][y] = true;
                    continue;
                }

                /*
                 * 保存当前连通区域中的所有像素。
                 */
                List<int[]> regionPixels =
                        new ArrayList<>();

                floodFillRegion(
                        processor,
                        x,
                        y,
                        threshold,
                        visited,
                        regionPixels
                );

                if (regionPixels.isEmpty()) {
                    continue;
                }

                double sumIntensity = 0.0;
                double weightedX = 0.0;
                double weightedY = 0.0;
                double maximumIntensity =
                        Double.NEGATIVE_INFINITY;

                /*
                 * 强度加权质心：
                 *
                 * centroidX = Σ(x × intensity) / Σ(intensity)
                 * centroidY = Σ(y × intensity) / Σ(intensity)
                 */
                for (int[] pixel : regionPixels) {

                    int pixelX = pixel[0];
                    int pixelY = pixel[1];

                    double intensity =
                            processor.getf(
                                    pixelX,
                                    pixelY
                            );

                    sumIntensity += intensity;
                    weightedX += pixelX * intensity;
                    weightedY += pixelY * intensity;

                    if (intensity > maximumIntensity) {
                        maximumIntensity = intensity;
                    }
                }

                if (sumIntensity <= 0.0) {
                    continue;
                }

                double centroidX =
                        weightedX / sumIntensity;

                double centroidY =
                        weightedY / sumIntensity;

                detections.add(
                        new Detection(
                                frame,
                                centroidX,
                                centroidY,
                                maximumIntensity
                        )
                );
            }
        }

        return detections;
    }

    /**
     * 从指定起始像素出发，寻找同一阈值连通区域中的全部像素。
     *
     * 当前使用四邻域：
     * 上、下、左、右。
     */
    private void floodFillRegion(
            FloatProcessor processor,
            int startX,
            int startY,
            double threshold,
            boolean[][] visited,
            List<int[]> regionPixels
    ) {
        int width = processor.getWidth();
        int height = processor.getHeight();

        Deque<int[]> queue =
                new ArrayDeque<>();

        queue.addLast(
                new int[]{startX, startY}
        );

        visited[startX][startY] = true;

        while (!queue.isEmpty()) {

            int[] current =
                    queue.removeFirst();

            int x = current[0];
            int y = current[1];

            regionPixels.add(
                    new int[]{x, y}
            );

            addNeighborIfValid(
                    processor,
                    x + 1,
                    y,
                    threshold,
                    visited,
                    queue,
                    width,
                    height
            );

            addNeighborIfValid(
                    processor,
                    x - 1,
                    y,
                    threshold,
                    visited,
                    queue,
                    width,
                    height
            );

            addNeighborIfValid(
                    processor,
                    x,
                    y + 1,
                    threshold,
                    visited,
                    queue,
                    width,
                    height
            );

            addNeighborIfValid(
                    processor,
                    x,
                    y - 1,
                    threshold,
                    visited,
                    queue,
                    width,
                    height
            );
        }
    }

    /**
     * 检查相邻像素是否有效。
     *
     * 若像素：
     * 1. 没有超出图像范围；
     * 2. 尚未访问；
     * 3. 强度不低于阈值；
     *
     * 就加入待检查队列。
     */
    private void addNeighborIfValid(
            FloatProcessor processor,
            int x,
            int y,
            double threshold,
            boolean[][] visited,
            Deque<int[]> queue,
            int width,
            int height
    ) {
        if (x < 0
                || x >= width
                || y < 0
                || y >= height) {
            return;
        }

        if (visited[x][y]) {
            return;
        }

        /*
         * 一旦检查过该像素，就立即标记。
         * 避免同一个像素被反复加入队列。
         */
        visited[x][y] = true;

        if (processor.getf(x, y) < threshold) {
            return;
        }

        queue.addLast(
                new int[]{x, y}
        );
    }

    /**
     * 删除同一帧中距离过近的检测结果。
     *
     * candidates已经按照强度从高到低排列，
     * 因此距离冲突时保留强度更高的检测点。
     */
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