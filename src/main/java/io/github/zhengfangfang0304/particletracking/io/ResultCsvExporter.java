package io.github.zhengfangfang0304.particletracking.io;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.EnsembleMsdResult;
import io.github.zhengfangfang0304.particletracking.model.MsdResult;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.model.TrackStatistics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CSV结果导出工具。
 *
 * 这个类只负责把已经计算好的结果写入CSV文件，
 * 不负责弹出文件选择框，
 * 不负责显示ResultsTable，
 * 不负责写GUI日志。
 */
public final class ResultCsvExporter {

    private ResultCsvExporter() {
    }

    /**
     * 导出轨迹坐标结果。
     *
     * @param tracks 轨迹列表
     * @param file   保存文件
     * @return 导出的数据行数，不包括表头
     */
    public static int exportTrackResults(
            List<Track> tracks,
            File file
    ) throws IOException {

        validateTracks(tracks);
        validateFile(file);

        int rowCount =
                0;

        try (PrintWriter writer =
                     new PrintWriter(
                             Files.newBufferedWriter(
                                     file.toPath(),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.println(
                    "particle,Frame,X,Y,Intensity"
            );

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

                    writer.println(
                            track.id
                                    + ","
                                    + detection.frame
                                    + ","
                                    + detection.x
                                    + ","
                                    + detection.y
                                    + ","
                                    + detection.intensity
                    );

                    rowCount++;
                }
            }
        }

        return rowCount;
    }

    /**
     * 导出轨迹统计结果。
     *
     * @param statisticsList 轨迹统计结果
     * @param file           保存文件
     * @return 导出的数据行数，不包括表头
     */
    public static int exportTrackStatistics(
            List<TrackStatistics> statisticsList,
            File file
    ) throws IOException {

        if (statisticsList == null) {
            throw new IllegalArgumentException(
                    "轨迹统计结果不能为null。"
            );
        }

        validateFile(file);

        int rowCount =
                0;

        try (PrintWriter writer =
                     new PrintWriter(
                             Files.newBufferedWriter(
                                     file.toPath(),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.println(
                    "particle,Start_Frame,End_Frame,N_Points,"
                            + "Duration_Frames,Start_X,Start_Y,"
                            + "End_X,End_Y,Displacement,Path_Length,"
                            + "Mean_Step,Mean_Speed_px_per_frame,"
                            + "Mean_Intensity"
            );

            for (TrackStatistics statistics : statisticsList) {

                writer.println(
                        statistics.trackId()
                                + ","
                                + statistics.startFrame()
                                + ","
                                + statistics.endFrame()
                                + ","
                                + statistics.numberOfPoints()
                                + ","
                                + statistics.durationFrames()
                                + ","
                                + statistics.startX()
                                + ","
                                + statistics.startY()
                                + ","
                                + statistics.endX()
                                + ","
                                + statistics.endY()
                                + ","
                                + statistics.displacement()
                                + ","
                                + statistics.pathLength()
                                + ","
                                + statistics.meanStep()
                                + ","
                                + statistics.meanSpeed()
                                + ","
                                + statistics.meanIntensity()
                );

                rowCount++;
            }
        }

        return rowCount;
    }

    /**
     * 导出单轨迹MSD结果。
     *
     * @param msdResults MSD结果
     * @param file       保存文件
     * @return 导出的数据行数，不包括表头
     */
    public static int exportMsdResults(
            List<MsdResult> msdResults,
            File file
    ) throws IOException {

        if (msdResults == null) {
            throw new IllegalArgumentException(
                    "MSD结果不能为null。"
            );
        }

        validateFile(file);

        int rowCount =
                0;

        try (PrintWriter writer =
                     new PrintWriter(
                             Files.newBufferedWriter(
                                     file.toPath(),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.println(
                    "particle,Lag_Frames,MSD_px2,N_Pairs"
            );

            for (MsdResult result : msdResults) {

                writer.println(
                        result.trackId()
                                + ","
                                + result.lagFrames()
                                + ","
                                + result.msd()
                                + ","
                                + result.pairCount()
                );

                rowCount++;
            }
        }

        return rowCount;
    }

    /**
     * 导出总体平均MSD结果。
     *
     * @param ensembleResults Ensemble MSD结果
     * @param file            保存文件
     * @return 导出的数据行数，不包括表头
     */
    public static int exportEnsembleMsdResults(
            List<EnsembleMsdResult> ensembleResults,
            File file
    ) throws IOException {

        if (ensembleResults == null) {
            throw new IllegalArgumentException(
                    "Ensemble MSD结果不能为null。"
            );
        }

        validateFile(file);

        int rowCount =
                0;

        try (PrintWriter writer =
                     new PrintWriter(
                             Files.newBufferedWriter(
                                     file.toPath(),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            writer.println(
                    "Lag_Frames,Ensemble_MSD_px2,N_Pairs"
            );

            for (EnsembleMsdResult result : ensembleResults) {

                writer.println(
                        result.lagFrames()
                                + ","
                                + result.ensembleMsd()
                                + ","
                                + result.pairCount()
                );

                rowCount++;
            }
        }

        return rowCount;
    }

    private static void validateTracks(
            List<Track> tracks
    ) {
        if (tracks == null) {
            throw new IllegalArgumentException(
                    "轨迹列表不能为null。"
            );
        }
    }

    private static void validateFile(
            File file
    ) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "保存文件不能为null。"
            );
        }
    }
}