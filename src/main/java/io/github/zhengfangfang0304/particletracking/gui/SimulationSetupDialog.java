package io.github.zhengfangfang0304.particletracking.gui;

import io.github.zhengfangfang0304.particletracking.simulation.MotionMode;
import io.github.zhengfangfang0304.particletracking.simulation.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.MotionSegment;
import io.github.zhengfangfang0304.particletracking.simulation.SimulationScenario;
import io.github.zhengfangfang0304.particletracking.simulation.VisibilityEvent;
import io.github.zhengfangfang0304.particletracking.simulation.VisibilityEventType;
import io.github.zhengfangfang0304.particletracking.simulation.SimulationExportOptions;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

//模拟数据生成参数设置窗口。
//本文件唯一的类：SimulationSetupDialog
public class SimulationSetupDialog extends JDialog {

        public enum DialogAction {
            CANCEL,
            SINGLE_DATASET,
            STANDARD_BROWNIAN_BATCH,
            MULTI_STAGE_MOTION_TEST
        }
        public static class DialogResult {

            private final DialogAction action;

            private final SimulationConfig config;

            private final SimulationScenario scenario;

            private final SimulationExportOptions exportOptions;

            public DialogResult(
                    DialogAction action,
                    SimulationConfig config,
                    SimulationScenario scenario,
                    SimulationExportOptions exportOptions
            ) {
                this.action =
                        action;

                this.config =
                        config;

                this.scenario =
                        scenario;

                this.exportOptions =
                        exportOptions;
            }

            public DialogAction getAction() {
                return action;
            }

            public SimulationConfig getConfig() {
                return config;
            }

            public SimulationScenario getScenario() {
                return scenario;
            }

            public SimulationExportOptions getExportOptions() {
                return exportOptions;
            }
        }
       
    private DialogAction action = DialogAction.CANCEL;

    private final JTextField widthField = new JTextField("256");

    private final JTextField heightField = new JTextField("256");

    private final JTextField framesField = new JTextField("100");

    private final JLabel particleCountLabel = new JLabel("粒子数：");

    private final JTextField particleCountField = new JTextField("8");

    private final JCheckBox useDensityCheckBox = new JCheckBox("根据粒子密度自动计算粒子数");

    private final JLabel densityLabel = new JLabel("粒子密度 particles/μm²：");

    private final JTextField densityField = new JTextField("0.05");

    private final JLabel densityModeNoteLabel = new JLabel();

    private final JTextField pixelSizeField = new JTextField("0.1");

    private final JTextField frameRateField = new JTextField("10.0");

    private final JTextField diffusionField = new JTextField("0.05");

    private final JTextField randomSeedField = new JTextField("12345");

    private final JComboBox<MotionMode> motionModeBox = new JComboBox<>(MotionMode.values());

    private final JTextField psfSigmaField = new JTextField("2.0");

    private final JTextField amplitudeField = new JTextField("180.0");

    private final JTextField backgroundField = new JTextField("20.0");

    private final JTextField noiseSigmaField = new JTextField("8.0");

    private final JTextField confinementRadiusField = new JTextField("80.0");

    private final JLabel motionTimelineSummaryLabel = new JLabel("运动时间轴：未初始化");

    private final JLabel visibilityEventsSummaryLabel = new JLabel("可见性事件：未初始化");

    private final JLabel outputOptionsSummaryLabel = new JLabel("输出设置：未初始化");

    private JTable motionTimelineTable;

    private DefaultTableModel motionTimelineTableModel;

    private final CardLayout cardLayout = new CardLayout();

    private final JPanel cardPanel = new JPanel(cardLayout);

    private static final String PAGE_OVERVIEW = "overview";

    private static final String PAGE_MOTION_TIMELINE = "motionTimeline";

    private static final String PAGE_VISIBILITY_EVENTS = "visibilityEvents";

    private static final String PAGE_OUTPUT_OPTIONS = "outputOptions";

    private static final String PAGE_PRE_GENERATE_CHECK = "preGenerateCheck";

    private static final String PAGE_PRESETS = "presets";

    private final JCheckBox exportMovieCheckBox =
        new JCheckBox("simulation_movie.tif", true);

    private final JCheckBox exportDetectionsCheckBox =
            new JCheckBox("ground_truth_detections.csv", true);

    private final JCheckBox exportTracksCheckBox =
            new JCheckBox("ground_truth_tracks.csv", true);

    private final JCheckBox exportVisibilityCheckBox =
            new JCheckBox("ground_truth_visibility.csv", true);

    private final JCheckBox exportVisibilityEventsCheckBox =
            new JCheckBox("ground_truth_visibility_events.csv", true);

    private final JCheckBox exportMotionSegmentsCheckBox =
            new JCheckBox("ground_truth_motion_segments.csv", true);

    private final JCheckBox exportSimulationConfigCheckBox =
            new JCheckBox("simulation_config.json", true);

    private final JCheckBox exportScenarioConfigCheckBox =
            new JCheckBox("scenario_config.json", true);

    private final JCheckBox exportTheoreticalMsdCheckBox =
            new JCheckBox("theoretical_msd.csv", true);

    private final JCheckBox exportGroundTruthMsdCheckBox =
            new JCheckBox("ground_truth_msd.csv", true);

    private SimulationExportOptions exportOptions;

    private JTable visibilityEventTable;

    private DefaultTableModel visibilityEventTableModel;

    private SimulationConfig config;

    private SimulationScenario scenario;

    private final JTextArea preGenerateCheckTextArea =
            new JTextArea();

    private JButton confirmGenerateButton;

    private SimulationConfig pendingConfig;

    private SimulationScenario pendingScenario;

    private SimulationExportOptions pendingExportOptions;

    private void showPage(
            String pageName
    ) {
        if (PAGE_OVERVIEW.equals(pageName)) {
            updateOverviewSummary();
        }

        cardLayout.show(
                cardPanel,
                pageName
        );
    }

    private int countSelectedOutputOptions() {
        int count =
                0;

        if (exportMovieCheckBox.isSelected()) {
            count++;
        }

        if (exportDetectionsCheckBox.isSelected()) {
            count++;
        }

        if (exportTracksCheckBox.isSelected()) {
            count++;
        }

        if (exportVisibilityCheckBox.isSelected()) {
            count++;
        }

        if (exportVisibilityEventsCheckBox.isSelected()) {
            count++;
        }

        if (exportMotionSegmentsCheckBox.isSelected()) {
            count++;
        }

        if (exportSimulationConfigCheckBox.isSelected()) {
            count++;
        }

        if (exportScenarioConfigCheckBox.isSelected()) {
            count++;
        }

        if (exportTheoreticalMsdCheckBox.isSelected()) {
            count++;
        }

        if (exportGroundTruthMsdCheckBox.isSelected()) {
            count++;
        }

        return count;
    }

    private void updateOverviewSummary() {
        int totalFrames =
                getFrameCountFromFieldOrDefault();

        int motionSegmentCount =
                motionTimelineTableModel == null
                        ? 0
                        : motionTimelineTableModel.getRowCount();

        int visibilityEventCount =
                visibilityEventTableModel == null
                        ? 0
                        : visibilityEventTableModel.getRowCount();

        int selectedOutputCount =
                countSelectedOutputOptions();

        motionTimelineSummaryLabel.setText(
                "运动时间轴："
                        + motionSegmentCount
                        + " 个阶段，当前总帧数 "
                        + totalFrames
        );

        visibilityEventsSummaryLabel.setText(
                "可见性事件："
                        + visibilityEventCount
                        + " 个事件"
        );

        outputOptionsSummaryLabel.setText(
                "输出设置："
                        + selectedOutputCount
                        + " / 10 个文件已勾选"
        );
    }

    private JPanel createOverviewSummaryPanel() {
        JPanel summaryPanel =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                6,
                                6
                        )
                );

        summaryPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "当前高级设置概览"
                )
        );

        summaryPanel.add(
                motionTimelineSummaryLabel
        );

        summaryPanel.add(
                visibilityEventsSummaryLabel
        );

        summaryPanel.add(
                outputOptionsSummaryLabel
        );

        return summaryPanel;
    }

    private JPanel createPageBottomPanel() {
        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton backButton =
                new JButton("返回总览");

        JButton generateButton =
                new JButton("生成数据");

        JButton cancelButton =
                new JButton("取消");

        backButton.addActionListener(
                event -> showPage(
                        PAGE_OVERVIEW
                )
        );

        generateButton.addActionListener(
                event -> onConfirm()
        );

        cancelButton.addActionListener(event -> {
            action =
                    DialogAction.CANCEL;

            dispose();
        });

        bottomPanel.add(backButton);
        bottomPanel.add(generateButton);
        bottomPanel.add(cancelButton);

        return bottomPanel;
    }

    private JPanel createOverviewPage(
            JPanel formPanel
    ) {
        JPanel overviewPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        overviewPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

JPanel contentPanel =
        new JPanel(
                new BorderLayout(
                        10,
                        10
                )
        );

        contentPanel.add(
                new JScrollPane(
                        formPanel
                ),
                BorderLayout.CENTER
        );

        contentPanel.add(
                createOverviewSummaryPanel(),
                BorderLayout.SOUTH
        );

        overviewPanel.add(
                contentPanel,
                BorderLayout.CENTER
        );

        JPanel navigationPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JButton motionTimelineButton = new JButton("编辑运动时间轴");

        JButton visibilityEventsButton = new JButton("编辑可见性事件");

        JButton outputOptionsButton = new JButton("输出设置");

        JButton presetsButton = new JButton("数据集预设");

        motionTimelineButton.addActionListener(
                event -> showPage(
                        PAGE_MOTION_TIMELINE
                )
        );

        visibilityEventsButton.addActionListener(
                event -> showPage(
                        PAGE_VISIBILITY_EVENTS
                )
        );

        outputOptionsButton.addActionListener(
                event -> showPage(
                        PAGE_OUTPUT_OPTIONS
                )
        );

        presetsButton.addActionListener(
                event -> showPage(
                        PAGE_PRESETS
                )
        );

        navigationPanel.add(motionTimelineButton);
        navigationPanel.add(visibilityEventsButton);
        navigationPanel.add(outputOptionsButton);
        navigationPanel.add(presetsButton);

        JPanel bottomPanel =
                new JPanel(
                        new BorderLayout()
                );

        bottomPanel.add(
                navigationPanel,
                BorderLayout.CENTER
        );

        JPanel actionPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton okButton =
                new JButton("生成数据");

        JButton cancelButton =
                new JButton("取消");

        okButton.addActionListener(
                event -> onConfirm()
        );


        cancelButton.addActionListener(event -> {
            action =
                    DialogAction.CANCEL;

            dispose();
        });

        actionPanel.add(okButton);
        actionPanel.add(cancelButton);

        bottomPanel.add(
                actionPanel,
                BorderLayout.SOUTH
        );

        overviewPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        return overviewPanel;
    }

    private JPanel createMotionTimelinePage() {
        JPanel page =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "运动时间轴设置"
                );

        page.add(
                titleLabel,
                BorderLayout.NORTH
        );

        page.add(
                createMotionTimelineEditorPanel(),
                BorderLayout.CENTER
        );

        page.add(
                createPageBottomPanel(),
                BorderLayout.SOUTH
        );

        return page;
    }

    private JPanel createVisibilityEventsPage() {
        JPanel page =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "可见性事件设置"
                );

        page.add(
                titleLabel,
                BorderLayout.NORTH
        );

        page.add(
                createVisibilityEventEditorPanel(),
                BorderLayout.CENTER
        );

        page.add(
                createPageBottomPanel(),
                BorderLayout.SOUTH
        );

        return page;
    }

    private JPanel createOutputOptionsPage() {
        JPanel page =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "输出文件设置"
                );

        page.add(
                titleLabel,
                BorderLayout.NORTH
        );

        page.add(
                createOutputOptionsPanel(),
                BorderLayout.CENTER
        );

        page.add(
                createPageBottomPanel(),
                BorderLayout.SOUTH
        );

        return page;
    }

    private JPanel createPresetsPage() {
        JPanel page =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "数据集预设"
                );

        page.add(
                titleLabel,
                BorderLayout.NORTH
        );

        JPanel presetPanel =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                8,
                                8
                        )
                );

        presetPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "选择一个预设"
                )
        );

        JButton standardBrownianBatchButton =
                new JButton("标准布朗批量数据集");

        JButton multiStageMotionButton =
                new JButton("多阶段运动测试数据");

        JButton standardSingleBrownianButton =
                new JButton("加载标准自由布朗运动参数");

        JButton blinkingPhotobleachingButton =
                new JButton("加载闪烁 / 光漂白测试参数");

        standardBrownianBatchButton.addActionListener(event -> {
            action =
                    DialogAction.STANDARD_BROWNIAN_BATCH;

            dispose();
        });

        multiStageMotionButton.addActionListener(event -> {
            action =
                    DialogAction.MULTI_STAGE_MOTION_TEST;

            dispose();
        });

        standardSingleBrownianButton.addActionListener(
                event -> loadStandardBrownianPreset()
        );

        blinkingPhotobleachingButton.addActionListener(
                event -> loadBlinkingPhotobleachingPreset()
        );

        presetPanel.add(standardSingleBrownianButton);
        presetPanel.add(blinkingPhotobleachingButton);
        presetPanel.add(multiStageMotionButton);
        presetPanel.add(standardBrownianBatchButton);

        JTextArea descriptionArea =
                new JTextArea();

        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        descriptionArea.setText(
                "预设说明：\n\n"
                        + "1. 标准自由布朗运动参数：加载单个标准布朗运动数据集参数，可继续手动修改。\n"
                        + "2. 闪烁 / 光漂白测试参数：加载带 blinking 和 photobleaching 的测试参数。\n"
                        + "3. 多阶段运动测试数据：直接生成固定的多阶段运动测试数据。\n"
                        + "4. 标准布朗批量数据集：生成一批不同 D、密度、帧率、噪声条件的数据集。\n"
        );

        page.add(
                presetPanel,
                BorderLayout.CENTER
        );

        page.add(
                new JScrollPane(
                        descriptionArea
                ),
                BorderLayout.EAST
        );

        page.add(
                createPageBottomPanel(),
                BorderLayout.SOUTH
        );

        return page;
    }

    private JPanel createPreGenerateCheckPage() {
        JPanel page =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "生成前统一检查"
                );

        page.add(
                titleLabel,
                BorderLayout.NORTH
        );

        preGenerateCheckTextArea.setEditable(false);
        preGenerateCheckTextArea.setLineWrap(true);
        preGenerateCheckTextArea.setWrapStyleWord(true);
        preGenerateCheckTextArea.setText(
                "点击“生成数据”后，这里会显示参数检查结果。"
        );

        page.add(
                new JScrollPane(
                        preGenerateCheckTextArea
                ),
                BorderLayout.CENTER
        );

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton backButton =
                new JButton("返回总览");

        confirmGenerateButton =
                new JButton("确认生成");

        JButton cancelButton =
                new JButton("取消");

        backButton.addActionListener(
                event -> showPage(
                        PAGE_OVERVIEW
                )
        );

        confirmGenerateButton.addActionListener(
                event -> confirmGenerationAfterCheck()
        );

        cancelButton.addActionListener(event -> {
            action =
                    DialogAction.CANCEL;

            dispose();
        });

        bottomPanel.add(backButton);
        bottomPanel.add(confirmGenerateButton);
        bottomPanel.add(cancelButton);

        page.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        return page;
    }

    private SimulationConfig buildConfigFromFields() {
        SimulationConfig newConfig =
                SimulationConfig.defaultConfig();

        newConfig.width =
                Integer.parseInt(
                        widthField.getText()
                                .trim()
                );

        newConfig.height =
                Integer.parseInt(
                        heightField.getText()
                                .trim()
                );

        newConfig.frames =
                Integer.parseInt(
                        framesField.getText()
                                .trim()
                );

        newConfig.particleCount =
                Integer.parseInt(
                        particleCountField.getText()
                                .trim()
                );

        newConfig.useDensity =
                useDensityCheckBox.isSelected();

        newConfig.particleDensityPerUm2 =
                Double.parseDouble(
                        densityField.getText()
                                .trim()
                );

        newConfig.pixelSizeUm =
                Double.parseDouble(
                        pixelSizeField.getText()
                                .trim()
                );

        newConfig.frameRateFps =
                Double.parseDouble(
                        frameRateField.getText()
                                .trim()
                );

        newConfig.diffusionCoefficientUm2PerSecond =
                Double.parseDouble(
                        diffusionField.getText()
                                .trim()
                );

        newConfig.randomSeed =
                Long.parseLong(
                        randomSeedField.getText()
                                .trim()
                );

        newConfig.motionMode =
                (MotionMode) motionModeBox.getSelectedItem();

        newConfig.psfSigma =
                Double.parseDouble(
                        psfSigmaField.getText()
                                .trim()
                );

        newConfig.amplitude =
                Double.parseDouble(
                        amplitudeField.getText()
                                .trim()
                );

        newConfig.background =
                Double.parseDouble(
                        backgroundField.getText()
                                .trim()
                );

        newConfig.noiseSigma =
                Double.parseDouble(
                        noiseSigmaField.getText()
                                .trim()
                );

        newConfig.confinementRadius =
                Double.parseDouble(
                        confinementRadiusField.getText()
                                .trim()
                );

        validateBasicConfig(
                newConfig
        );

        return newConfig;
    }

    private void validateBasicConfig(
            SimulationConfig config
    ) {
        if (config.width <= 0) {
            throw new IllegalArgumentException(
                    "图像宽度必须大于 0。"
            );
        }

        if (config.height <= 0) {
            throw new IllegalArgumentException(
                    "图像高度必须大于 0。"
            );
        }

        if (config.frames <= 0) {
            throw new IllegalArgumentException(
                    "帧数必须大于 0。"
            );
        }

        if (!config.useDensity && config.particleCount <= 0) {
            throw new IllegalArgumentException(
                    "固定粒子数模式下，粒子数必须大于 0。"
            );
        }

        if (config.useDensity && config.particleDensityPerUm2 <= 0) {
            throw new IllegalArgumentException(
                    "密度模式下，粒子密度必须大于 0。"
            );
        }

        if (config.pixelSizeUm <= 0) {
            throw new IllegalArgumentException(
                    "像素尺寸必须大于 0。"
            );
        }

        if (config.frameRateFps <= 0) {
            throw new IllegalArgumentException(
                    "帧率必须大于 0。"
            );
        }

        if (config.diffusionCoefficientUm2PerSecond < 0) {
            throw new IllegalArgumentException(
                    "扩散系数不能为负数。"
            );
        }

        if (config.psfSigma <= 0) {
            throw new IllegalArgumentException(
                    "PSF sigma 必须大于 0。"
            );
        }

        if (config.amplitude <= 0) {
            throw new IllegalArgumentException(
                    "粒子峰值强度必须大于 0。"
            );
        }

        if (config.background < 0) {
            throw new IllegalArgumentException(
                    "背景强度不能为负数。"
            );
        }

        if (config.noiseSigma < 0) {
            throw new IllegalArgumentException(
                    "噪声 sigma 不能为负数。"
            );
        }

        if (config.confinementRadius < 0) {
            throw new IllegalArgumentException(
                    "受限区域半径不能为负数。"
            );
        }
    }

    private String buildPreGenerateCheckReport(
            SimulationConfig config,
            SimulationScenario scenario,
            SimulationExportOptions exportOptions
    ) {
        StringBuilder report =
                new StringBuilder();

        report.append("生成前检查结果：通过\n\n");

        report.append("一、基础参数\n");
        report.append("图像尺寸：")
                .append(config.width)
                .append(" × ")
                .append(config.height)
                .append(" pixel\n");

        report.append("帧数：")
                .append(config.frames)
                .append("\n");

        if (config.useDensity) {
            report.append("粒子数量模式：密度模式\n");
            report.append("粒子密度：")
                    .append(config.particleDensityPerUm2)
                    .append(" particles/μm²\n");
            report.append("自动计算粒子数：")
                    .append(config.getResolvedParticleCount())
                    .append("\n");
        } else {
            report.append("粒子数量模式：固定粒子数\n");
            report.append("粒子数：")
                    .append(config.particleCount)
                    .append("\n");
        }

        report.append("像素尺寸：")
                .append(config.pixelSizeUm)
                .append(" μm/pixel\n");

        report.append("帧率：")
                .append(config.frameRateFps)
                .append(" fps\n");

        report.append("基础扩散系数：")
                .append(config.diffusionCoefficientUm2PerSecond)
                .append(" μm²/s\n");

        report.append("随机种子：")
                .append(config.randomSeed)
                .append("\n\n");

        report.append("二、成像参数\n");
        report.append("PSF sigma：")
                .append(config.psfSigma)
                .append(" pixel\n");

        report.append("粒子峰值强度：")
                .append(config.amplitude)
                .append("\n");

        report.append("背景强度：")
                .append(config.background)
                .append("\n");

        report.append("噪声 sigma：")
                .append(config.noiseSigma)
                .append("\n\n");

        report.append("三、运动时间轴\n");
        report.append("运动阶段数：")
                .append(scenario.getMotionSegments().size())
                .append("\n");

        for (MotionSegment segment : scenario.getMotionSegments()) {
            report.append("  - ")
                    .append(segment.getStartFrame())
                    .append("–")
                    .append(segment.getEndFrame())
                    .append(" 帧，")
                    .append(segment.getMotionMode())
                    .append("，D = ")
                    .append(segment.getDiffusionCoefficientUm2PerSecond())
                    .append(" μm²/s，标签：")
                    .append(segment.getLabel())
                    .append("\n");
        }

        report.append("\n四、可见性事件\n");
        report.append("事件数量：")
                .append(scenario.getVisibilityEvents().size())
                .append("\n");

        if (scenario.getVisibilityEvents().isEmpty()) {
            report.append("  - 无可见性事件\n");
        } else {
            for (VisibilityEvent event : scenario.getVisibilityEvents()) {
                report.append("  - ")
                        .append(event.getStartFrame())
                        .append("–")
                        .append(event.getEndFrame())
                        .append(" 帧，")
                        .append(event.getType())
                        .append("，p = ")
                        .append(event.getProbabilityPerFrame())
                        .append("，标签：")
                        .append(event.getLabel())
                        .append("\n");
            }
        }

        report.append("\n五、输出设置\n");
        report.append("已勾选输出文件数：")
                .append(countSelectedOutputOptions())
                .append(" / 10\n");

        report.append("\n将导出的核心文件：\n");

        if (exportOptions.exportMovie) {
            report.append("  - simulation_movie.tif\n");
        }

        if (exportOptions.exportGroundTruthDetections) {
            report.append("  - ground_truth_detections.csv\n");
        }

        if (exportOptions.exportGroundTruthTracks) {
            report.append("  - ground_truth_tracks.csv\n");
        }

        if (exportOptions.exportGroundTruthVisibility) {
            report.append("  - ground_truth_visibility.csv\n");
        }

        if (exportOptions.exportGroundTruthVisibilityEvents) {
            report.append("  - ground_truth_visibility_events.csv\n");
        }

        if (exportOptions.exportGroundTruthMotionSegments) {
            report.append("  - ground_truth_motion_segments.csv\n");
        }

        if (exportOptions.exportSimulationConfig) {
            report.append("  - simulation_config.json\n");
        }

        if (exportOptions.exportScenarioConfig) {
            report.append("  - scenario_config.json\n");
        }

        if (exportOptions.exportTheoreticalMsd) {
            report.append("  - theoretical_msd.csv\n");
        }

        if (exportOptions.exportGroundTruthMsd) {
            report.append("  - ground_truth_msd.csv\n");
        }

        report.append("\n检查结论：可以生成数据。\n");
        report.append("确认无误后，请点击“确认生成”。\n");

        return report.toString();
    }

    private void openPreGenerateCheckPage() {
        try {
            SimulationConfig checkedConfig =
                    buildConfigFromFields();

            SimulationScenario checkedScenario =
                    buildScenarioFromTimelineTable(
                            checkedConfig
                    );

            SimulationExportOptions checkedExportOptions =
                    buildExportOptionsFromCheckBoxes();

            if (countSelectedOutputOptions() <= 0) {
                throw new IllegalArgumentException(
                        "至少需要勾选一个输出文件。"
                );
            }

            pendingConfig =
                    checkedConfig;

            pendingScenario =
                    checkedScenario;

            pendingExportOptions =
                    checkedExportOptions;

            preGenerateCheckTextArea.setText(
                    buildPreGenerateCheckReport(
                            checkedConfig,
                            checkedScenario,
                            checkedExportOptions
                    )
            );

            preGenerateCheckTextArea.setCaretPosition(
                    0
            );

            if (confirmGenerateButton != null) {
                confirmGenerateButton.setEnabled(true);
            }

            showPage(
                    PAGE_PRE_GENERATE_CHECK
            );

        } catch (RuntimeException ex) {
            pendingConfig =
                    null;

            pendingScenario =
                    null;

            pendingExportOptions =
                    null;

            preGenerateCheckTextArea.setText(
                    "生成前检查结果：未通过\n\n"
                            + "错误原因：\n"
                            + ex.getMessage()
                            + "\n\n"
                            + "请返回总览页、运动时间轴页、可见性事件页或输出设置页修改参数。"
            );

            preGenerateCheckTextArea.setCaretPosition(
                    0
            );

            if (confirmGenerateButton != null) {
                confirmGenerateButton.setEnabled(false);
            }

            showPage(
                    PAGE_PRE_GENERATE_CHECK
            );
        }
    }

    private void confirmGenerationAfterCheck() {
        if (pendingConfig == null
                || pendingScenario == null
                || pendingExportOptions == null) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "当前没有通过检查的生成配置。\n\n请返回修改参数后重新检查。",
                    "无法生成",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        this.config =
                pendingConfig;

        this.scenario =
                pendingScenario;

        this.exportOptions =
                pendingExportOptions;

        this.action =
                DialogAction.SINGLE_DATASET;

        dispose();
    }

    private void loadStandardBrownianPreset() {
        widthField.setText("256");
        heightField.setText("256");
        framesField.setText("100");
        particleCountField.setText("8");

        useDensityCheckBox.setSelected(false);
        updateDensityControlState();

        densityField.setText("0.05");
        pixelSizeField.setText("0.1");
        frameRateField.setText("10.0");
        diffusionField.setText("0.05");
        randomSeedField.setText("12345");

        motionModeBox.setSelectedItem(
                MotionMode.FREE_BROWNIAN
        );

        psfSigmaField.setText("2.0");
        amplitudeField.setText("180.0");
        backgroundField.setText("20.0");
        noiseSigmaField.setText("8.0");
        confinementRadiusField.setText("80.0");

        if (motionTimelineTableModel != null) {
            motionTimelineTableModel.setRowCount(0);

            motionTimelineTableModel.addRow(
                    new Object[]{
                            1,
                            100,
                            "FREE_BROWNIAN",
                            0.05,
                            0.0,
                            0.0,
                            0.0,
                            "free_brownian"
                    }
            );
        }

        if (visibilityEventTableModel != null) {
            visibilityEventTableModel.setRowCount(0);
        }

        updateOverviewSummary();

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "已加载标准自由布朗运动参数。\n\n"
                        + "你可以返回总览页继续修改参数，或直接点击生成数据。",
                "预设已加载",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );

        showPage(
                PAGE_OVERVIEW
        );
    }

    private void loadBlinkingPhotobleachingPreset() {
        widthField.setText("256");
        heightField.setText("256");
        framesField.setText("100");
        particleCountField.setText("8");

        useDensityCheckBox.setSelected(false);
        updateDensityControlState();

        densityField.setText("0.05");
        pixelSizeField.setText("0.1");
        frameRateField.setText("10.0");
        diffusionField.setText("0.05");
        randomSeedField.setText("12345");

        motionModeBox.setSelectedItem(
                MotionMode.FREE_BROWNIAN
        );

        psfSigmaField.setText("2.0");
        amplitudeField.setText("180.0");
        backgroundField.setText("20.0");
        noiseSigmaField.setText("8.0");
        confinementRadiusField.setText("80.0");

        if (motionTimelineTableModel != null) {
            motionTimelineTableModel.setRowCount(0);

            motionTimelineTableModel.addRow(
                    new Object[]{
                            1,
                            100,
                            "FREE_BROWNIAN",
                            0.05,
                            0.0,
                            0.0,
                            0.0,
                            "free_brownian"
                    }
            );
        }

        if (visibilityEventTableModel != null) {
            visibilityEventTableModel.setRowCount(0);

            visibilityEventTableModel.addRow(
                    new Object[]{
                            1,
                            100,
                            "BLINKING",
                            0.05,
                            "blinking"
                    }
            );

            visibilityEventTableModel.addRow(
                    new Object[]{
                            70,
                            100,
                            "PHOTOBLEACHING",
                            0.01,
                            "photobleaching"
                    }
            );
        }

        updateOverviewSummary();

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "已加载闪烁 / 光漂白测试参数。\n\n"
                        + "当前设置包括：\n"
                        + "1. 1–100 帧自由布朗运动\n"
                        + "2. 1–100 帧 blinking, p = 0.05\n"
                        + "3. 70–100 帧 photobleaching, p = 0.01",
                "预设已加载",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );

        showPage(
                PAGE_OVERVIEW
        );
    }

    private void updateDensityControlState() {
        boolean useDensity =
                useDensityCheckBox.isSelected();
                
        // 勾选密度模式 → 粒子数标签+输入框 全部置灰禁用
        particleCountField.setEnabled(!useDensity);
        particleCountLabel.setEnabled(!useDensity);

        // 勾选密度模式 → 密度输入框+标签 启用；取消勾选则置灰
        densityField.setEnabled(useDensity);
        densityLabel.setEnabled(useDensity);

        if (useDensity) {
            densityModeNoteLabel.setText(
                    "密度模式：粒子数不参与"
            );

            densityModeNoteLabel.setToolTipText(
                    "<html>"
                            + "已启用密度模式。<br>"
                            + "此时程序会根据图像尺寸、像素尺寸和粒子密度自动计算粒子数。<br>"
                            + "因此，手动输入的“粒子数”不会参与本次数据生成。"
                            + "</html>"
            );

            particleCountField.setToolTipText(
                    "密度模式下，粒子数由粒子密度自动计算，此输入框不参与生成。"
            );

            densityField.setToolTipText(
                    "当前启用密度模式，此参数会参与粒子数量计算。"
            );

        } else {
            densityModeNoteLabel.setText(
                    "固定粒子数：密度不参与"
            );

            densityModeNoteLabel.setToolTipText(
                    "<html>"
                            + "已启用固定粒子数模式。<br>"
                            + "此时程序直接使用手动输入的粒子数生成模拟数据。<br>"
                            + "因此，“粒子密度”输入框不会参与本次数据生成。"
                            + "</html>"
            );

            particleCountField.setToolTipText(
                    "当前启用固定粒子数模式，此参数会参与数据生成。"
            );

            densityField.setToolTipText(
                    "固定粒子数模式下，粒子密度不参与本次数据生成。"
            );
        }
    }

    private String[] getMotionModeNames() {
        MotionMode[] modes =
                MotionMode.values();

        String[] names =
                new String[modes.length];

        for (int i = 0; i < modes.length; i++) {
            names[i] =
                    modes[i].name();
        }

        return names;
    }

    private int getFrameCountFromFieldOrDefault() {
        try {
            int frames =
                    Integer.parseInt(
                            framesField.getText()
                                    .trim()
                    );

            return Math.max(
                    1,
                    frames
            );

        } catch (RuntimeException ex) {
            return 100;
        }
    }

    private String[] getVisibilityEventTypeNames() {
        VisibilityEventType[] eventTypes =
                VisibilityEventType.values();

        String[] names =
                new String[eventTypes.length];

        for (int i = 0; i < eventTypes.length; i++) {
            names[i] =
                    eventTypes[i].name();
        }

        return names;
    }

    private Object[][] createDefaultMotionRows(
            int totalFrames
    ) {
        if (totalFrames < 4) {
            return new Object[][]{
                    {
                            1,
                            totalFrames,
                            "FREE_BROWNIAN",
                            0.05,
                            0.0,
                            0.0,
                            0.0,
                            "free"
                    }
            };
        }

        int end1 =
                Math.max(
                        1,
                        (int) Math.round(totalFrames * 0.30)
                );

        int end2 =
                Math.max(
                        end1 + 1,
                        (int) Math.round(totalFrames * 0.60)
                );

        int end3 =
                Math.max(
                        end2 + 1,
                        (int) Math.round(totalFrames * 0.75)
                );

        if (end3 >= totalFrames) {
            end3 =
                    totalFrames - 1;
        }

        if (end2 >= end3) {
            end2 =
                    end3 - 1;
        }

        if (end1 >= end2) {
            end1 =
                    end2 - 1;
        }

        return new Object[][]{
                {
                        1,
                        end1,
                        "FREE_BROWNIAN",
                        0.05,
                        0.0,
                        0.0,
                        0.0,
                        "free"
                },
                {
                        end1 + 1,
                        end2,
                        "CONFINED_BROWNIAN",
                        0.02,
                        0.0,
                        0.0,
                        80.0,
                        "confined"
                },
                {
                        end2 + 1,
                        end3,
                        "IMMOBILE",
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        "immobile"
                },
                {
                        end3 + 1,
                        totalFrames,
                        "DIRECTED_BROWNIAN",
                        0.03,
                        0.20,
                        0.0,
                        0.0,
                        "directed"
                }
        };
    }

    private JScrollPane createVisibilityEventTablePanel() {
        String[] columnNames =
                new String[]{
                        "起始帧",
                        "结束帧",
                        "事件类型",
                        "每帧概率",
                        "标签"
                };

        Object[][] defaultRows =
                new Object[][]{
                };

        visibilityEventTableModel =
                new DefaultTableModel(
                        defaultRows,
                        columnNames
                );

        visibilityEventTable =
                new JTable(
                        visibilityEventTableModel
                );

        visibilityEventTable.setFillsViewportHeight(true);
        visibilityEventTable.setRowHeight(26);

        JComboBox<String> eventTypeEditor =
                new JComboBox<>(
                        getVisibilityEventTypeNames()
                );

        TableColumn eventTypeColumn =
                visibilityEventTable
                        .getColumnModel()
                        .getColumn(2);

        eventTypeColumn.setCellEditor(
                new DefaultCellEditor(
                        eventTypeEditor
                )
        );

        visibilityEventTable
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(70);

        visibilityEventTable
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(70);

        visibilityEventTable
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(160);

        visibilityEventTable
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(100);

        visibilityEventTable
                .getColumnModel()
                .getColumn(4)
                .setPreferredWidth(120);

        JScrollPane scrollPane =
                new JScrollPane(
                        visibilityEventTable
                );

        scrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "可见性事件：闪烁、光漂白、强制不可见"
                )
        );

        return scrollPane;
    }

    private JPanel createVisibilityEventEditorPanel() {
        JPanel editorPanel =
                new JPanel(
                        new BorderLayout(
                                8,
                                8
                        )
                );

        editorPanel.add(
                createVisibilityEventTablePanel(),
                BorderLayout.CENTER
        );

        JPanel buttonPanel =
                new JPanel();

        JButton addBlinkingButton =
                new JButton("添加闪烁");

        JButton addPhotobleachingButton =
                new JButton("添加光漂白");

        JButton addForcedInvisibleButton =
                new JButton("添加强制不可见");

        JButton removeEventButton =
                new JButton("删除选中事件");

        JButton checkEventButton =
                new JButton("检查事件");

        addBlinkingButton.addActionListener(
                event -> addVisibilityEventRow(
                        VisibilityEventType.BLINKING
                )
        );

        addPhotobleachingButton.addActionListener(
                event -> addVisibilityEventRow(
                        VisibilityEventType.PHOTOBLEACHING
                )
        );

        addForcedInvisibleButton.addActionListener(
                event -> addVisibilityEventRow(
                        VisibilityEventType.FORCED_INVISIBLE
                )
        );

        removeEventButton.addActionListener(
                event -> removeSelectedVisibilityEventRow()
        );

        checkEventButton.addActionListener(
                event -> checkVisibilityEvents()
        );

        buttonPanel.add(addBlinkingButton);
        buttonPanel.add(addPhotobleachingButton);
        buttonPanel.add(addForcedInvisibleButton);
        buttonPanel.add(removeEventButton);
        buttonPanel.add(checkEventButton);

        editorPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        return editorPanel;
    }

    private JPanel createOutputOptionsPanel() {
        JPanel wrapperPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        wrapperPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "输出文件设置"
                )
        );

        JPanel groupPanel =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                12,
                                12
                        )
                );

        groupPanel.add(
                createOutputGroupPanel(
                        "图像文件",
                        new JCheckBox[]{
                                exportMovieCheckBox
                        }
                )
        );

        groupPanel.add(
                createOutputGroupPanel(
                        "检测与轨迹 Ground Truth",
                        new JCheckBox[]{
                                exportDetectionsCheckBox,
                                exportTracksCheckBox,
                                exportVisibilityCheckBox
                        }
                )
        );

        groupPanel.add(
                createOutputGroupPanel(
                        "实验设计文件",
                        new JCheckBox[]{
                                exportVisibilityEventsCheckBox,
                                exportMotionSegmentsCheckBox,
                                exportSimulationConfigCheckBox,
                                exportScenarioConfigCheckBox
                        }
                )
        );

        groupPanel.add(
                createOutputGroupPanel(
                        "MSD 参考文件",
                        new JCheckBox[]{
                                exportTheoreticalMsdCheckBox,
                                exportGroundTruthMsdCheckBox
                        }
                )
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        groupPanel
                );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        JButton selectAllButton =
                new JButton("全选");

        JButton deselectAllButton =
                new JButton("全不选");

        JButton recommendedButton =
                new JButton("推荐默认");

        selectAllButton.addActionListener(
                event -> {
                    setAllOutputOptions(
                            true
                    );

                    updateOverviewSummary();
                }
        );

        deselectAllButton.addActionListener(
                event -> {
                    setAllOutputOptions(
                            false
                    );

                    updateOverviewSummary();
                }
        );

        recommendedButton.addActionListener(
                event -> {
                    selectRecommendedOutputOptions();

                    updateOverviewSummary();
                }
        );

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(recommendedButton);

        wrapperPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        wrapperPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        return wrapperPanel;
    }

    private JPanel createOutputGroupPanel(
            String title,
            JCheckBox[] checkBoxes
    ) {
        JPanel panel =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                6,
                                6
                        )
                );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        title
                )
        );

        for (JCheckBox checkBox : checkBoxes) {
            panel.add(
                    checkBox
            );
        }

        return panel;
    }

    private void setAllOutputOptions(
            boolean selected
    ) {
        exportMovieCheckBox.setSelected(selected);
        exportDetectionsCheckBox.setSelected(selected);
        exportTracksCheckBox.setSelected(selected);
        exportVisibilityCheckBox.setSelected(selected);
        exportVisibilityEventsCheckBox.setSelected(selected);
        exportMotionSegmentsCheckBox.setSelected(selected);
        exportSimulationConfigCheckBox.setSelected(selected);
        exportScenarioConfigCheckBox.setSelected(selected);
        exportTheoreticalMsdCheckBox.setSelected(selected);
        exportGroundTruthMsdCheckBox.setSelected(selected);
    }

    private void selectRecommendedOutputOptions() {
        // 图像文件：保留
        exportMovieCheckBox.setSelected(true);

        // 检测与轨迹 ground truth：保留核心文件
        exportDetectionsCheckBox.setSelected(true);
        exportTracksCheckBox.setSelected(true);
        exportVisibilityCheckBox.setSelected(false);

        // 实验设计文件：只保留完整 scenario 配置
        exportVisibilityEventsCheckBox.setSelected(false);
        exportMotionSegmentsCheckBox.setSelected(false);
        exportSimulationConfigCheckBox.setSelected(false);
        exportScenarioConfigCheckBox.setSelected(true);

        // MSD 参考文件：保留 ground truth MSD，理论 MSD 暂时不作为默认核心输出
        exportTheoreticalMsdCheckBox.setSelected(false);
        exportGroundTruthMsdCheckBox.setSelected(true);
    }

    private SimulationExportOptions buildExportOptionsFromCheckBoxes() {
        SimulationExportOptions options =
                new SimulationExportOptions();

        options.exportMovie =
                exportMovieCheckBox.isSelected();

        options.exportGroundTruthDetections =
                exportDetectionsCheckBox.isSelected();

        options.exportGroundTruthTracks =
                exportTracksCheckBox.isSelected();

        options.exportGroundTruthVisibility =
                exportVisibilityCheckBox.isSelected();

        options.exportGroundTruthVisibilityEvents =
                exportVisibilityEventsCheckBox.isSelected();

        options.exportGroundTruthMotionSegments =
                exportMotionSegmentsCheckBox.isSelected();

        options.exportSimulationConfig =
                exportSimulationConfigCheckBox.isSelected();

        options.exportScenarioConfig =
                exportScenarioConfigCheckBox.isSelected();

        options.exportTheoreticalMsd =
                exportTheoreticalMsdCheckBox.isSelected();

        options.exportGroundTruthMsd =
                exportGroundTruthMsdCheckBox.isSelected();

        return options;
    }

    private void addVisibilityEventRow(
            VisibilityEventType eventType
    ) {
        int totalFrames =
                getFrameCountFromFieldOrDefault();

        double probability =
                0.05;

        String label =
                "blinking";

        if (eventType == VisibilityEventType.PHOTOBLEACHING) {
            probability =
                    0.01;

            label =
                    "photobleaching";
        }

        if (eventType == VisibilityEventType.FORCED_INVISIBLE) {
            probability =
                    1.0;

            label =
                    "forced_invisible";
        }

        visibilityEventTableModel.addRow(
                new Object[]{
                        1,
                        totalFrames,
                        eventType.name(),
                        probability,
                        label
                }
        );
    }

    private void removeSelectedVisibilityEventRow() {
        int selectedRow =
                visibilityEventTable.getSelectedRow();

        if (selectedRow < 0) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "请先在可见性事件表格中选中一行。",
                    "未选择事件",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        visibilityEventTableModel.removeRow(
                selectedRow
        );
    }

    private void resetMotionTimelineByFrameCount() {
        int totalFrames =
                getFrameCountFromFieldOrDefault();

        Object[][] rows =
                createDefaultMotionRows(
                        totalFrames
                );

        motionTimelineTableModel.setRowCount(
                0
        );

        for (Object[] row : rows) {
            motionTimelineTableModel.addRow(
                    row
            );
        }
    }

    private void applyFrameCountToTimelineWithConfirm() {
        int totalFrames =
                getFrameCountFromFieldOrDefault();

        if (motionTimelineTableModel == null) {
            updateOverviewSummary();
            return;
        }

        int choice =
                javax.swing.JOptionPane.showConfirmDialog(
                        this,
                        "将根据当前总帧数重新生成运动时间轴。\n\n"
                                + "当前总帧数："
                                + totalFrames
                                + "\n\n"
                                + "注意：这会覆盖你当前手动编辑的运动时间轴。\n"
                                + "是否继续？",
                        "同步运动时间轴",
                        javax.swing.JOptionPane.YES_NO_OPTION
                );

        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            updateOverviewSummary();
            return;
        }

        resetMotionTimelineByFrameCount();

        clampVisibilityEventsToFrameCount(
                totalFrames
        );

        updateOverviewSummary();

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "运动时间轴已根据总帧数重新生成。\n\n"
                        + "当前总帧数："
                        + totalFrames,
                "同步完成",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void clampVisibilityEventsToFrameCount(
            int totalFrames
    ) {
        if (visibilityEventTableModel == null) {
            return;
        }

        for (
                int row = 0;
                row < visibilityEventTableModel.getRowCount();
                row++
        ) {
            int startFrame =
                    Integer.parseInt(
                            visibilityEventTableModel
                                    .getValueAt(row, 0)
                                    .toString()
                                    .trim()
                    );

            int endFrame =
                    Integer.parseInt(
                            visibilityEventTableModel
                                    .getValueAt(row, 1)
                                    .toString()
                                    .trim()
                    );

            if (startFrame > totalFrames) {
                startFrame =
                        totalFrames;
            }

            if (endFrame > totalFrames) {
                endFrame =
                        totalFrames;
            }

            if (endFrame < startFrame) {
                endFrame =
                        startFrame;
            }

            visibilityEventTableModel.setValueAt(
                    startFrame,
                    row,
                    0
            );

            visibilityEventTableModel.setValueAt(
                    endFrame,
                    row,
                    1
            );
        }
    }

    private JScrollPane createMotionTimelineTablePanel() {
        String[] columnNames =
                new String[]{
                        "起始帧",
                        "结束帧",
                        "运动模式",
                        "D μm²/s",
                        "Vx μm/s",
                        "Vy μm/s",
                        "受限半径 pixel",
                        "标签"
                };

                Object[][] defaultRows =
                        createDefaultMotionRows(
                                getFrameCountFromFieldOrDefault()
                        );

        motionTimelineTableModel =
                new DefaultTableModel(
                        defaultRows,
                        columnNames
                );

        motionTimelineTable =
                new JTable(
                        motionTimelineTableModel
                );

        motionTimelineTable.setFillsViewportHeight(true);

        JComboBox<String> motionModeEditor =
                new JComboBox<>(
                        getMotionModeNames()
                );

        TableColumn motionModeColumn =
                motionTimelineTable
                        .getColumnModel()
                        .getColumn(2);

        motionModeColumn.setCellEditor(
                new DefaultCellEditor(
                        motionModeEditor
                )
        );

        motionTimelineTable.setRowHeight(26);

        motionTimelineTable
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(60);

        motionTimelineTable
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(60);

        motionTimelineTable
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(160);

        motionTimelineTable
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(80);

        motionTimelineTable
                .getColumnModel()
                .getColumn(4)
                .setPreferredWidth(80);

        motionTimelineTable
                .getColumnModel()
                .getColumn(5)
                .setPreferredWidth(80);

        motionTimelineTable
                .getColumnModel()
                .getColumn(6)
                .setPreferredWidth(110);

        motionTimelineTable
                .getColumnModel()
                .getColumn(7)
                .setPreferredWidth(100);

        JScrollPane scrollPane =
                new JScrollPane(
                        motionTimelineTable
                );

        scrollPane.setBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        "运动时间轴：第几帧到第几帧使用哪种运动模式"
                )
        );

        return scrollPane;
    }

    private JPanel createMotionTimelineEditorPanel() {
        JPanel editorPanel =
                new JPanel(
                        new BorderLayout(
                                8,
                                8
                        )
                );

        editorPanel.add(
                createMotionTimelineTablePanel(),
                BorderLayout.CENTER
        );

        JPanel buttonPanel =
                new JPanel();

        JButton addSegmentButton =
                new JButton("添加阶段");

        JButton removeSegmentButton =
                new JButton("删除选中阶段");

        JButton checkTimelineButton =
                new JButton("检查时间轴");

        JButton resetTimelineButton =
                new JButton("按帧数重置时间轴");

        addSegmentButton.addActionListener(
                event -> addMotionSegmentRow()
        );

        removeSegmentButton.addActionListener(
                event -> removeSelectedMotionSegmentRow()
        );

        checkTimelineButton.addActionListener(
                event -> checkMotionTimeline()
        );

        resetTimelineButton.addActionListener(
                event -> resetMotionTimelineByFrameCount()
        );

        buttonPanel.add(addSegmentButton);
        buttonPanel.add(removeSegmentButton);
        buttonPanel.add(checkTimelineButton);
        buttonPanel.add(resetTimelineButton);

        editorPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        return editorPanel;
    }

    private void addMotionSegmentRow() {
        int totalFrames =
                getFrameCountFromFieldOrDefault();

        int rowCount =
                motionTimelineTableModel.getRowCount();

        int startFrame =
                1;

        if (rowCount > 0) {
            Object lastEndFrameValue =
                    motionTimelineTableModel.getValueAt(
                            rowCount - 1,
                            1
                    );

            startFrame =
                    Integer.parseInt(
                            lastEndFrameValue
                                    .toString()
                                    .trim()
                    ) + 1;
        }

        if (startFrame > totalFrames) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "已经覆盖到总帧数，不能继续添加阶段。\n\n"
                            + "总帧数："
                            + totalFrames,
                    "无法添加阶段",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int endFrame =
                Math.min(
                        startFrame + 9,
                        totalFrames
                );

        motionTimelineTableModel.addRow(
                new Object[]{
                        startFrame,
                        endFrame,
                        "FREE_BROWNIAN",
                        0.05,
                        0.0,
                        0.0,
                        0.0,
                        "new_segment"
                }
        );
    }

    private void removeSelectedMotionSegmentRow() {
        int selectedRow =
                motionTimelineTable.getSelectedRow();

        if (selectedRow < 0) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "请先在运动时间轴表格中选中一行。",
                    "未选择阶段",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        motionTimelineTableModel.removeRow(
                selectedRow
        );
    }

    private void stopMotionTimelineCellEditing() {
        if (motionTimelineTable != null
                && motionTimelineTable.isEditing()
                && motionTimelineTable.getCellEditor() != null) {

            motionTimelineTable
                    .getCellEditor()
                    .stopCellEditing();
        }
    }

    private void stopVisibilityEventCellEditing() {
        if (visibilityEventTable != null
                && visibilityEventTable.isEditing()
                && visibilityEventTable.getCellEditor() != null) {

            visibilityEventTable
                    .getCellEditor()
                    .stopCellEditing();
        }
    }

    private void checkMotionTimeline() {
        try {
            SimulationConfig tempConfig =
                    SimulationConfig.defaultConfig();

            tempConfig.frames =
                    Integer.parseInt(
                            framesField.getText()
                                    .trim()
                    );

            buildScenarioFromTimelineTable(
                    tempConfig
            );

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "运动时间轴检查通过。\n\n"
                            + "所有帧均已覆盖，且不存在重叠阶段。",
                    "检查通过",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "运动时间轴存在问题：\n\n"
                            + ex.getMessage(),
                    "时间轴错误",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private SimulationScenario buildScenarioFromTimelineTable(
            SimulationConfig config
    ) {
        stopMotionTimelineCellEditing();
        stopVisibilityEventCellEditing();
        
        SimulationScenario newScenario =
                new SimulationScenario(
                        config
                );

        for (
                int row = 0;
                row < motionTimelineTableModel.getRowCount();
                row++
        ) {
            int startFrame =
                    Integer.parseInt(
                            motionTimelineTableModel
                                    .getValueAt(row, 0)
                                    .toString()
                                    .trim()
                    );

            int endFrame =
                    Integer.parseInt(
                            motionTimelineTableModel
                                    .getValueAt(row, 1)
                                    .toString()
                                    .trim()
                    );

            MotionMode motionMode =
                    MotionMode.valueOf(
                            motionTimelineTableModel
                                    .getValueAt(row, 2)
                                    .toString()
                                    .trim()
                    );

            double diffusionCoefficient =
                    Double.parseDouble(
                            motionTimelineTableModel
                                    .getValueAt(row, 3)
                                    .toString()
                                    .trim()
                    );

            double vx =
                    Double.parseDouble(
                            motionTimelineTableModel
                                    .getValueAt(row, 4)
                                    .toString()
                                    .trim()
                    );

            double vy =
                    Double.parseDouble(
                            motionTimelineTableModel
                                    .getValueAt(row, 5)
                                    .toString()
                                    .trim()
                    );

            double confinementRadius =
                    Double.parseDouble(
                            motionTimelineTableModel
                                    .getValueAt(row, 6)
                                    .toString()
                                    .trim()
                    );

            String label =
                    motionTimelineTableModel
                            .getValueAt(row, 7)
                            .toString()
                            .trim();

            MotionSegment segment =
                    new MotionSegment(
                            startFrame,
                            endFrame,
                            motionMode,
                            diffusionCoefficient,
                            vx,
                            vy,
                            confinementRadius,
                            label
                    );

            newScenario.addMotionSegment(
                    segment
            );
        }
    for (
            int row = 0;
            row < visibilityEventTableModel.getRowCount();
            row++
    ) {
        int startFrame =
                Integer.parseInt(
                        visibilityEventTableModel
                                .getValueAt(row, 0)
                                .toString()
                                .trim()
                );

        int endFrame =
                Integer.parseInt(
                        visibilityEventTableModel
                                .getValueAt(row, 1)
                                .toString()
                                .trim()
                );

        VisibilityEventType eventType =
                VisibilityEventType.valueOf(
                        visibilityEventTableModel
                                .getValueAt(row, 2)
                                .toString()
                                .trim()
                );

        double probabilityPerFrame =
                Double.parseDouble(
                        visibilityEventTableModel
                                .getValueAt(row, 3)
                                .toString()
                                .trim()
                );

        String label =
                visibilityEventTableModel
                        .getValueAt(row, 4)
                        .toString()
                        .trim();

        VisibilityEvent visibilityEvent =
                new VisibilityEvent(
                        startFrame,
                        endFrame,
                        eventType,
                        probabilityPerFrame,
                        label
                );

        newScenario.addVisibilityEvent(
                visibilityEvent
        );
    }

        newScenario.validate();

        return newScenario;
    }

    //构造方法，调用父类JDialog构造，1.绑定父窗口owner;2.弹窗标题：模拟数据生成器；
    // 3.true，模态弹窗：弹窗不关闭，不能操作后面主界面；
    public SimulationSetupDialog(JFrame owner) {
        super(owner, "模拟数据生成器", true);

        setSize(1100, 820);
         //弹窗自动在父窗口居中
        setLocationRelativeTo(owner);
       //BorderLayout：边界布局
        setLayout(new BorderLayout());

        JPanel formPanel =
                new JPanel(new GridLayout(0, 2, 8, 8));

        formPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        formPanel.add(new JLabel("图像宽度 pixel："));
        formPanel.add(widthField);

        formPanel.add(new JLabel("图像高度 pixel："));
        formPanel.add(heightField);

        JPanel framesPanel =
                new JPanel(
                        new BorderLayout(
                                6,
                                0
                        )
                );

        JButton syncTimelineButton =
                new JButton("同步时间轴");

        framesPanel.add(
                framesField,
                BorderLayout.CENTER
        );

        framesPanel.add(
                syncTimelineButton,
                BorderLayout.EAST
        );

        syncTimelineButton.addActionListener(
                event -> applyFrameCountToTimelineWithConfirm()
        );

        framesField.addActionListener(
                event -> applyFrameCountToTimelineWithConfirm()
        );

        formPanel.add(
                new JLabel("帧数：")
        );

        formPanel.add(
                framesPanel
        );

        formPanel.add(particleCountLabel);
        formPanel.add(particleCountField);

        formPanel.add(new JLabel("是否使用密度："));
        formPanel.add(useDensityCheckBox);

        formPanel.add(densityLabel);
        formPanel.add(densityField);

        formPanel.add(new JLabel("说明："));
        formPanel.add(densityModeNoteLabel);

        particleCountField.setToolTipText(
                "密度模式下该参数不参与数据生成。"
        );

        densityField.setToolTipText(
                "固定粒子数模式下该参数不参与数据生成。"
        );

        useDensityCheckBox.addActionListener(
                event -> updateDensityControlState()
        );

        updateDensityControlState();   

        formPanel.add(new JLabel("像素尺寸 μm/pixel："));
        formPanel.add(pixelSizeField);

        formPanel.add(new JLabel("帧率 fps："));
        formPanel.add(frameRateField);

        formPanel.add(new JLabel("扩散系数 μm²/s："));
        formPanel.add(diffusionField);

        formPanel.add(new JLabel("随机种子："));
        formPanel.add(randomSeedField);

        formPanel.add(new JLabel("粒子运动模式："));
        formPanel.add(motionModeBox);

        formPanel.add(new JLabel("PSF sigma pixel："));
        formPanel.add(psfSigmaField);

        formPanel.add(new JLabel("粒子峰值强度："));
        formPanel.add(amplitudeField);

        formPanel.add(new JLabel("背景强度："));
        formPanel.add(backgroundField);

        formPanel.add(new JLabel("噪声 sigma："));
        formPanel.add(noiseSigmaField);

        formPanel.add(new JLabel("受限区域半径 pixel："));
        formPanel.add(confinementRadiusField);

        JPanel motionTimelinePage =
                createMotionTimelinePage();

        JPanel visibilityEventsPage =
                createVisibilityEventsPage();

        JPanel outputOptionsPage =
                createOutputOptionsPage();

        JPanel preGenerateCheckPage =
                createPreGenerateCheckPage();


        JPanel presetsPage =
                createPresetsPage();


        JPanel overviewPage =
                createOverviewPage(
                        formPanel
                );

        cardPanel.add(
                overviewPage,
                PAGE_OVERVIEW
        );

        cardPanel.add(
                motionTimelinePage,
                PAGE_MOTION_TIMELINE
        );

        cardPanel.add(
                visibilityEventsPage,
                PAGE_VISIBILITY_EVENTS
        );

        cardPanel.add(
                outputOptionsPage,
                PAGE_OUTPUT_OPTIONS
        );

        cardPanel.add(
                preGenerateCheckPage,
                PAGE_PRE_GENERATE_CHECK
        );

        cardPanel.add(
                presetsPage,
                PAGE_PRESETS
        );

        add(
                cardPanel,
                BorderLayout.CENTER
        );

        showPage(
                PAGE_OVERVIEW
        );
    }

    private void checkVisibilityEvents() {
        try {
            SimulationConfig tempConfig =
                    SimulationConfig.defaultConfig();

            tempConfig.frames =
                    Integer.parseInt(
                            framesField.getText()
                                    .trim()
                    );

            SimulationScenario tempScenario =
                    new SimulationScenario(
                            tempConfig
                    );

            tempScenario.addMotionSegment(
                    MotionSegment.freeBrownian(
                            1,
                            tempConfig.frames,
                            0.05
                    )
            );

            for (
                    int row = 0;
                    row < visibilityEventTableModel.getRowCount();
                    row++
            ) {
                int startFrame =
                        Integer.parseInt(
                                visibilityEventTableModel
                                        .getValueAt(row, 0)
                                        .toString()
                                        .trim()
                        );

                int endFrame =
                        Integer.parseInt(
                                visibilityEventTableModel
                                        .getValueAt(row, 1)
                                        .toString()
                                        .trim()
                        );

                VisibilityEventType eventType =
                        VisibilityEventType.valueOf(
                                visibilityEventTableModel
                                        .getValueAt(row, 2)
                                        .toString()
                                        .trim()
                        );

                double probabilityPerFrame =
                        Double.parseDouble(
                                visibilityEventTableModel
                                        .getValueAt(row, 3)
                                        .toString()
                                        .trim()
                        );

                String label =
                        visibilityEventTableModel
                                .getValueAt(row, 4)
                                .toString()
                                .trim();

                tempScenario.addVisibilityEvent(
                        new VisibilityEvent(
                                startFrame,
                                endFrame,
                                eventType,
                                probabilityPerFrame,
                                label
                        )
                );
            }

            tempScenario.validate();

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "可见性事件检查通过。",
                    "检查通过",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "可见性事件存在问题：\n\n"
                            + ex.getMessage(),
                    "事件错误",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void onConfirm() {
        openPreGenerateCheckPage();
    }

    public static DialogResult showDialog(JFrame owner) {
        SimulationSetupDialog dialog =
                new SimulationSetupDialog(owner);

        dialog.setVisible(true);

        return new DialogResult(
                dialog.action,
                dialog.config,
                dialog.scenario,
                dialog.exportOptions
        );
    }
}
