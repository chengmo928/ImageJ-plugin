package io.github.zhengfangfang0304.particletracking.io;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部轨迹CSV导入器。
 *
 * 这个类只负责读取和解析CSV文件，
 * 不负责弹出文件选择框，
 * 不负责显示ResultsTable，
 * 不负责写GUI日志。
 */
public final class TrackCsvImporter {

    private TrackCsvImporter() {
    }

    /**
     * 从CSV文件中导入轨迹。
     *
     * CSV至少需要包含：
     * particle, frame, x, y
     *
     * 可选强度列：
     * intensity, mass, signal
     *
     * @param csvFile CSV文件
     * @return 导入结果
     */
    public static ImportResult importFrom(
            File csvFile
    ) throws IOException {

        if (csvFile == null) {
            throw new IllegalArgumentException(
                    "CSV文件不能为null。"
            );
        }

        if (!csvFile.isFile()) {
            throw new IllegalArgumentException(
                    "CSV文件不存在或不是普通文件。"
            );
        }

        Map<Integer, Track> importedTrackMap =
                new HashMap<>();

        List<Detection> importedDetections =
                new ArrayList<>();

        int importedRows =
                0;

        boolean intensityColumnFound =
                false;

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(csvFile)
                     )) {

            String headerLine =
                    reader.readLine();

            if (headerLine == null
                    || headerLine.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "CSV文件为空。"
                );
            }

            String[] headers =
                    headerLine.split(",");

            Map<String, Integer> columnMap =
                    buildColumnMap(headers);

            validateRequiredColumns(columnMap);

            int particleColumn =
                    columnMap.get("particle");

            int frameColumn =
                    columnMap.get("frame");

            int xColumn =
                    columnMap.get("x");

            int yColumn =
                    columnMap.get("y");

            Integer intensityColumn =
                    findIntensityColumn(columnMap);

            intensityColumnFound =
                    intensityColumn != null;

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values =
                        line.split(",");

                int requiredMaximumColumn =
                        Math.max(
                                Math.max(
                                        particleColumn,
                                        frameColumn
                                ),
                                Math.max(
                                        xColumn,
                                        yColumn
                                )
                        );

                if (values.length <= requiredMaximumColumn) {
                    continue;
                }

                try {
                    int particle =
                            (int) Double.parseDouble(
                                    cleanValue(
                                            values[particleColumn]
                                    )
                            );

                    int frame =
                            (int) Double.parseDouble(
                                    cleanValue(
                                            values[frameColumn]
                                    )
                            );

                    double x =
                            Double.parseDouble(
                                    cleanValue(
                                            values[xColumn]
                                    )
                            );

                    double y =
                            Double.parseDouble(
                                    cleanValue(
                                            values[yColumn]
                                    )
                            );

                    double intensity =
                            0.0;

                    if (intensityColumn != null
                            && values.length > intensityColumn) {

                        String intensityText =
                                cleanValue(
                                        values[intensityColumn]
                                );

                        if (!intensityText.isEmpty()) {
                            intensity =
                                    Double.parseDouble(
                                            intensityText
                                    );
                        }
                    }

                    Detection detection =
                            new Detection(
                                    frame,
                                    x,
                                    y,
                                    intensity
                            );

                    importedDetections.add(
                            detection
                    );

                    Track track =
                            importedTrackMap.get(
                                    particle
                            );

                    if (track == null) {

                        track =
                                new Track(
                                        particle,
                                        detection
                                );

                        importedTrackMap.put(
                                particle,
                                track
                        );

                    } else {

                        track.addDetection(
                                detection
                        );
                    }

                    importedRows++;

                } catch (NumberFormatException rowException) {
                    /*
                     * 当前行格式错误时跳过，
                     * 不中断整个文件导入。
                     */
                }
            }
        }

        if (importedTrackMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "没有从CSV中读取到有效轨迹。"
            );
        }

        List<Track> importedTracks =
                new ArrayList<>(
                        importedTrackMap.values()
                );

        importedTracks.sort(
                Comparator.comparingInt(
                        track -> track.id
                )
        );

        for (Track track : importedTracks) {
            track.detections.sort(
                    Comparator.comparingInt(
                            detection -> detection.frame
                    )
            );
        }

        return new ImportResult(
                importedDetections,
                importedTracks,
                importedRows,
                intensityColumnFound
        );
    }

    private static Map<String, Integer> buildColumnMap(
            String[] headers
    ) {
        Map<String, Integer> columnMap =
                new HashMap<>();

        for (int index = 0;
             index < headers.length;
             index++) {

            String normalizedHeader =
                    cleanValue(
                            headers[index]
                    ).toLowerCase();

            columnMap.put(
                    normalizedHeader,
                    index
            );
        }

        return columnMap;
    }

    private static void validateRequiredColumns(
            Map<String, Integer> columnMap
    ) {
        if (!columnMap.containsKey("particle")
                || !columnMap.containsKey("frame")
                || !columnMap.containsKey("x")
                || !columnMap.containsKey("y")) {

            throw new IllegalArgumentException(
                    "CSV缺少必须列。必须包含：particle、frame、x、y。"
            );
        }
    }

    private static Integer findIntensityColumn(
            Map<String, Integer> columnMap
    ) {
        if (columnMap.containsKey("intensity")) {
            return columnMap.get("intensity");
        }

        if (columnMap.containsKey("mass")) {
            return columnMap.get("mass");
        }

        if (columnMap.containsKey("signal")) {
            return columnMap.get("signal");
        }

        return null;
    }

    private static String cleanValue(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("\"", "");
    }

    /**
     * CSV导入结果。
     *
     * @param detections            导入的所有检测点
     * @param tracks                导入的所有轨迹
     * @param importedRows          有效数据行数
     * @param intensityColumnFound  是否找到了强度列
     */
    public record ImportResult(
            List<Detection> detections,
            List<Track> tracks,
            int importedRows,
            boolean intensityColumnFound
    ) {
    }
}