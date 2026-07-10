package io.github.zhengfangfang0304.particletracking.gui;

import io.github.zhengfangfang0304.particletracking.controller.ParticleTrackingController;
import io.github.zhengfangfang0304.particletracking.io.ImageSequenceImporter;
import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.preprocessing.GaussianDenoiser;
import io.github.zhengfangfang0304.particletracking.preprocessing.ImageDenoiser;
import io.github.zhengfangfang0304.particletracking.preprocessing.MedianDenoiser;
import io.github.zhengfangfang0304.particletracking.detection.CentroidDetector;
import io.github.zhengfangfang0304.particletracking.detection.DetectionParameters;
import io.github.zhengfangfang0304.particletracking.detection.LocalMaximumDetector;
import io.github.zhengfangfang0304.particletracking.tracking.ParticleTracker;
import io.github.zhengfangfang0304.particletracking.tracking.TrackingParameters;
import io.github.zhengfangfang0304.particletracking.detection.ParticleDetector;
import io.github.zhengfangfang0304.particletracking.tracking.GreedyNearestNeighborTracker;

import io.github.zhengfangfang0304.particletracking.tracking.trackmate.TrackMateNearestNeighborTracker;
import io.github.zhengfangfang0304.particletracking.analysis.DiffusionCoefficientCalculator;
import io.github.zhengfangfang0304.particletracking.analysis.MsdCalculator;
import io.github.zhengfangfang0304.particletracking.analysis.TrackStatisticsCalculator;
import io.github.zhengfangfang0304.particletracking.io.ResultCsvExporter;
import io.github.zhengfangfang0304.particletracking.io.TrackCsvImporter;
import io.github.zhengfangfang0304.particletracking.model.DiffusionCoefficientResult;
import io.github.zhengfangfang0304.particletracking.model.EnsembleMsdResult;
import io.github.zhengfangfang0304.particletracking.model.MsdResult;
import io.github.zhengfangfang0304.particletracking.model.TrackStatistics;


import io.github.zhengfangfang0304.particletracking.simulation.GaussianSpotRenderer;
import io.github.zhengfangfang0304.particletracking.simulation.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.SyntheticDatasetExporter;
import io.github.zhengfangfang0304.particletracking.simulation.DatasetBatchConfig;
import io.github.zhengfangfang0304.particletracking.simulation.DatasetBatchGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.StandardBrownianBatchPreset;


import ij.WindowManager;
import ij.gui.Plot;
import ij.measure.ResultsTable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.io.File;


import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * 单颗粒追踪插件的主界面。
 *
 * 这个类负责：
 * 1. 创建窗口；
 * 2. 创建按钮、输入框和下拉框；
 * 3. 接收用户操作；
 * 4. 显示日志和结果。
 *
 * 这个类不负责实现检测和追踪算法。
 */
public final class ParticleTrackingFrame extends JFrame {
    private JComboBox<String> denoiseMethodBox;
    private JTextField denoiseParameterField;

    private JComboBox<String> detectionMethodBox;
    private JComboBox<String> exportTypeBox;
    private JCheckBox invertSequenceCheckBox;

    private JTextField detectionThresholdField;
    private JTextField localMaxRadiusField;
    private JTextField minDistanceField;
    private JTextField trackingMaxDistanceField;

    private final List<Detection> lastDetections =
            new ArrayList<>();

    private final List<Track> lastTracks =
            new ArrayList<>();

    private final ParticleTrackingController controller;

    private final Font chineseFont =
            new Font(
                    "Microsoft YaHei",
                    Font.PLAIN,
                    16
            );

    private final Font titleFont =
            new Font(
                    "Microsoft YaHei",
                    Font.BOLD,
                    22
            );

    private final JTextArea logArea =
            new JTextArea();

    

    public ParticleTrackingFrame(
            ParticleTrackingController controller
    ) {
        super("My ImageJ GUI Plugin");

        this.controller =
                Objects.requireNonNull(
                        controller,
                        "Controller不能为null。"
                );

        initializeWindow();

        SwingUtilities.invokeLater(
                this::showStartupChoice
        );
    }

    //弹出选择窗口，是否进入数据模拟
    private void showStartupChoice() {
        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "请选择进入方式：",
                        "单颗粒追踪插件",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{
                                "设计并生成模拟数据",
                                "进入普通分析界面",
                                "取消"
                        },
                        "进入普通分析界面"
                );

        if (choice == 0) {
            generateTestImage();
        } else if (choice == 2) {
            dispose();
        }
    }

    private void initializeWindow() {

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setSize(1300, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title =
                new JLabel(
                        "单颗粒追踪分析系统",
                        SwingConstants.CENTER
                );

        title.setFont(titleFont);

        JButton importSequenceButton = new JButton("导入图像序列");

        JButton imageButton = new JButton("检查当前图像");

        JButton importCSVButton = new JButton("导入CSV轨迹");

        JButton denoiseButton = new JButton("执行降噪");

        JButton detectButton = new JButton("识别颗粒");

        JButton trackButton = new JButton("自编简单追踪");

        JButton trackMateButton = new JButton("TrackMate 最近邻");

        JButton analyzeButton = new JButton("轨迹统计");

        JButton msdButton = new JButton("计算 MSD");

        JButton plotMSDButton = new JButton("绘制MSD");

        JButton diffusionButton = new JButton("计算扩散系数D");

        JButton exportButton = new JButton("导出结果");

        JButton closeButton = new JButton("关闭");

        JButton[] buttons =
                {
                        importSequenceButton,
                        imageButton,
                        importCSVButton,
                        denoiseButton,
                        detectButton,
                        trackButton,
                        trackMateButton,
                        analyzeButton,
                        msdButton,
                        plotMSDButton,
                        diffusionButton,
                        exportButton,
                        closeButton
                };

        for (JButton button : buttons) {
            button.setFont(chineseFont);
        }

        invertSequenceCheckBox =
                new JCheckBox(
                        "导入时反相（适用于暗颗粒）",
                        true
                );

        invertSequenceCheckBox.setFont(chineseFont);

        denoiseMethodBox =
                new JComboBox<>(
                        new String[]{
                                "Gaussian Blur 高斯滤波",
                                "Median Filter 中值滤波"
                        }
                );

        denoiseMethodBox.setFont(chineseFont);

        denoiseParameterField =
                new JTextField("1.0", 6);

        denoiseParameterField.setFont(chineseFont);

        detectionMethodBox =
                new JComboBox<>(
                        new String[]{
                                "Local Maximum 局部极大值",
                                "Centroid 质心定位"
                        }
                );

        detectionMethodBox.setFont(chineseFont);

        exportTypeBox =
                new JComboBox<>(
                        new String[]{
                                "Track Results 轨迹坐标",
                                "Track Summary 轨迹统计",
                                "MSD Results MSD结果",
                                "Ensemble MSD 总体平均MSD"
                        }
                );

        exportTypeBox.setFont(chineseFont);

        detectionThresholdField =
                new JTextField("80", 6);

        localMaxRadiusField =
                new JTextField("2", 6);

        minDistanceField =
                new JTextField("6", 6);

        trackingMaxDistanceField =
                new JTextField("10", 6);

        JTextField[] textFields =
                {
                        denoiseParameterField,
                        detectionThresholdField,
                        localMaxRadiusField,
                        minDistanceField,
                        trackingMaxDistanceField
                };

        for (JTextField textField : textFields) {
            textField.setFont(chineseFont);
        }

        JLabel denoiseMethodLabel =
                new JLabel("降噪方法：");

        JLabel denoiseParameterLabel =
                new JLabel("降噪参数：");

        JLabel detectionMethodLabel =
                new JLabel("识别方法：");

        JLabel thresholdLabel =
                new JLabel("识别阈值：");

        JLabel radiusLabel =
                new JLabel("局部极大半径：");

        JLabel minDistanceLabel =
                new JLabel("最小距离：");

        JLabel trackingDistanceLabel =
                new JLabel("追踪最大距离：");

        JLabel exportTypeLabel =
                new JLabel("导出类型：");

        JLabel[] labels =
                {
                        denoiseMethodLabel,
                        denoiseParameterLabel,
                        detectionMethodLabel,
                        thresholdLabel,
                        radiusLabel,
                        minDistanceLabel,
                        trackingDistanceLabel,
                        exportTypeLabel
                };

        for (JLabel label : labels) {
            label.setFont(chineseFont);
        }

        logArea.setEditable(false);
        logArea.setFont(
                new Font(
                        "Microsoft YaHei",
                        Font.PLAIN,
                        14
                )
        );

        importSequenceButton.addActionListener(event -> importImageSequence());

        imageButton.addActionListener(event -> readCurrentImage());

        importCSVButton.addActionListener(event -> importTrackingCSV());

        denoiseButton.addActionListener(event -> denoiseCurrentImage());

        detectButton.addActionListener(event -> detectParticles());

        trackButton.addActionListener(event -> trackParticles());

        trackMateButton.addActionListener(event -> trackParticlesWithTrackMate());

        analyzeButton.addActionListener(event -> analyzeTracks());

        msdButton.addActionListener(event -> calculateMSD());

        plotMSDButton.addActionListener(event -> plotMSD());

        diffusionButton.addActionListener(event -> calculateDiffusionCoefficient());

        exportButton.addActionListener(event -> exportSelectedResults());

        closeButton.addActionListener(event -> dispose());

        JPanel controlPanel =
                new JPanel();

        controlPanel.setLayout(
                new GridLayout(
                        6,
                        1,
                        10,
                        10
                )
        );

        controlPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        JPanel row1 =
                new JPanel();
        row1.add(imageButton);
        row1.add(denoiseButton);

        JPanel importRow =
                new JPanel();

        importRow.add(importSequenceButton);
        importRow.add(importCSVButton);
        importRow.add(invertSequenceCheckBox);

        JPanel row2 =
                new JPanel();

        row2.add(denoiseMethodLabel);
        row2.add(denoiseMethodBox);
        row2.add(denoiseParameterLabel);
        row2.add(denoiseParameterField);

        JPanel row3 =
                new JPanel();

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

        JPanel row4 =
                new JPanel();

        row4.add(detectButton);
        row4.add(trackButton);
        row4.add(trackMateButton);
        row4.add(analyzeButton);
        row4.add(msdButton);
        row4.add(plotMSDButton);
        row4.add(diffusionButton);
        row4.add(exportButton);
        row4.add(closeButton);

        JPanel row5 =
                new JPanel();

        row5.add(exportTypeLabel);
        row5.add(exportTypeBox);

        controlPanel.add(row1);
        controlPanel.add(importRow);
        controlPanel.add(row2);
        controlPanel.add(row3);
        controlPanel.add(row4);
        controlPanel.add(row5);

        JScrollPane logScrollPane =
                new JScrollPane(logArea);

        logScrollPane.setPreferredSize(
                new Dimension(
                        800,
                        150
                )
        );

        logScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "日志区域"
                )
        );

        add(title, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }
    
    private void readCurrentImage() {
        try {
            ImagePlus image =
                    IJ.getImage();

            logArea.append(
                    "当前图像："
                            + image.getTitle()
                            + "\n"
            );

            logArea.append(
                    "宽度："
                            + image.getWidth()
                            + "\n"
            );

            logArea.append(
                    "高度："
                            + image.getHeight()
                            + "\n"
            );

            logArea.append(
                    "切片/帧数："
                            + image.getStackSize()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "请先在 Fiji/ImageJ 中打开一张图像。\n\n"
            );
        }
    }
//generateTestImage更清楚的名字应该是openSimulationGenerator，为了少改代码，暂时不更改
//它的主要作用是启动时选择“设计并生成模拟数据”后，打开模拟数据生成器界面
    private void generateTestImage() {
        try {
            SimulationSetupDialog.DialogResult dialogResult =
                    SimulationSetupDialog.showDialog(this); 
            if (dialogResult.getAction()
                    == SimulationSetupDialog.DialogAction.CANCEL) {

                logArea.append(
                        "已取消模拟数据生成。\n\n"
                );
                return;
            }

            if (dialogResult.getAction()
                    == SimulationSetupDialog.DialogAction.STANDARD_BROWNIAN_BATCH) {

                generateStandardBrownianBatchDataset();
                return;
            }

            SimulationConfig config =
                    dialogResult.getConfig();

            if (config == null) {
                logArea.append(
                        "模拟数据参数为空，已取消生成。\n\n"
                );
                return;
            }

            SyntheticDatasetGenerator generator =
                    new SyntheticDatasetGenerator();

            SyntheticDataset dataset =
                    generator.generate(config);

            GaussianSpotRenderer renderer =
                    new GaussianSpotRenderer();

            ImagePlus image =
                    renderer.render(dataset, config);

            image.show();

            int saveChoice =
                    javax.swing.JOptionPane.showConfirmDialog(
                            this,
                            "是否保存模拟图像、ground truth 和参数配置？",
                            "保存模拟数据",
                            javax.swing.JOptionPane.YES_NO_OPTION
                    );

            if (saveChoice == javax.swing.JOptionPane.YES_OPTION) {
                JFileChooser directoryChooser =
                        new JFileChooser();

                directoryChooser.setDialogTitle(
                        "选择模拟数据保存文件夹"
                );

                directoryChooser.setFileSelectionMode(
                        JFileChooser.DIRECTORIES_ONLY
                );

                int result =
                        directoryChooser.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputDirectory =
                            directoryChooser.getSelectedFile();

                    SyntheticDatasetExporter.exportAll(
                            image,
                            dataset,
                            config,
                            outputDirectory
                    );

                    logArea.append(
                            "模拟数据已保存。\n"
                                    + "保存文件夹: "
                                    + outputDirectory.getAbsolutePath()
                                    + "\n"
                                    + "已导出文件:\n"
                                    + "1. simulation_movie.tif\n"
                                    + "2. ground_truth_detections.csv\n"
                                    + "3. simulation_config.json\n\n"
                    );
                } else {
                    logArea.append(
                            "已取消保存模拟数据。\n\n"
                    );
                }
            }

            logArea.append(
                    "已生成模拟测试图像。\n"
                            + "图像尺寸: " + config.width + " × " + config.height + "\n"
                            + "帧数: " + config.frames + "\n"
                            + "帧率: " + config.frameRateFps + " fps\n"
                            + "时间间隔: " + config.getFrameIntervalSeconds() + " s\n"
                            + "运动模式: " + config.motionMode + "\n"
                            + "粒子数: " + config.getResolvedParticleCount() + "\n"
                            + "是否按密度计算: " + (config.useDensity ? "是" : "否") + "\n"
                            + "粒子密度: " + config.particleDensityPerUm2 + " particles/μm²\n"
                            + "扩散系数: " + config.diffusionCoefficientUm2PerSecond + " μm²/s\n"
                            + "真实检测点数量: " + dataset.size() + "\n"
                            + "PSF sigma: " + config.psfSigma + "\n"
                            + "背景强度: " + config.background + "\n"
                            + "噪声 sigma: " + config.noiseSigma + "\n\n"
            );

        } catch (Exception ex) {
            logArea.append(
                    "生成模拟测试图像失败: "
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }
    
    private void importImageSequence() {
        try {
            DirectoryChooser directoryChooser =
                    new DirectoryChooser(
                            "选择图像序列文件夹"
                    );

            String directory =
                    directoryChooser.getDirectory();

            if (directory == null) {
                logArea.append(
                        "已取消导入图像序列。\n\n"
                );
                return;
            }

            boolean invertImage =
                    invertSequenceCheckBox.isSelected();

            ImageSequenceImporter.ImportResult importResult =
                    ImageSequenceImporter.importFromDirectory(
                            directory,
                            invertImage
                    );

            ImagePlus preparedSequence =
                    importResult.image();

            preparedSequence.show();

            lastDetections.clear();
            lastTracks.clear();
            controller.clearSession();

            logArea.append("图像序列导入完成。\n");

            logArea.append(
                    "文件夹："
                            + importResult.directory()
                            + "\n"
            );

            logArea.append(
                    "图像名称："
                            + preparedSequence.getTitle()
                            + "\n"
            );

            logArea.append(
                    "宽度："
                            + importResult.width()
                            + "\n"
            );

            logArea.append(
                    "高度："
                            + importResult.height()
                            + "\n"
            );

            logArea.append(
                    "帧数："
                            + importResult.frames()
                            + "\n"
            );

            logArea.append(
                    "导入时反相："
                            + (importResult.inverted() ? "是" : "否")
                            + "\n"
            );

            logArea.append(
                    "之前的识别和追踪结果已清空。\n"
            );

            logArea.append(
                    "下一步可点击：检查当前图像 → 执行降噪 → 识别颗粒。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "图像序列导入参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "图像序列导入失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void denoiseCurrentImage() {
        try {
            ImagePlus original =
                    IJ.getImage();

            if (original == null) {
                logArea.append(
                        "没有检测到当前图像，请先打开或生成一张图像。\n\n"
                );
                return;
            }

            String method =
                    (String) denoiseMethodBox.getSelectedItem();

            double parameter =
                    Double.parseDouble(
                            denoiseParameterField.getText()
                    );

            ImageDenoiser denoiser;

            if (method != null
                    && method.contains("Median")) {

                denoiser =
                        new MedianDenoiser();

            } else {

                denoiser =
                        new GaussianDenoiser();
            }

            ImagePlus denoised =
                    denoiser.denoise(
                            original,
                            parameter
                    );

            denoised.show();

            lastDetections.clear();
            lastTracks.clear();
            controller.clearSession();

            logArea.append("降噪完成。\n");

            logArea.append(
                    "原始图像："
                            + original.getTitle()
                            + "\n"
            );

            logArea.append(
                    "降噪方法："
                            + denoiser.getName()
                            + "\n"
            );

            logArea.append(
                    "参数："
                            + parameter
                            + "\n"
            );

            logArea.append(
                    "处理帧数："
                            + denoised.getStackSize()
                            + "\n"
            );

            logArea.append(
                    "已生成新图像："
                            + denoised.getTitle()
                            + "\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "降噪参数输入错误，请输入数字，例如 1.0 或 2.0。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "降噪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "降噪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void detectParticles() {
        try {
            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像，请先打开、生成或导入一张图像。\n\n"
                );
                return;
            }

            double threshold =
                    Double.parseDouble(
                            detectionThresholdField.getText()
                    );

            int localMaximumRadius =
                    Integer.parseInt(
                            localMaxRadiusField.getText()
                    );

            double minimumDistance =
                    Double.parseDouble(
                            minDistanceField.getText()
                    );

            DetectionParameters parameters =
                    new DetectionParameters(
                            threshold,
                            localMaximumRadius,
                            minimumDistance
                    );

            String method =
                    (String) detectionMethodBox.getSelectedItem();

            ParticleDetector detector;

            if (method != null
                    && method.contains("Centroid")) {

                detector =
                        new CentroidDetector();

            } else {

                detector =
                        new LocalMaximumDetector();
            }

            List<Detection> detections =
                    controller.detect(
                            image,
                            detector,
                            parameters
                    );

            lastDetections.clear();
            lastDetections.addAll(
                    detections
            );

            lastTracks.clear();

            ResultTablePresenter.showDetections(
                    image,
                    lastDetections,
                    "Particle Detections"
            );

            logArea.append("颗粒识别完成。\n");

            logArea.append(
                    "识别方法："
                            + detector.getName()
                            + "\n"
            );

            logArea.append(
                    "识别阈值："
                            + threshold
                            + "\n"
            );

            logArea.append(
                    "局部极大半径："
                            + localMaximumRadius
                            + "\n"
            );

            logArea.append(
                    "最小距离："
                            + minimumDistance
                            + "\n"
            );

            logArea.append(
                    "识别到颗粒数量："
                            + lastDetections.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 Particle Detections 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "识别参数输入错误，请检查阈值、半径和最小距离是否为数字。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "识别参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "颗粒识别失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void trackParticles() {
        try {
            if (lastDetections.isEmpty()) {
                logArea.append(
                        "还没有颗粒识别结果，请先点击“识别颗粒”。\n\n"
                );
                return;
            }

            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像。\n\n"
                );
                return;
            }

            double maximumLinkingDistance =
                    Double.parseDouble(
                            trackingMaxDistanceField.getText()
                    );

            TrackingParameters parameters =
                    new TrackingParameters(
                            maximumLinkingDistance,
                            0
                    );
//最大连接距离 = 用户输入的像素距离
//最大跨帧间隔 = 0，也就是只连接相邻帧
            ParticleTracker tracker =
                    new GreedyNearestNeighborTracker();

            List<Track> tracks =
                    controller.track(
                            tracker,
                            parameters
                    );

            lastTracks.clear();
            lastTracks.addAll(
                    tracks
            );

            ResultTablePresenter.showTrackingResults(
                    image,
                    lastDetections,
                    lastTracks,
                    "Track Results"
            );

            logArea.append("简单追踪完成。\n");

            logArea.append(
                    "追踪器："
                            + tracker.getName()
                            + "\n"
            );

            logArea.append(
                    "追踪最大距离："
                            + maximumLinkingDistance
                            + " pixel\n"
            );

            logArea.append(
                    "最大间隔帧数：0\n"
            );

            logArea.append(
                    "生成轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 Track Results 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "追踪参数输入错误，请输入数字，例如 10。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "追踪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "追踪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void trackParticlesWithTrackMate() {
        try {
            if (lastDetections.isEmpty()) {
                logArea.append(
                        "还没有颗粒识别结果，请先点击“识别颗粒”。\n\n"
                );
                return;
            }

            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像。\n\n"
                );
                return;
            }

            double maximumLinkingDistance =
                    Double.parseDouble(
                            trackingMaxDistanceField.getText()
                    );

            TrackingParameters parameters =
                    new TrackingParameters(
                            maximumLinkingDistance,
                            0
                    );

            ParticleTracker tracker =
                    new TrackMateNearestNeighborTracker();

            List<Track> tracks =
                    controller.track(
                            tracker,
                            parameters
                    );

            lastTracks.clear();
            lastTracks.addAll(
                    tracks
            );

            ResultTablePresenter.showTrackingResults(
                    image,
                    lastDetections,
                    lastTracks,
                    "TrackMate NN Results"
            );

            logArea.append("TrackMate 最近邻追踪完成。\n");

            logArea.append(
                    "追踪器："
                            + tracker.getName()
                            + "\n"
            );

            logArea.append(
                    "追踪最大距离："
                            + maximumLinkingDistance
                            + " pixel\n"
            );

            logArea.append(
                    "生成轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 TrackMate NN Results 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "TrackMate追踪参数输入错误，请输入数字，例如 10。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "TrackMate追踪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "TrackMate追踪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void importTrackingCSV() {
        JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle(
                "选择外部追踪 CSV 文件"
        );

        int userSelection =
                fileChooser.showOpenDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            logArea.append("已取消导入。\n\n");
            return;
        }

        File csvFile =
                fileChooser.getSelectedFile();

        try {
            TrackCsvImporter.ImportResult importResult =
                    TrackCsvImporter.importFrom(
                            csvFile
                    );

            lastDetections.clear();
            lastDetections.addAll(
                    importResult.detections()
            );

            lastTracks.clear();
            lastTracks.addAll(
                    importResult.tracks()
            );

            ImagePlus currentImage =
                    WindowManager.getCurrentImage();

            controller.loadImportedResults(
                    currentImage,
                    importResult.detections(),
                    importResult.tracks()
            );

            ResultTablePresenter.showImportedTrackResults(
                    lastTracks
            );

            logArea.append("外部追踪 CSV 导入完成。\n");
            logArea.append(
                    "文件："
                            + csvFile.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "有效数据行数："
                            + importResult.importedRows()
                            + "\n"
            );
            logArea.append(
                    "particle 数量："
                            + lastTracks.size()
                            + "\n"
            );
            logArea.append(
                    "坐标点数量："
                            + lastDetections.size()
                            + "\n"
            );

            if (!importResult.intensityColumnFound()) {
                logArea.append(
                        "未找到 mass、signal 或 intensity 列，强度统一记为 0。\n"
                );
            }

            logArea.append(
                    "现在可以直接点击：轨迹统计、计算 MSD、绘制MSD、计算D或导出结果。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "CSV格式错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "CSV 导入失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void analyzeTracks() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            List<TrackStatistics> statisticsList =
                    TrackStatisticsCalculator.calculate(
                            lastTracks
                    );

            ResultsTable table =
                    new ResultsTable();

            for (TrackStatistics statistics : statisticsList) {

                table.incrementCounter();

                table.addValue(
                        "particle",
                        statistics.trackId()
                );

                table.addValue(
                        "Start_Frame",
                        statistics.startFrame()
                );

                table.addValue(
                        "End_Frame",
                        statistics.endFrame()
                );

                table.addValue(
                        "N_Points",
                        statistics.numberOfPoints()
                );

                table.addValue(
                        "Duration_Frames",
                        statistics.durationFrames()
                );

                table.addValue(
                        "Start_X",
                        statistics.startX()
                );

                table.addValue(
                        "Start_Y",
                        statistics.startY()
                );

                table.addValue(
                        "End_X",
                        statistics.endX()
                );

                table.addValue(
                        "End_Y",
                        statistics.endY()
                );

                table.addValue(
                        "Displacement",
                        statistics.displacement()
                );

                table.addValue(
                        "Path_Length",
                        statistics.pathLength()
                );

                table.addValue(
                        "Mean_Step",
                        statistics.meanStep()
                );

                table.addValue(
                        "Mean_Speed_px_per_frame",
                        statistics.meanSpeed()
                );

                table.addValue(
                        "Mean_Intensity",
                        statistics.meanIntensity()
                );
            }

            table.show(
                    "Track Summary"
            );

            logArea.append("轨迹统计完成。\n");
            logArea.append(
                    "输入轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );
            logArea.append(
                    "输出统计行数："
                            + statisticsList.size()
                            + "\n"
            );
            logArea.append(
                    "结果已显示在 Track Summary 表格中。\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹统计失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void calculateMSD() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            List<MsdResult> msdResults =
                    MsdCalculator.calculatePerTrack(
                            lastTracks
                    );

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            ResultsTable msdTable =
                    new ResultsTable();

            for (MsdResult result : msdResults) {

                msdTable.incrementCounter();

                msdTable.addValue(
                        "particle",
                        result.trackId()
                );

                msdTable.addValue(
                        "Lag_Frames",
                        result.lagFrames()
                );

                msdTable.addValue(
                        "MSD_px2",
                        result.msd()
                );

                msdTable.addValue(
                        "N_Pairs",
                        result.pairCount()
                );
            }

            ResultsTable ensembleTable =
                    new ResultsTable();

            for (EnsembleMsdResult result : ensembleResults) {

                ensembleTable.incrementCounter();

                ensembleTable.addValue(
                        "Lag_Frames",
                        result.lagFrames()
                );

                ensembleTable.addValue(
                        "Ensemble_MSD_px2",
                        result.ensembleMsd()
                );

                ensembleTable.addValue(
                        "N_Pairs",
                        result.pairCount()
                );
            }

            msdTable.show("MSD Results");
            ensembleTable.show("MSD Ensemble");

            logArea.append("MSD 计算完成。\n");
            logArea.append(
                    "单轨迹MSD结果行数："
                            + msdResults.size()
                            + "\n"
            );
            logArea.append(
                    "Ensemble MSD结果行数："
                            + ensembleResults.size()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD 计算失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void plotMSD() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            if (ensembleResults.isEmpty()) {
                logArea.append(
                        "没有足够的轨迹点绘制MSD曲线。\n\n"
                );
                return;
            }

            double[] x =
                    new double[ensembleResults.size()];

            double[] y =
                    new double[ensembleResults.size()];

            for (int i = 0;
                i < ensembleResults.size();
                i++) {

                EnsembleMsdResult result =
                        ensembleResults.get(i);

                x[i] =
                        result.lagFrames();

                y[i] =
                        result.ensembleMsd();
            }

            Plot plot =
                    new Plot(
                            "Ensemble MSD Curve",
                            "Lag Frames",
                            "MSD px^2"
                    );

            plot.addPoints(
                    x,
                    y,
                    Plot.CONNECTED_CIRCLES
            );

            plot.show();

            logArea.append("MSD曲线绘制完成。\n");
            logArea.append(
                    "数据点数量："
                            + ensembleResults.size()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD绘图失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void calculateDiffusionCoefficient() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            DiffusionCoefficientResult result =
                    DiffusionCoefficientCalculator.calculateFromTracks(
                            lastTracks,
                            5
                    );

            ResultsTable diffusionTable =
                    new ResultsTable();

            diffusionTable.incrementCounter();

            diffusionTable.addValue(
                    "Fit_Points",
                    result.fitPoints()
            );

            diffusionTable.addValue(
                    "Slope_px2_per_frame",
                    result.slope()
            );

            diffusionTable.addValue(
                    "Intercept_px2",
                    result.intercept()
            );

            diffusionTable.addValue(
                    "Diffusion_Coefficient_D_px2_per_frame",
                    result.diffusionCoefficient()
            );

            diffusionTable.show(
                    "Diffusion Coefficient"
            );

            logArea.append("扩散系数计算完成。\n");
            logArea.append(
                    "拟合模型：MSD = slope × lag + intercept\n"
            );
            logArea.append(
                    "使用前 "
                            + result.fitPoints()
                            + " 个 Ensemble MSD 点拟合。\n"
            );
            logArea.append(
                    "Slope = "
                            + result.slope()
                            + " px²/frame\n"
            );
            logArea.append(
                    "Intercept = "
                            + result.intercept()
                            + " px²\n"
            );
            logArea.append(
                    "D = slope / 4 = "
                            + result.diffusionCoefficient()
                            + " px²/frame\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "扩散系数参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "扩散系数计算失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportSelectedResults() {
        String exportType =
                (String) exportTypeBox.getSelectedItem();

        if (exportType == null) {
            logArea.append(
                    "请选择导出类型。\n\n"
            );
            return;
        }

        if (exportType.contains("Track Results")) {

            exportTrackResultsToCSV();

        } else if (exportType.contains("Track Summary")) {

            exportTrackSummaryToCSV();

        } else if (exportType.contains("MSD Results")) {

            exportMSDResultsToCSV();

        } else if (exportType.contains("Ensemble MSD")) {

            exportEnsembleMSDToCSV();

        } else {

            logArea.append(
                    "未知导出类型："
                            + exportType
                            + "\n\n"
            );
        }
    }

    private File chooseCSVFile(
            String defaultFileName
    ) {
        JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle(
                "选择CSV保存位置"
        );

        fileChooser.setSelectedFile(
                new File(defaultFileName)
        );

        int userSelection =
                fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile =
                fileChooser.getSelectedFile();

        if (selectedFile == null) {
            return null;
        }

        String path =
                selectedFile.getAbsolutePath();

        if (!path.toLowerCase().endsWith(".csv")) {
            selectedFile =
                    new File(
                            path + ".csv"
                    );
        }

        return selectedFile;
    }

    private void exportTrackResultsToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "track_results.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            int rowCount =
                    ResultCsvExporter.exportTrackResults(
                            lastTracks,
                            fileToSave
                    );

            logArea.append("轨迹坐标导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹坐标导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportTrackSummaryToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "track_summary.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<TrackStatistics> statisticsList =
                    TrackStatisticsCalculator.calculate(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportTrackStatistics(
                            statisticsList,
                            fileToSave
                    );

            logArea.append("轨迹统计导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出轨迹数量："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹统计导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportMSDResultsToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "msd_results.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<MsdResult> msdResults =
                    MsdCalculator.calculatePerTrack(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportMsdResults(
                            msdResults,
                            fileToSave
                    );

            logArea.append("MSD 结果导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出 MSD 数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD 结果导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportEnsembleMSDToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先点击“自编简单追踪”或“TrackMate 最近邻”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "ensemble_msd.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportEnsembleMsdResults(
                            ensembleResults,
                            fileToSave
                    );

            logArea.append("Ensemble MSD 导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "Ensemble MSD 导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    /**
     * 向界面日志框追加文字。
     */
    public void appendLog(String message) {
        logArea.append(message);
    }

    private void generateStandardBrownianBatchDataset() {
        try {
            DatasetBatchConfig batchConfig =
                    StandardBrownianBatchPreset.createDefault();

            int choice =
                    JOptionPane.showConfirmDialog(
                            this,
                            "即将生成标准自由空间布朗运动数据集。\n\n"
                                    + "实验条件数量: "
                                    + batchConfig.getTotalExperimentCount()
                                    + "\n"
                                    + "FOV 总数量: "
                                    + batchConfig.getTotalFovCount()
                                    + "\n\n"
                                    + "是否继续？",
                            "生成标准布朗运动数据集",
                            JOptionPane.YES_NO_OPTION
                    );

            if (choice != JOptionPane.YES_OPTION) {
                logArea.append(
                        "已取消标准布朗运动数据集生成。\n\n"
                );
                return;
            }

            JFileChooser directoryChooser =
                    new JFileChooser();

            directoryChooser.setDialogTitle(
                    "选择标准布朗运动数据集保存位置"
            );

            directoryChooser.setFileSelectionMode(
                    JFileChooser.DIRECTORIES_ONLY
            );

            directoryChooser.setAcceptAllFileFilterUsed(false);

            int result =
                    directoryChooser.showDialog(
                            this,
                            "选择此文件夹"
                    );

            if (result != JFileChooser.APPROVE_OPTION) {
                logArea.append(
                        "已取消标准布朗运动数据集保存。\n\n"
                );
                return;
            }

            File outputDirectory =
                    directoryChooser.getSelectedFile();

            DatasetBatchGenerator batchGenerator =
                    new DatasetBatchGenerator();

            DatasetBatchGenerator.GenerationSummary summary =
                    batchGenerator.generate(
                            batchConfig,
                            outputDirectory
                    );

            logArea.append(
                    "标准布朗运动数据集生成完成。\n"
                            + "数据集路径: "
                            + summary.getDatasetRoot().getAbsolutePath()
                            + "\n"
                            + "实验条件数量: "
                            + summary.getExperimentCount()
                            + "\n"
                            + "FOV 数量: "
                            + summary.getFovCount()
                            + "\n"
                            + "运动模型: Einstein free Brownian motion\n"
                            + "理论关系: MSD(τ) = 4Dτ\n\n"
            );

        } catch (Exception ex) {
            logArea.append(
                    "标准布朗运动数据集生成失败: "
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    /**
     * 返回当前控制器。
     */
    public ParticleTrackingController getController() {
        return controller;
    }
}