package io.github.zhengfangfang0304.particletracking.gui;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.measure.ResultsTable;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ImageJ结果显示工具。
 *
 * 这个类负责：
 * 1. 显示检测结果表格；
 * 2. 在图像上绘制检测点；
 * 3. 显示追踪结果表格；
 * 4. 在图像上绘制轨迹线；
 * 5. 显示外部导入轨迹表格。
 *
 * 它不负责：
 * 1. 执行检测算法；
 * 2. 执行追踪算法；
 * 3. 计算MSD；
 * 4. 读写CSV文件。
 */
public final class ResultTablePresenter {

    private ResultTablePresenter() {
    }

    /**
     * 显示颗粒检测结果。
     *
     * @param image      当前图像
     * @param detections 检测结果
     * @param tableTitle 表格标题
     */
    public static void showDetections(
            ImagePlus image,
            List<Detection> detections,
            String tableTitle
    ) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "显示检测结果时，图像不能为null。"
            );
        }

        if (detections == null) {
            throw new IllegalArgumentException(
                    "检测结果不能为null。"
            );
        }

        ResultsTable table =
                new ResultsTable();

        Overlay overlay =
                new Overlay();

        for (Detection detection : detections) {

            table.incrementCounter();

            table.addValue(
                    "Frame",
                    detection.frame
            );

            table.addValue(
                    "X",
                    detection.x
            );

            table.addValue(
                    "Y",
                    detection.y
            );

            table.addValue(
                    "Intensity",
                    detection.intensity
            );

            OvalRoi roi =
                    new OvalRoi(
                            detection.x - 4,
                            detection.y - 4,
                            8,
                            8
                    );

            roi.setStrokeColor(
                    Color.RED
            );

            roi.setPosition(
                    detection.frame
            );

            overlay.add(
                    roi
            );
        }

        image.setOverlay(
                overlay
        );

        table.show(
                tableTitle
        );
    }

    /**
     * 显示外部CSV导入的轨迹坐标表格。
     *
     * @param tracks 导入的轨迹
     */
    public static void showImportedTrackResults(
            List<Track> tracks
    ) {
        if (tracks == null) {
            throw new IllegalArgumentException(
                    "导入轨迹不能为null。"
            );
        }

        ResultsTable trackTable =
                new ResultsTable();

        for (Track track : tracks) {

            List<Detection> points =
                    new ArrayList<>(
                            track.detections
                    );

            points.sort(
                    Comparator.comparingInt(
                            detection -> detection.frame
                    )
            );

            for (Detection detection : points) {

                trackTable.incrementCounter();

                trackTable.addValue(
                        "particle",
                        track.id
                );

                trackTable.addValue(
                        "frame",
                        detection.frame
                );

                trackTable.addValue(
                        "x",
                        detection.x
                );

                trackTable.addValue(
                        "y",
                        detection.y
                );

                trackTable.addValue(
                        "Intensity",
                        detection.intensity
                );
            }
        }

        trackTable.show(
                "Imported Track Results"
        );
    }

    /**
     * 显示追踪结果。
     *
     * @param image      当前图像
     * @param detections 检测点，用于重新绘制红色圆圈
     * @param tracks     轨迹结果
     * @param tableTitle 表格标题
     */
    public static void showTrackingResults(
            ImagePlus image,
            List<Detection> detections,
            List<Track> tracks,
            String tableTitle
    ) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "显示追踪结果时，图像不能为null。"
            );
        }

        if (detections == null) {
            throw new IllegalArgumentException(
                    "检测结果不能为null。"
            );
        }

        if (tracks == null) {
            throw new IllegalArgumentException(
                    "追踪结果不能为null。"
            );
        }

        ResultsTable trackTable =
                new ResultsTable();

        Overlay overlay =
                new Overlay();

        /*
         * 先重新绘制红色检测圆。
         */
        for (Detection detection : detections) {

            OvalRoi roi =
                    new OvalRoi(
                            detection.x - 4,
                            detection.y - 4,
                            8,
                            8
                    );

            roi.setStrokeColor(
                    Color.RED
            );

            roi.setPosition(
                    detection.frame
            );

            overlay.add(
                    roi
            );
        }

        /*
         * 再写入轨迹表格并绘制绿色连接线。
         */
        for (Track track : tracks) {

            List<Detection> points =
                    new ArrayList<>(
                            track.detections
                    );

            points.sort(
                    Comparator.comparingInt(
                            detection -> detection.frame
                    )
            );

            for (Detection detection : points) {

                trackTable.incrementCounter();

                trackTable.addValue(
                        "particle",
                        track.id
                );

                trackTable.addValue(
                        "Frame",
                        detection.frame
                );

                trackTable.addValue(
                        "X",
                        detection.x
                );

                trackTable.addValue(
                        "Y",
                        detection.y
                );

                trackTable.addValue(
                        "Intensity",
                        detection.intensity
                );
            }

            for (int index = 1;
                 index < points.size();
                 index++) {

                Detection previous =
                        points.get(index - 1);

                Detection current =
                        points.get(index);

                Line line =
                        new Line(
                                previous.x,
                                previous.y,
                                current.x,
                                current.y
                        );

                line.setStrokeColor(
                        Color.GREEN
                );

                line.setStrokeWidth(
                        2
                );

                /*
                 * 0表示所有帧都能看到连接线。
                 */
                line.setPosition(
                        0
                );

                overlay.add(
                        line
                );
            }
        }

        image.setOverlay(
                overlay
        );

        trackTable.show(
                tableTitle
        );
    }
}
