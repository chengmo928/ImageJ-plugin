package io.github.zhengfangfang0304.particletracking.gui;

import io.github.zhengfangfang0304.particletracking.simulation.MotionMode;
import io.github.zhengfangfang0304.particletracking.simulation.SimulationConfig;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.GridLayout;

//模拟数据生成参数设置窗口。
//本文件唯一的类：SimulationSetupDialog
public class SimulationSetupDialog extends JDialog {

        public enum DialogAction {
        CANCEL,
        SINGLE_DATASET,
        STANDARD_BROWNIAN_BATCH
        }

        public static class DialogResult {

        private final DialogAction action;
        private final SimulationConfig config;

        public DialogResult(
                DialogAction action,
                SimulationConfig config
        ) {
                this.action = action;
                this.config = config;
        }

        public DialogAction getAction() {
                return action;
        }

        public SimulationConfig getConfig() {
                return config;
        }
        }

    private DialogAction action = DialogAction.CANCEL;

    private final JTextField widthField = new JTextField("256");

    private final JTextField heightField = new JTextField("256");

    private final JTextField framesField = new JTextField("30");

    private final JLabel particleCountLabel = new JLabel("粒子数：");

    private final JTextField particleCountField = new JTextField("8");

    private final JCheckBox useDensityCheckBox = new JCheckBox("根据粒子密度自动计算粒子数");

    private final JLabel densityLabel = new JLabel("粒子密度 particles/μm²：");

    private final JTextField densityField = new JTextField("0.05");

    private final JLabel densityModeNoteLabel = new JLabel();

    private final JTextField pixelSizeField = new JTextField("0.1");

    private final JTextField frameRateField = new JTextField("10.0");

    private final JTextField diffusionField = new JTextField("0.05");

    private final JComboBox<MotionMode> motionModeBox = new JComboBox<>(MotionMode.values());

    private final JTextField psfSigmaField = new JTextField("2.0");

    private final JTextField amplitudeField = new JTextField("180.0");

    private final JTextField backgroundField = new JTextField("20.0");

    private final JTextField noiseSigmaField = new JTextField("8.0");

    private final JTextField confinementRadiusField = new JTextField("80.0");

    private SimulationConfig config;

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

    //构造方法，调用父类JDialog构造，1.绑定父窗口owner;2.弹窗标题：模拟数据生成器；
    // 3.true，模态弹窗：弹窗不关闭，不能操作后面主界面；
    public SimulationSetupDialog(JFrame owner) {
        super(owner, "模拟数据生成器", true);

        setSize(520, 620);
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

        formPanel.add(new JLabel("帧数："));
        formPanel.add(framesField);

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

        add(formPanel, BorderLayout.CENTER);

        //底部按钮面板
        JPanel buttonPanel = new JPanel();

        JButton okButton = new JButton("生成数据");
        JButton standardBrownianButton = new JButton("标准布朗批量数据集");

        JButton cancelButton = new JButton("取消");

        standardBrownianButton.addActionListener(event -> {
            action = 
                    DialogAction.STANDARD_BROWNIAN_BATCH;
            dispose();
        });

        okButton.addActionListener(event -> onConfirm());

        cancelButton.addActionListener(event -> {
            action = DialogAction.CANCEL;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(standardBrownianButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    //核心确认逻辑方法
    private void onConfirm() {
        try {
            SimulationConfig newConfig =
                    SimulationConfig.defaultConfig();

            newConfig.width =
                    Integer.parseInt(widthField.getText());

            newConfig.height =
                    Integer.parseInt(heightField.getText());

            newConfig.frames =
                    Integer.parseInt(framesField.getText());

            newConfig.particleCount =
                    Integer.parseInt(particleCountField.getText());

            newConfig.useDensity =
                    useDensityCheckBox.isSelected();

            newConfig.particleDensityPerUm2 =
                    Double.parseDouble(densityField.getText());

            newConfig.pixelSizeUm =
                    Double.parseDouble(pixelSizeField.getText());

            newConfig.frameRateFps =
                    Double.parseDouble(frameRateField.getText());

            newConfig.diffusionCoefficientUm2PerSecond =
                    Double.parseDouble(diffusionField.getText());

            newConfig.motionMode =
                    (MotionMode) motionModeBox.getSelectedItem();

            newConfig.psfSigma =
                    Double.parseDouble(psfSigmaField.getText());

            newConfig.amplitude =
                    Double.parseDouble(amplitudeField.getText());

            newConfig.background =
                    Double.parseDouble(backgroundField.getText());

            newConfig.noiseSigma =
                    Double.parseDouble(noiseSigmaField.getText());

            newConfig.confinementRadius =
                    Double.parseDouble(confinementRadiusField.getText());

            this.config = newConfig;
            this.action = DialogAction.SINGLE_DATASET;

            dispose();

        //异常捕获，try-catch
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "参数格式错误，请检查是否输入了有效数字。",
                    "参数错误",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static DialogResult showDialog(JFrame owner) {
        SimulationSetupDialog dialog =
                new SimulationSetupDialog(owner);

        dialog.setVisible(true);

        return new DialogResult(
                dialog.action,
                dialog.config
        );
    }
}
