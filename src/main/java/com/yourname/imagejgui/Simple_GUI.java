package com.yourname.imagejgui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.DirectoryChooser;
import ij.plugin.FolderOpener;
import ij.gui.Overlay;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Simple_GUI implements PlugIn {

    private final Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 16);
    private final Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 22);

    private JComboBox<String> denoiseMethodBox;
    private JTextField denoiseParameterField;

    private JComboBox<String> detectionMethodBox;
    private JComboBox<String> exportTypeBox;
    private JCheckBox invertSequenceCheckBox;

    private JTextField detectionThresholdField;
    private JTextField localMaxRadiusField;
    private JTextField minDistanceField;
    private JTextField trackingMaxDistanceField;

    private final List<Detection> lastDetections = new ArrayList<>();
    private final List<Track> lastTracks = new ArrayList<>();


    @Override
    public void run(String arg) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {

        JFrame frame = new JFrame("My ImageJ GUI Plugin");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1300, 750);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("单颗粒分子识别 GUI Demo", SwingConstants.CENTER);
        title.setFont(titleFont);

        JButton generateButton = new JButton("生成测试图像");
        JButton importSequenceButton = new JButton("导入图像序列");
        JButton imageButton = new JButton("检查当前图像");
        JButton importCSVButton = new JButton("导入CSV轨迹");
        JButton denoiseButton = new JButton("执行降噪");
        JButton detectButton = new JButton("识别颗粒");
        JButton trackButton = new JButton("简单追踪");
        JButton analyzeButton = new JButton("轨迹统计");
        JButton msdButton = new JButton("计算 MSD");
        JButton plotMSDButton = new JButton("绘制MSD");
        JButton diffusionButton = new JButton("计算扩散系数D");
        JButton exportButton = new JButton("导出结果");
        JButton closeButton = new JButton("关闭");

        generateButton.setFont(chineseFont);
        importSequenceButton.setFont(chineseFont);
        imageButton.setFont(chineseFont);
        importCSVButton.setFont(chineseFont);
        imageButton.setFont(chineseFont);
        denoiseButton.setFont(chineseFont);
        detectButton.setFont(chineseFont);
        trackButton.setFont(chineseFont);
        analyzeButton.setFont(chineseFont);
        msdButton.setFont(chineseFont);
        plotMSDButton.setFont(chineseFont);
        diffusionButton.setFont(chineseFont);
        exportButton.setFont(chineseFont);
        closeButton.setFont(chineseFont);

        invertSequenceCheckBox = new JCheckBox(
                "导入时反相（适用于暗颗粒）",
                true
        );
        invertSequenceCheckBox.setFont(chineseFont);
        denoiseMethodBox = new JComboBox<>(new String[]{
                "Gaussian Blur 高斯滤波",
                "Median Filter 中值滤波"
        });
        denoiseMethodBox.setFont(chineseFont);

        denoiseParameterField = new JTextField("1.0", 6);
        denoiseParameterField.setFont(chineseFont);

        detectionMethodBox = new JComboBox<>(new String[]{
                "Local Maximum 局部极大值",
                "Centroid 质心定位"
        });
        detectionMethodBox.setFont(chineseFont);
        exportTypeBox = new JComboBox<>(new String[]{
                "Track Results 轨迹坐标",
                "Track Summary 轨迹统计",
                "MSD Results MSD结果",
                "Ensemble MSD 总体平均MSD"
        });
        exportTypeBox.setFont(chineseFont);
        detectionThresholdField = new JTextField("80", 6);
        detectionThresholdField.setFont(chineseFont);

        localMaxRadiusField = new JTextField("2", 6);
        localMaxRadiusField.setFont(chineseFont);

        minDistanceField = new JTextField("6", 6);
        minDistanceField.setFont(chineseFont);
        trackingMaxDistanceField = new JTextField("10", 6);
        trackingMaxDistanceField.setFont(chineseFont);

        JLabel denoiseMethodLabel = new JLabel("降噪方法：");
        JLabel denoiseParameterLabel = new JLabel("降噪参数：");
        JLabel detectionMethodLabel = new JLabel("识别方法：");
        JLabel thresholdLabel = new JLabel("识别阈值：");
        JLabel radiusLabel = new JLabel("局部极大半径：");
        JLabel minDistanceLabel = new JLabel("最小距离：");
        JLabel trackingDistanceLabel = new JLabel("追踪最大距离：");
        JLabel exportTypeLabel = new JLabel("导出类型：");

        denoiseMethodLabel.setFont(chineseFont);
        denoiseParameterLabel.setFont(chineseFont);
        detectionMethodLabel.setFont(chineseFont);
        exportTypeLabel.setFont(chineseFont);
        thresholdLabel.setFont(chineseFont);
        radiusLabel.setFont(chineseFont);
        minDistanceLabel.setFont(chineseFont);
        trackingDistanceLabel.setFont(chineseFont);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        generateButton.addActionListener(e -> generateTestImage(logArea));

        importSequenceButton.addActionListener(e -> importImageSequence(logArea));

        imageButton.addActionListener(e -> readCurrentImage(logArea));

        importCSVButton.addActionListener(e -> importTrackingCSV(logArea));

        denoiseButton.addActionListener(e -> denoiseCurrentImage(logArea));

        detectButton.addActionListener(e -> detectParticles(logArea));

        trackButton.addActionListener(e -> trackParticles(logArea));

        plotMSDButton.addActionListener(e -> plotMSD(logArea));

        analyzeButton.addActionListener(e -> analyzeTracks(logArea));

        msdButton.addActionListener(e -> calculateMSD(logArea));

        diffusionButton.addActionListener(e -> calculateDiffusionCoefficient(logArea));

        exportButton.addActionListener(e -> exportSelectedResults(logArea));

        closeButton.addActionListener(e -> frame.dispose());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(6, 1, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel row1 = new JPanel();
        row1.add(generateButton);
        row1.add(importSequenceButton);
        row1.add(imageButton);
        row1.add(importCSVButton);
        row1.add(denoiseButton);
        row1.add(invertSequenceCheckBox);

        JPanel importRow = new JPanel();
        importRow.add(importSequenceButton);
        importRow.add(importCSVButton);
        importRow.add(invertSequenceCheckBox);

        JPanel row2 = new JPanel();
        row2.add(denoiseMethodLabel);
        row2.add(denoiseMethodBox);
        row2.add(denoiseParameterLabel);
        row2.add(denoiseParameterField);

        JPanel row3 = new JPanel();
        row3.add(detectionMethodLabel);
        row3.add(detectionMethodBox);
        row3.add(thresholdLabel);
        row3.add(detectionThresholdField);
        row3.add(radiusLabel);
        row3.add(localMaxRadiusField);
        row3.add(minDistanceLabel);
        row3.add(minDistanceField);
        row3.add(trackingDistanceLabel);
        row3.add(trackingMaxDistanceField);

        JPanel row4 = new JPanel();
        row4.add(detectButton);
        row4.add(trackButton);
        row4.add(analyzeButton);
        row4.add(msdButton);
        row4.add(plotMSDButton);
        row4.add(diffusionButton);
        row4.add(exportButton);
        row4.add(closeButton);

        JPanel row5 = new JPanel();
        row5.add(exportTypeLabel);
        row5.add(exportTypeBox);


        controlPanel.add(row1);
        controlPanel.add(importRow);
        controlPanel.add(row2);
        controlPanel.add(row3);
        controlPanel.add(row4);
        controlPanel.add(row5);

        frame.setLayout(new BorderLayout());
        frame.add(title, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.CENTER);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(800, 150));
        logScrollPane.setBorder(BorderFactory.createTitledBorder("日志区域"));

        frame.add(logScrollPane, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void importImageSequence(JTextArea logArea) {
        try {
            DirectoryChooser directoryChooser =
                    new DirectoryChooser("选择 bulk_water 图像序列文件夹");

            String directory = directoryChooser.getDirectory();

            if (directory == null) {
                logArea.append("已取消导入图像序列。\n\n");
                return;
            }

            ImagePlus sourceSequence = FolderOpener.open(directory);

            if (sourceSequence == null
                    || sourceSequence.getStackSize() == 0) {

                logArea.append("没有从所选文件夹读取到图像。\n\n");
                return;
            }

            ImageStack sourceStack = sourceSequence.getStack();

            int width = sourceSequence.getWidth();
            int height = sourceSequence.getHeight();
            int totalFrames = sourceStack.getSize();

            ImageStack preparedStack = new ImageStack(width, height);

            boolean invertImage = invertSequenceCheckBox.isSelected();

            for (int frame = 1; frame <= totalFrames; frame++) {

                ImageProcessor sourceProcessor =
                        sourceStack.getProcessor(frame);

                /*
                 * 转成8位灰度。
                 * false 表示不要对每一帧单独进行强度拉伸，
                 * 避免不同帧被分别归一化。
                 */
                ImageProcessor preparedProcessor =
                        sourceProcessor.convertToByteProcessor(false);

                if (invertImage) {
                    preparedProcessor.invert();
                }

                String sliceLabel = sourceStack.getSliceLabel(frame);

                preparedStack.addSlice(
                        sliceLabel,
                        preparedProcessor
                );

                IJ.showProgress(frame, totalFrames);
            }

            ImagePlus preparedSequence = new ImagePlus(
                    "Bulk Water - Prepared",
                    preparedStack
            );

            preparedSequence.setDimensions(
                    1,
                    1,
                    totalFrames
            );

            preparedSequence.setOpenAsHyperStack(true);
            preparedSequence.setDisplayRange(0, 255);
            preparedSequence.show();

            /*
             * 导入新数据后，清空之前图像产生的检测和追踪结果，
             * 避免旧结果被错误用于新图像。
             */
            lastDetections.clear();
            lastTracks.clear();

            logArea.append("图像序列导入完成。\n");
            logArea.append("文件夹：" + directory + "\n");
            logArea.append("图像名称："
                    + preparedSequence.getTitle() + "\n");
            logArea.append("宽度：" + width + "\n");
            logArea.append("高度：" + height + "\n");
            logArea.append("帧数：" + totalFrames + "\n");
            logArea.append(
                    "导入时反相：" +
                            (invertImage ? "是" : "否") +
                            "\n"
            );
            logArea.append(
                    "之前的识别和追踪结果已清空。\n"
            );
            logArea.append(
                    "下一步可点击：检查当前图像 → 执行降噪 → 识别颗粒。\n\n"
            );

        } catch (Exception ex) {
            logArea.append(
                    "图像序列导入失败：" +
                            ex.getMessage() +
                            "\n\n"
            );
        }
    }

    private void readCurrentImage(JTextArea logArea) {
        try {
            ImagePlus imp = IJ.getImage();

            logArea.append("当前图像：" + imp.getTitle() + "\n");
            logArea.append("宽度：" + imp.getWidth() + "\n");
            logArea.append("高度：" + imp.getHeight() + "\n");
            logArea.append("切片/帧数：" + imp.getStackSize() + "\n\n");

        } catch (Exception ex) {
            logArea.append("请先在 Fiji/ImageJ 中打开一张图像。\n\n");
        }
    }

    private void importTrackingCSV(JTextArea logArea) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择外部追踪 CSV 文件");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            logArea.append("已取消导入。\n\n");
            return;
        }

        File csvFile = fileChooser.getSelectedFile();

        Map<Integer, Track> importedTrackMap = new HashMap<>();
        List<Detection> importedDetections = new ArrayList<>();

        int importedRows = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {

            String headerLine = reader.readLine();

            if (headerLine == null || headerLine.trim().isEmpty()) {
                logArea.append("CSV 文件为空。\n\n");
                return;
            }

            String[] headers = headerLine.split(",");

            Map<String, Integer> columnMap = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {
                String normalizedHeader = headers[i]
                        .trim()
                        .replace("\"", "")
                        .toLowerCase();

                columnMap.put(normalizedHeader, i);
            }

            if (!columnMap.containsKey("particle")
                    || !columnMap.containsKey("frame")
                    || !columnMap.containsKey("x")
                    || !columnMap.containsKey("y")) {

                logArea.append(
                        "CSV 缺少必须列。\n" +
                                "必须包含：particle、frame、x、y\n\n"
                );
                return;
            }

            int particleColumn = columnMap.get("particle");
            int frameColumn = columnMap.get("frame");
            int xColumn = columnMap.get("x");
            int yColumn = columnMap.get("y");

            Integer intensityColumn = null;

            if (columnMap.containsKey("intensity")) {
                intensityColumn = columnMap.get("intensity");
            } else if (columnMap.containsKey("mass")) {
                intensityColumn = columnMap.get("mass");
            } else if (columnMap.containsKey("signal")) {
                intensityColumn = columnMap.get("signal");
            }

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");

                int requiredMaximumColumn = Math.max(
                        Math.max(particleColumn, frameColumn),
                        Math.max(xColumn, yColumn)
                );

                if (values.length <= requiredMaximumColumn) {
                    continue;
                }

                try {
                    int particle = (int) Double.parseDouble(
                            values[particleColumn].trim().replace("\"", "")
                    );

                    int frame = (int) Double.parseDouble(
                            values[frameColumn].trim().replace("\"", "")
                    );

                    double x = Double.parseDouble(
                            values[xColumn].trim().replace("\"", "")
                    );

                    double y = Double.parseDouble(
                            values[yColumn].trim().replace("\"", "")
                    );

                    double intensity = 0.0;

                    if (intensityColumn != null && values.length > intensityColumn) {
                        String intensityText = values[intensityColumn]
                                .trim()
                                .replace("\"", "");

                        if (!intensityText.isEmpty()) {
                            intensity = Double.parseDouble(intensityText);
                        }
                    }

                    Detection detection = new Detection(
                            frame,
                            x,
                            y,
                            intensity
                    );

                    importedDetections.add(detection);

                    Track track = importedTrackMap.get(particle);

                    if (track == null) {
                        track = new Track(particle, detection);
                        importedTrackMap.put(particle, track);
                    } else {
                        track.addDetection(detection);
                    }

                    importedRows++;

                } catch (NumberFormatException rowException) {
                    // 当前行格式错误时跳过，不中断整个文件导入。
                }
            }

            if (importedTrackMap.isEmpty()) {
                logArea.append("没有从 CSV 中读取到有效轨迹。\n\n");
                return;
            }

            List<Track> importedTracks =
                    new ArrayList<>(importedTrackMap.values());

            importedTracks.sort(Comparator.comparingInt(track -> track.id));

            for (Track track : importedTracks) {
                track.detections.sort(
                        Comparator.comparingInt(detection -> detection.frame)
                );
            }

            lastDetections.clear();
            lastDetections.addAll(importedDetections);

            lastTracks.clear();
            lastTracks.addAll(importedTracks);

            showImportedTrackResults();

            logArea.append("外部追踪 CSV 导入完成。\n");
            logArea.append("文件：" + csvFile.getAbsolutePath() + "\n");
            logArea.append("有效数据行数：" + importedRows + "\n");
            logArea.append("particle 数量：" + lastTracks.size() + "\n");
            logArea.append("坐标点数量：" + lastDetections.size() + "\n");

            if (intensityColumn == null) {
                logArea.append("未找到 mass、signal 或 Intensity 列，强度统一记为 0。\n");
            }

            logArea.append(
                    "现在可以直接点击：轨迹统计、计算 MSD、绘制MSD、计算D或导出结果。\n\n"
            );

        } catch (Exception ex) {
            logArea.append("CSV 导入失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void showImportedTrackResults() {
        ResultsTable trackTable = new ResultsTable();

        for (Track track : lastTracks) {
            for (Detection detection : track.detections) {
                trackTable.incrementCounter();
                trackTable.addValue("particle", track.id);
                trackTable.addValue("frame", detection.frame);
                trackTable.addValue("x", detection.x);
                trackTable.addValue("y", detection.y);
                trackTable.addValue("Intensity", detection.intensity);
            }
        }

        trackTable.show("Imported Track Results");
    }
    private void denoiseCurrentImage(JTextArea logArea) {
        try {
            ImagePlus original = IJ.getImage();

            if (original == null) {
                logArea.append("没有检测到当前图像，请先打开或生成一张图像。\n\n");
                return;
            }

            String method = (String) denoiseMethodBox.getSelectedItem();
            double parameter = Double.parseDouble(denoiseParameterField.getText());

            if (parameter <= 0) {
                logArea.append("降噪参数必须大于 0。\n\n");
                return;
            }

            ImagePlus denoised = original.duplicate();
            denoised.setTitle(original.getTitle() + " - Denoised");

            ImageStack stack = denoised.getStack();
            int totalSlices = stack.getSize();

            for (int s = 1; s <= totalSlices; s++) {

                ImageProcessor ip = stack.getProcessor(s);

                if (method.contains("Gaussian")) {
                    ip.blurGaussian(parameter);
                } else if (method.contains("Median")) {
                    RankFilters rankFilters = new RankFilters();
                    rankFilters.rank(ip, parameter, RankFilters.MEDIAN);
                }

                IJ.showProgress(s, totalSlices);
            }

            denoised.show();

            logArea.append("降噪完成。\n");
            logArea.append("原始图像：" + original.getTitle() + "\n");
            logArea.append("降噪方法：" + method + "\n");
            logArea.append("参数：" + parameter + "\n");
            logArea.append("处理帧数：" + totalSlices + "\n");
            logArea.append("已生成新图像：" + denoised.getTitle() + "\n\n");

        } catch (NumberFormatException ex) {
            logArea.append("参数输入错误，请输入数字，例如 1.0 或 2.0。\n\n");
        } catch (Exception ex) {
            logArea.append("降噪失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void detectParticles(JTextArea logArea) {
        try {
            ImagePlus imp = IJ.getImage();

            if (imp == null) {
                logArea.append("没有检测到当前图像，请先打开或生成一张图像。\n\n");
                return;
            }

            double threshold = Double.parseDouble(detectionThresholdField.getText());
            int localRadius = Integer.parseInt(localMaxRadiusField.getText());
            double minDistance = Double.parseDouble(minDistanceField.getText());
            String detectionMethod = (String) detectionMethodBox.getSelectedItem();

            if (threshold <= 0) {
                logArea.append("识别阈值必须大于 0。\n\n");
                return;
            }

            if (localRadius < 1) {
                logArea.append("局部极大半径至少为 1。\n\n");
                return;
            }

            if (minDistance < 1) {
                logArea.append("最小距离至少为 1。\n\n");
                return;
            }

            lastDetections.clear();

            ImageStack stack = imp.getStack();
            int totalSlices = stack.getSize();

            Overlay overlay = new Overlay();
            ResultsTable resultsTable = new ResultsTable();

            for (int frame = 1; frame <= totalSlices; frame++) {

                ImageProcessor ip = stack.getProcessor(frame);
                FloatProcessor fp = ip.convertToFloatProcessor();

                List<Detection> candidates;

                if (detectionMethod.contains("Centroid")) {
                    candidates = findCentroidDetections(
                            fp,
                            frame,
                            threshold
                    );
                } else {
                    candidates = findLocalMaxima(
                            fp,
                            frame,
                            threshold,
                            localRadius
                    );
                }

                candidates.sort(
                        Comparator.comparingDouble((Detection d) -> d.intensity).reversed()
                );

                List<Detection> acceptedInThisFrame = new ArrayList<>();

                for (Detection candidate : candidates) {

                    boolean tooClose = false;

                    for (Detection accepted : acceptedInThisFrame) {
                        double distance = distance(
                                candidate.x,
                                candidate.y,
                                accepted.x,
                                accepted.y
                        );

                        if (distance < minDistance) {
                            tooClose = true;
                            break;
                        }
                    }

                    if (!tooClose) {
                        acceptedInThisFrame.add(candidate);
                        lastDetections.add(candidate);

                        resultsTable.incrementCounter();
                        resultsTable.addValue("Frame", candidate.frame);
                        resultsTable.addValue("X", candidate.x);
                        resultsTable.addValue("Y", candidate.y);
                        resultsTable.addValue("Intensity", candidate.intensity);

                        OvalRoi roi = new OvalRoi(
                                candidate.x - 4,
                                candidate.y - 4,
                                8,
                                8
                        );

                        roi.setStrokeColor(Color.RED);
                        roi.setPosition(frame);
                        overlay.add(roi);
                    }
                }

                IJ.showProgress(frame, totalSlices);
            }

            imp.setOverlay(overlay);
            resultsTable.show("Particle Detections");

            logArea.append("颗粒识别完成。\n");
            logArea.append("图像：" + imp.getTitle() + "\n");
            logArea.append("识别方法：" + detectionMethod + "\n");
            logArea.append("识别阈值：" + threshold + "\n");
            logArea.append("局部极大半径：" + localRadius + "\n");
            logArea.append("最小距离：" + minDistance + "\n");
            logArea.append("总帧数：" + totalSlices + "\n");
            logArea.append("检测到颗粒总数：" + lastDetections.size() + "\n");
            logArea.append("结果已显示在 Particle Detections 表格中。\n\n");

        } catch (NumberFormatException ex) {
            logArea.append("识别参数输入错误，请输入数字。\n\n");
        } catch (Exception ex) {
            logArea.append("颗粒识别失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void trackParticles(JTextArea logArea) {
        try {
            logArea.append("开始执行简单追踪。\n");

            if (lastDetections.isEmpty()) {
                logArea.append("还没有颗粒识别结果，请先点击“识别颗粒”。\n\n");
                return;
            }

            double maxLinkDistance = Double.parseDouble(trackingMaxDistanceField.getText());

            if (maxLinkDistance <= 0) {
                logArea.append("追踪最大距离必须大于 0。\n\n");
                return;
            }

            ImagePlus imp = IJ.getImage();

            if (imp == null) {
                logArea.append("没有检测到当前图像。\n\n");
                return;
            }

            Map<Integer, List<Detection>> detectionsByFrame = new HashMap<>();

            int maxFrame = 0;

            for (Detection detection : lastDetections) {
                detectionsByFrame
                        .computeIfAbsent(detection.frame, k -> new ArrayList<>())
                        .add(detection);

                if (detection.frame > maxFrame) {
                    maxFrame = detection.frame;
                }
            }

            List<Track> allTracks = new ArrayList<>();
            int nextTrackId = 0;

            for (int frame = 1; frame <= maxFrame; frame++) {

                List<Detection> currentDetections =
                        detectionsByFrame.getOrDefault(frame, new ArrayList<>());

                if (frame == 1) {
                    for (Detection detection : currentDetections) {
                        Track track = new Track(nextTrackId, detection);
                        allTracks.add(track);
                        nextTrackId++;
                    }
                    continue;
                }

                List<Track> previousFrameTracks = new ArrayList<>();

                for (Track track : allTracks) {
                    Detection last = track.getLastDetection();

                    if (last.frame == frame - 1) {
                        previousFrameTracks.add(track);
                    }
                }

                List<LinkCandidate> candidates = new ArrayList<>();

                for (Track track : previousFrameTracks) {
                    Detection last = track.getLastDetection();

                    for (Detection detection : currentDetections) {
                        double distance = distance(
                                last.x,
                                last.y,
                                detection.x,
                                detection.y
                        );

                        if (distance <= maxLinkDistance) {
                            candidates.add(
                                    new LinkCandidate(
                                            track,
                                            detection,
                                            distance
                                    )
                            );
                        }
                    }
                }

                candidates.sort(Comparator.comparingDouble(c -> c.distance));

                Set<Track> matchedTracks = new HashSet<>();
                Set<Detection> matchedDetections = new HashSet<>();

                for (LinkCandidate candidate : candidates) {

                    if (matchedTracks.contains(candidate.track)) {
                        continue;
                    }

                    if (matchedDetections.contains(candidate.detection)) {
                        continue;
                    }

                    candidate.track.addDetection(candidate.detection);

                    matchedTracks.add(candidate.track);
                    matchedDetections.add(candidate.detection);
                }

                for (Detection detection : currentDetections) {
                    if (!matchedDetections.contains(detection)) {
                        Track newTrack = new Track(nextTrackId, detection);
                        allTracks.add(newTrack);
                        nextTrackId++;
                    }
                }
            }

            ResultsTable trackTable = new ResultsTable();

            Overlay overlay = imp.getOverlay();

            if (overlay == null) {
                overlay = new Overlay();
            }

            for (Track track : allTracks) {

                List<Detection> points = track.detections;

                for (Detection detection : points) {
                    trackTable.incrementCounter();
                    trackTable.addValue("particle", track.id);
                    trackTable.addValue("Frame", detection.frame);
                    trackTable.addValue("X", detection.x);
                    trackTable.addValue("Y", detection.y);
                    trackTable.addValue("Intensity", detection.intensity);
                }

                for (int i = 1; i < points.size(); i++) {

                    Detection previous = points.get(i - 1);
                    Detection current = points.get(i);

                    Line line = new Line(
                            previous.x,
                            previous.y,
                            current.x,
                            current.y
                    );

                    line.setStrokeColor(Color.GREEN);
                    line.setStrokeWidth(2);
                    line.setPosition(0);

                    overlay.add(line);
                }
            }

            imp.setOverlay(overlay);
            trackTable.show("Track Results");

            lastTracks.clear();
            lastTracks.addAll(allTracks);

            logArea.append("简单追踪完成。\n");
            logArea.append("识别点总数：" + lastDetections.size() + "\n");
            logArea.append("追踪最大距离：" + maxLinkDistance + "\n");
            logArea.append("生成轨迹数量：" + allTracks.size() + "\n");
            logArea.append("结果已显示在 Track Results 表格中。\n");
            logArea.append("绿色线段表示相邻帧之间的连接。\n\n");

        } catch (NumberFormatException ex) {
            logArea.append("追踪参数输入错误，请输入数字，例如 10。\n\n");
        } catch (Exception ex) {
            logArea.append("追踪失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void analyzeTracks(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            ResultsTable summaryTable = new ResultsTable();

            for (Track track : lastTracks) {

                List<Detection> points = track.detections;

                if (points.isEmpty()) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                Detection first = points.get(0);
                Detection last = points.get(points.size() - 1);

                int startFrame = first.frame;
                int endFrame = last.frame;
                int numberOfPoints = points.size();
                int duration = endFrame - startFrame + 1;

                double displacement = distance(
                        first.x,
                        first.y,
                        last.x,
                        last.y
                );

                double pathLength = 0.0;
                double intensitySum = 0.0;

                for (Detection detection : points) {
                    intensitySum += detection.intensity;
                }

                for (int i = 1; i < points.size(); i++) {
                    Detection previous = points.get(i - 1);
                    Detection current = points.get(i);

                    pathLength += distance(
                            previous.x,
                            previous.y,
                            current.x,
                            current.y
                    );
                }

                double meanIntensity = intensitySum / numberOfPoints;

                double meanStep = 0.0;
                if (numberOfPoints > 1) {
                    meanStep = pathLength / (numberOfPoints - 1);
                }

                double meanSpeed = 0.0;
                if (duration > 1) {
                    meanSpeed = pathLength / (duration - 1);
                }

                summaryTable.incrementCounter();
                summaryTable.addValue("particle", track.id);
                summaryTable.addValue("Start_Frame", startFrame);
                summaryTable.addValue("End_Frame", endFrame);
                summaryTable.addValue("N_Points", numberOfPoints);
                summaryTable.addValue("Duration_Frames", duration);
                summaryTable.addValue("Start_X", first.x);
                summaryTable.addValue("Start_Y", first.y);
                summaryTable.addValue("End_X", last.x);
                summaryTable.addValue("End_Y", last.y);
                summaryTable.addValue("Displacement", displacement);
                summaryTable.addValue("Path_Length", pathLength);
                summaryTable.addValue("Mean_Step", meanStep);
                summaryTable.addValue("Mean_Speed_px_per_frame", meanSpeed);
                summaryTable.addValue("Mean_Intensity", meanIntensity);
            }

            summaryTable.show("Track Summary");

            logArea.append("轨迹统计完成。\n");
            logArea.append("轨迹数量：" + lastTracks.size() + "\n");
            logArea.append("结果已显示在 Track Summary 表格中。\n");
            logArea.append("统计指标包括：轨迹长度、净位移、路径长度、平均步长、平均速度、平均强度。\n\n");

        } catch (Exception ex) {
            logArea.append("轨迹统计失败：" + ex.getMessage() + "\n\n");
        }
    }
    private void calculateMSD(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            ResultsTable msdTable = new ResultsTable();
            ResultsTable ensembleTable = new ResultsTable();

            Map<Integer, Double> ensembleSumByLag = new HashMap<>();
            Map<Integer, Integer> ensembleCountByLag = new HashMap<>();

            int totalMSDRows = 0;

            for (Track track : lastTracks) {

                List<Detection> points = track.detections;

                if (points.size() < 2) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                int maxLag = points.size() - 1;

                for (int lag = 1; lag <= maxLag; lag++) {

                    double sumSquaredDisplacement = 0.0;
                    int pairCount = 0;

                    for (int i = 0; i < points.size() - lag; i++) {

                        Detection p1 = points.get(i);
                        Detection p2 = points.get(i + lag);

                        double dx = p2.x - p1.x;
                        double dy = p2.y - p1.y;

                        double squaredDisplacement = dx * dx + dy * dy;

                        sumSquaredDisplacement += squaredDisplacement;
                        pairCount++;

                        ensembleSumByLag.put(
                                lag,
                                ensembleSumByLag.getOrDefault(lag, 0.0) + squaredDisplacement
                        );

                        ensembleCountByLag.put(
                                lag,
                                ensembleCountByLag.getOrDefault(lag, 0) + 1
                        );
                    }

                    if (pairCount > 0) {
                        double msd = sumSquaredDisplacement / pairCount;

                        msdTable.incrementCounter();
                        msdTable.addValue("particle", track.id);
                        msdTable.addValue("Lag_Frames", lag);
                        msdTable.addValue("MSD_px2", msd);
                        msdTable.addValue("N_Pairs", pairCount);

                        totalMSDRows++;
                    }
                }
            }

            for (Integer lag : ensembleSumByLag.keySet()) {
                double sum = ensembleSumByLag.get(lag);
                int count = ensembleCountByLag.get(lag);

                if (count > 0) {
                    double ensembleMSD = sum / count;

                    ensembleTable.incrementCounter();
                    ensembleTable.addValue("Lag_Frames", lag);
                    ensembleTable.addValue("Ensemble_MSD_px2", ensembleMSD);
                    ensembleTable.addValue("N_Pairs", count);
                }
            }

            msdTable.show("MSD Results");
            ensembleTable.show("MSD Ensemble");

            logArea.append("MSD 计算完成。\n");
            logArea.append("轨迹数量：" + lastTracks.size() + "\n");
            logArea.append("MSD 结果行数：" + totalMSDRows + "\n");
            logArea.append("已生成 MSD Results 表格：每条轨迹的 MSD。\n");
            logArea.append("已生成 MSD Ensemble 表格：所有轨迹的平均 MSD。\n\n");

        } catch (Exception ex) {
            logArea.append("MSD 计算失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void plotMSD(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”，再点击“计算 MSD”。\n\n");
                return;
            }

            Map<Integer, Double> ensembleSumByLag = new HashMap<>();
            Map<Integer, Integer> ensembleCountByLag = new HashMap<>();

            for (Track track : lastTracks) {
                List<Detection> points = track.detections;

                if (points.size() < 2) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                int maxLag = points.size() - 1;

                for (int lag = 1; lag <= maxLag; lag++) {
                    for (int i = 0; i < points.size() - lag; i++) {
                        Detection p1 = points.get(i);
                        Detection p2 = points.get(i + lag);

                        double dx = p2.x - p1.x;
                        double dy = p2.y - p1.y;
                        double squaredDisplacement = dx * dx + dy * dy;

                        ensembleSumByLag.put(
                                lag,
                                ensembleSumByLag.getOrDefault(lag, 0.0) + squaredDisplacement
                        );

                        ensembleCountByLag.put(
                                lag,
                                ensembleCountByLag.getOrDefault(lag, 0) + 1
                        );
                    }
                }
            }

            List<Integer> lags = new ArrayList<>(ensembleSumByLag.keySet());
            lags.sort(Integer::compareTo);

            double[] x = new double[lags.size()];
            double[] y = new double[lags.size()];

            for (int i = 0; i < lags.size(); i++) {
                int lag = lags.get(i);
                x[i] = lag;
                y[i] = ensembleSumByLag.get(lag) / ensembleCountByLag.get(lag);
            }

            ij.gui.Plot plot = new ij.gui.Plot(
                    "Ensemble MSD Curve",
                    "Lag Frames",
                    "MSD px^2",
                    x,
                    y
            );

            plot.show();

            logArea.append("MSD曲线绘制完成。\n");
            logArea.append("曲线类型：Ensemble MSD\n");
            logArea.append("数据点数量：" + lags.size() + "\n\n");

        } catch (Exception ex) {
            logArea.append("MSD绘图失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void calculateDiffusionCoefficient(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            Map<Integer, Double> ensembleSumByLag = new HashMap<>();
            Map<Integer, Integer> ensembleCountByLag = new HashMap<>();

            for (Track track : lastTracks) {
                List<Detection> points = track.detections;

                if (points.size() < 2) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                int maxLag = points.size() - 1;

                for (int lag = 1; lag <= maxLag; lag++) {
                    for (int i = 0; i < points.size() - lag; i++) {
                        Detection p1 = points.get(i);
                        Detection p2 = points.get(i + lag);

                        double dx = p2.x - p1.x;
                        double dy = p2.y - p1.y;
                        double squaredDisplacement = dx * dx + dy * dy;

                        ensembleSumByLag.put(
                                lag,
                                ensembleSumByLag.getOrDefault(lag, 0.0) + squaredDisplacement
                        );

                        ensembleCountByLag.put(
                                lag,
                                ensembleCountByLag.getOrDefault(lag, 0) + 1
                        );
                    }
                }
            }

            List<Integer> lags = new ArrayList<>(ensembleSumByLag.keySet());
            lags.sort(Integer::compareTo);

            int fitPoints = Math.min(5, lags.size());

            if (fitPoints < 2) {
                logArea.append("MSD点数不足，无法拟合扩散系数。\n\n");
                return;
            }

            double sumX = 0.0;
            double sumY = 0.0;
            double sumXY = 0.0;
            double sumXX = 0.0;

            for (int i = 0; i < fitPoints; i++) {
                int lag = lags.get(i);
                double x = lag;
                double y = ensembleSumByLag.get(lag) / ensembleCountByLag.get(lag);

                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumXX += x * x;
            }

            double n = fitPoints;
            double denominator = n * sumXX - sumX * sumX;

            if (denominator == 0) {
                logArea.append("拟合失败：分母为0。\n\n");
                return;
            }

            double slope = (n * sumXY - sumX * sumY) / denominator;
            double intercept = (sumY - slope * sumX) / n;

            double diffusionCoefficient = slope / 4.0;

            ResultsTable diffusionTable = new ResultsTable();
            diffusionTable.incrementCounter();
            diffusionTable.addValue("Fit_Points", fitPoints);
            diffusionTable.addValue("Slope_px2_per_frame", slope);
            diffusionTable.addValue("Intercept_px2", intercept);
            diffusionTable.addValue("Diffusion_Coefficient_D_px2_per_frame", diffusionCoefficient);
            diffusionTable.show("Diffusion Coefficient");

            logArea.append("扩散系数计算完成。\n");
            logArea.append("拟合模型：MSD = slope × lag + intercept\n");
            logArea.append("使用前 " + fitPoints + " 个 MSD 点拟合。\n");
            logArea.append("Slope = " + slope + " px²/frame\n");
            logArea.append("Intercept = " + intercept + " px²\n");
            logArea.append("D = slope / 4 = " + diffusionCoefficient + " px²/frame\n\n");

        } catch (Exception ex) {
            logArea.append("扩散系数计算失败：" + ex.getMessage() + "\n\n");
        }
    }
    private void exportSelectedResults(JTextArea logArea) {
        String exportType = (String) exportTypeBox.getSelectedItem();

        if (exportType == null) {
            logArea.append("请选择导出类型。\n\n");
            return;
        }

        if (exportType.contains("Track Results")) {
            exportTrackResultsToCSV(logArea);
        } else if (exportType.contains("Track Summary")) {
            exportTrackSummaryToCSV(logArea);
        } else if (exportType.contains("MSD Results")) {
            exportMSDResultsToCSV(logArea);
        } else if (exportType.contains("Ensemble MSD")) {
            exportEnsembleMSDToCSV(logArea);
        }
    }

    private File chooseCSVFile(String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存 CSV 文件");
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".csv")) {
            fileToSave = new File(filePath + ".csv");
        }

        return fileToSave;
    }

    private void exportTrackResultsToCSV(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            File fileToSave = chooseCSVFile("track_results.csv");

            if (fileToSave == null) {
                logArea.append("已取消导出。\n\n");
                return;
            }

            PrintWriter writer = new PrintWriter(fileToSave, "UTF-8");

            writer.println("particle,Frame,X,Y,Intensity");

            int rowCount = 0;

            for (Track track : lastTracks) {
                for (Detection detection : track.detections) {
                    writer.println(
                            track.id + "," +
                                    detection.frame + "," +
                                    detection.x + "," +
                                    detection.y + "," +
                                    detection.intensity
                    );
                    rowCount++;
                }
            }

            writer.close();

            logArea.append("轨迹坐标导出完成。\n");
            logArea.append("保存路径：" + fileToSave.getAbsolutePath() + "\n");
            logArea.append("导出轨迹数量：" + lastTracks.size() + "\n");
            logArea.append("导出数据行数：" + rowCount + "\n\n");

        } catch (Exception ex) {
            logArea.append("轨迹坐标导出失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void exportTrackSummaryToCSV(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            File fileToSave = chooseCSVFile("track_summary.csv");

            if (fileToSave == null) {
                logArea.append("已取消导出。\n\n");
                return;
            }

            PrintWriter writer = new PrintWriter(fileToSave, "UTF-8");

            writer.println("particle,Start_Frame,End_Frame,N_Points,Duration_Frames,Start_X,Start_Y,End_X,End_Y,Displacement,Path_Length,Mean_Step,Mean_Speed_px_per_frame,Mean_Intensity");

            int rowCount = 0;

            for (Track track : lastTracks) {
                List<Detection> points = track.detections;

                if (points.isEmpty()) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                Detection first = points.get(0);
                Detection last = points.get(points.size() - 1);

                int startFrame = first.frame;
                int endFrame = last.frame;
                int numberOfPoints = points.size();
                int duration = endFrame - startFrame + 1;

                double displacement = distance(first.x, first.y, last.x, last.y);

                double pathLength = 0.0;
                double intensitySum = 0.0;

                for (Detection detection : points) {
                    intensitySum += detection.intensity;
                }

                for (int i = 1; i < points.size(); i++) {
                    Detection previous = points.get(i - 1);
                    Detection current = points.get(i);

                    pathLength += distance(
                            previous.x,
                            previous.y,
                            current.x,
                            current.y
                    );
                }

                double meanIntensity = intensitySum / numberOfPoints;

                double meanStep = 0.0;
                if (numberOfPoints > 1) {
                    meanStep = pathLength / (numberOfPoints - 1);
                }

                double meanSpeed = 0.0;
                if (duration > 1) {
                    meanSpeed = pathLength / (duration - 1);
                }

                writer.println(
                        track.id + "," +
                                startFrame + "," +
                                endFrame + "," +
                                numberOfPoints + "," +
                                duration + "," +
                                first.x + "," +
                                first.y + "," +
                                last.x + "," +
                                last.y + "," +
                                displacement + "," +
                                pathLength + "," +
                                meanStep + "," +
                                meanSpeed + "," +
                                meanIntensity
                );

                rowCount++;
            }

            writer.close();

            logArea.append("轨迹统计导出完成。\n");
            logArea.append("保存路径：" + fileToSave.getAbsolutePath() + "\n");
            logArea.append("导出轨迹数量：" + rowCount + "\n\n");

        } catch (Exception ex) {
            logArea.append("轨迹统计导出失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void exportMSDResultsToCSV(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            File fileToSave = chooseCSVFile("msd_results.csv");

            if (fileToSave == null) {
                logArea.append("已取消导出。\n\n");
                return;
            }

            PrintWriter writer = new PrintWriter(fileToSave, "UTF-8");

            writer.println("particle,Lag_Frames,MSD_px2,N_Pairs");

            int rowCount = 0;

            for (Track track : lastTracks) {
                List<Detection> points = track.detections;

                if (points.size() < 2) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                int maxLag = points.size() - 1;

                for (int lag = 1; lag <= maxLag; lag++) {
                    double sumSquaredDisplacement = 0.0;
                    int pairCount = 0;

                    for (int i = 0; i < points.size() - lag; i++) {
                        Detection p1 = points.get(i);
                        Detection p2 = points.get(i + lag);

                        double dx = p2.x - p1.x;
                        double dy = p2.y - p1.y;

                        double squaredDisplacement = dx * dx + dy * dy;

                        sumSquaredDisplacement += squaredDisplacement;
                        pairCount++;
                    }

                    if (pairCount > 0) {
                        double msd = sumSquaredDisplacement / pairCount;

                        writer.println(
                                track.id + "," +
                                        lag + "," +
                                        msd + "," +
                                        pairCount
                        );

                        rowCount++;
                    }
                }
            }

            writer.close();

            logArea.append("MSD 结果导出完成。\n");
            logArea.append("保存路径：" + fileToSave.getAbsolutePath() + "\n");
            logArea.append("导出 MSD 数据行数：" + rowCount + "\n\n");

        } catch (Exception ex) {
            logArea.append("MSD 结果导出失败：" + ex.getMessage() + "\n\n");
        }
    }

    private void exportEnsembleMSDToCSV(JTextArea logArea) {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append("还没有追踪结果，请先点击“简单追踪”。\n\n");
                return;
            }

            File fileToSave = chooseCSVFile("ensemble_msd.csv");

            if (fileToSave == null) {
                logArea.append("已取消导出。\n\n");
                return;
            }

            Map<Integer, Double> ensembleSumByLag = new HashMap<>();
            Map<Integer, Integer> ensembleCountByLag = new HashMap<>();

            for (Track track : lastTracks) {
                List<Detection> points = track.detections;

                if (points.size() < 2) {
                    continue;
                }

                points.sort(Comparator.comparingInt(d -> d.frame));

                int maxLag = points.size() - 1;

                for (int lag = 1; lag <= maxLag; lag++) {
                    for (int i = 0; i < points.size() - lag; i++) {
                        Detection p1 = points.get(i);
                        Detection p2 = points.get(i + lag);

                        double dx = p2.x - p1.x;
                        double dy = p2.y - p1.y;

                        double squaredDisplacement = dx * dx + dy * dy;

                        ensembleSumByLag.put(
                                lag,
                                ensembleSumByLag.getOrDefault(lag, 0.0) + squaredDisplacement
                        );

                        ensembleCountByLag.put(
                                lag,
                                ensembleCountByLag.getOrDefault(lag, 0) + 1
                        );
                    }
                }
            }

            PrintWriter writer = new PrintWriter(fileToSave, "UTF-8");

            writer.println("Lag_Frames,Ensemble_MSD_px2,N_Pairs");

            int rowCount = 0;

            List<Integer> lags = new ArrayList<>(ensembleSumByLag.keySet());
            lags.sort(Integer::compareTo);

            for (Integer lag : lags) {
                double sum = ensembleSumByLag.get(lag);
                int count = ensembleCountByLag.get(lag);

                if (count > 0) {
                    double ensembleMSD = sum / count;

                    writer.println(
                            lag + "," +
                                    ensembleMSD + "," +
                                    count
                    );

                    rowCount++;
                }
            }

            writer.close();

            logArea.append("Ensemble MSD 导出完成。\n");
            logArea.append("保存路径：" + fileToSave.getAbsolutePath() + "\n");
            logArea.append("导出数据行数：" + rowCount + "\n\n");

        } catch (Exception ex) {
            logArea.append("Ensemble MSD 导出失败：" + ex.getMessage() + "\n\n");
        }
    }
    private List<Detection> findCentroidDetections(
            FloatProcessor fp,
            int frame,
            double threshold
    ) {
        List<Detection> detections = new ArrayList<>();

        int width = fp.getWidth();
        int height = fp.getHeight();

        boolean[][] visited = new boolean[width][height];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                if (visited[x][y]) {
                    continue;
                }

                float value = fp.getf(x, y);

                if (value < threshold) {
                    continue;
                }

                List<int[]> regionPixels = new ArrayList<>();
                floodFillRegion(
                        fp,
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
                double maxIntensity = 0.0;

                for (int[] pixel : regionPixels) {
                    int px = pixel[0];
                    int py = pixel[1];

                    double intensity = fp.getf(px, py);

                    sumIntensity += intensity;
                    weightedX += px * intensity;
                    weightedY += py * intensity;

                    if (intensity > maxIntensity) {
                        maxIntensity = intensity;
                    }
                }

                if (sumIntensity <= 0) {
                    continue;
                }

                double centroidX = weightedX / sumIntensity;
                double centroidY = weightedY / sumIntensity;

                detections.add(
                        new Detection(
                                frame,
                                centroidX,
                                centroidY,
                                maxIntensity
                        )
                );
            }
        }

        return detections;
    }
    private void floodFillRegion(
            FloatProcessor fp,
            int startX,
            int startY,
            double threshold,
            boolean[][] visited,
            List<int[]> regionPixels
    ) {
        int width = fp.getWidth();
        int height = fp.getHeight();

        List<int[]> queue = new ArrayList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        int index = 0;

        while (index < queue.size()) {
            int[] current = queue.get(index);
            index++;

            int x = current[0];
            int y = current[1];

            regionPixels.add(new int[]{x, y});

            int[][] neighbors = new int[][]{
                    {x + 1, y},
                    {x - 1, y},
                    {x, y + 1},
                    {x, y - 1}
            };

            for (int[] neighbor : neighbors) {
                int nx = neighbor[0];
                int ny = neighbor[1];

                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }

                if (visited[nx][ny]) {
                    continue;
                }

                if (fp.getf(nx, ny) < threshold) {
                    continue;
                }

                visited[nx][ny] = true;
                queue.add(new int[]{nx, ny});
            }
        }
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

                if (centerValue < threshold) {
                    continue;
                }

                boolean isLocalMaximum = true;

                for (int yy = y - radius; yy <= y + radius; yy++) {
                    for (int xx = x - radius; xx <= x + radius; xx++) {

                        if (xx == x && yy == y) {
                            continue;
                        }

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

    private double distance(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private void generateTestImage(JTextArea logArea) {

        int width = 256;
        int height = 256;
        int frames = 30;
        int particles = 8;

        double sigma = 2.0;
        double amplitude = 180.0;
        double background = 20.0;
        double noiseLevel = 8.0;

        Random random = new Random(12345);

        double[] x = new double[particles];
        double[] y = new double[particles];
        double[] vx = new double[particles];
        double[] vy = new double[particles];

        for (int i = 0; i < particles; i++) {
            x[i] = 30 + random.nextDouble() * (width - 60);
            y[i] = 30 + random.nextDouble() * (height - 60);

            vx[i] = -1.5 + random.nextDouble() * 3.0;
            vy[i] = -1.5 + random.nextDouble() * 3.0;
        }

        ImageStack stack = new ImageStack(width, height);

        for (int t = 0; t < frames; t++) {

            float[] pixels = new float[width * height];

            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (float) (background + random.nextGaussian() * noiseLevel);
            }

            for (int p = 0; p < particles; p++) {

                drawGaussianSpot(
                        pixels,
                        width,
                        height,
                        x[p],
                        y[p],
                        amplitude,
                        sigma
                );

                x[p] += vx[p];
                y[p] += vy[p];

                if (x[p] < 10 || x[p] > width - 10) {
                    vx[p] = -vx[p];
                }

                if (y[p] < 10 || y[p] > height - 10) {
                    vy[p] = -vy[p];
                }
            }

            FloatProcessor fp = new FloatProcessor(width, height, pixels);
            stack.addSlice("Frame " + (t + 1), fp);
        }

        ImagePlus imp = new ImagePlus("Synthetic Single Particle Movie", stack);
        imp.setDimensions(1, 1, frames);
        imp.setOpenAsHyperStack(true);
        imp.setDisplayRange(0, 255);
        imp.show();

        logArea.append("已生成测试图像。\n");
        logArea.append("图像大小：" + width + " × " + height + "\n");
        logArea.append("帧数：" + frames + "\n");
        logArea.append("颗粒数：" + particles + "\n");
        logArea.append("说明：亮点模拟单颗粒分子，背景加入随机噪声。\n\n");
    }

    private void drawGaussianSpot(
            float[] pixels,
            int width,
            int height,
            double centerX,
            double centerY,
            double amplitude,
            double sigma
    ) {

        int radius = (int) Math.ceil(3 * sigma);

        int xMin = Math.max(0, (int) Math.floor(centerX - radius));
        int xMax = Math.min(width - 1, (int) Math.ceil(centerX + radius));
        int yMin = Math.max(0, (int) Math.floor(centerY - radius));
        int yMax = Math.min(height - 1, (int) Math.ceil(centerY + radius));

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {

                double dx = x - centerX;
                double dy = y - centerY;

                double value = amplitude * Math.exp(
                        -(dx * dx + dy * dy) / (2 * sigma * sigma)
                );

                int index = y * width + x;
                pixels[index] += (float) value;
            }
        }
    }

    private static class Detection {

        int frame;
        double x;
        double y;
        double intensity;

        Detection(
                int frame,
                double x,
                double y,
                double intensity
        ) {
            this.frame = frame;
            this.x = x;
            this.y = y;
            this.intensity = intensity;
        }
    }

    private static class Track {

        int id;
        List<Detection> detections = new ArrayList<>();

        Track(int id, Detection firstDetection) {
            this.id = id;
            addDetection(firstDetection);
        }

        void addDetection(Detection detection) {
            detections.add(detection);
        }

        Detection getLastDetection() {
            return detections.get(detections.size() - 1);
        }
    }

    private static class LinkCandidate {

        Track track;
        Detection detection;
        double distance;

        LinkCandidate(
                Track track,
                Detection detection,
                double distance
        ) {
            this.track = track;
            this.detection = detection;
            this.distance = distance;
        }
    }
}