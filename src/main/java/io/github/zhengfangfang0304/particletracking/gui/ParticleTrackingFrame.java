package io.github.zhengfangfang0304.particletracking.gui;

import io.github.zhengfangfang0304.particletracking.controller
        .ParticleTrackingController;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

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

    /**
     * 创建主窗口。
     *
     * @param controller 程序控制器
     */
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
    }

    /**
     * 初始化窗口基础结构。
     */
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

        /*
         * 当前只是迁移骨架。
         * 后面再把Simple_GUI中的按钮面板移动到这里。
         */
        JLabel migrationMessage =
                new JLabel(
                        "GUI模块迁移中",
                        SwingConstants.CENTER
                );

        migrationMessage.setFont(chineseFont);

        logArea.setEditable(false);
        logArea.setFont(
                new Font(
                        "Microsoft YaHei",
                        Font.PLAIN,
                        14
                )
        );

        JScrollPane logScrollPane =
                new JScrollPane(logArea);

        logScrollPane.setPreferredSize(
                new Dimension(800, 150)
        );

        add(title, BorderLayout.NORTH);
        add(migrationMessage, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }

    /**
     * 向界面日志框追加文字。
     */
    public void appendLog(String message) {
        logArea.append(message);
    }

    /**
     * 返回当前控制器。
     */
    public ParticleTrackingController getController() {
        return controller;
    }
}