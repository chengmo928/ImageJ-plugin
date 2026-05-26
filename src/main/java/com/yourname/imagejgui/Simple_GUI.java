package com.yourname.imagejgui;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.awt.*;

public class Simple_GUI implements PlugIn {

    @Override
    public void run(String arg) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {

        JFrame frame = new JFrame("My ImageJ GUI Plugin");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setSize(450, 300);

        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel(
                "ImageJ GUI Demo",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton imageButton = new JButton("读取当前图像");

        JButton denoiseButton = new JButton("降噪测试");

        JButton closeButton = new JButton("关闭");

        Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 16);

        imageButton.setFont(chineseFont);
        denoiseButton.setFont(chineseFont);
        closeButton.setFont(chineseFont);

        JTextArea logArea = new JTextArea();

        logArea.setEditable(false);

        imageButton.addActionListener(e -> {

            try {

                ImagePlus imp = IJ.getImage();

                logArea.append(
                        "当前图像：" + imp.getTitle() + "\n"
                );

                logArea.append(
                        "宽度：" + imp.getWidth() + "\n"
                );

                logArea.append(
                        "高度：" + imp.getHeight() + "\n"
                );

            } catch (Exception ex) {

                logArea.append(
                        "请先在 ImageJ/Fiji 中打开图像。\n"
                );
            }
        });

        denoiseButton.addActionListener(e -> {

            try {

                ImagePlus imp = IJ.getImage();

                IJ.run(
                        imp,
                        "Median...",
                        "radius=2"
                );

                logArea.append(
                        "已执行 Median 降噪。\n"
                );

            } catch (Exception ex) {

                logArea.append(
                        "请先打开图像。\n"
                );
            }
        });

        closeButton.addActionListener(e -> frame.dispose());

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(imageButton);

        buttonPanel.add(denoiseButton);

        buttonPanel.add(closeButton);

        frame.setLayout(new BorderLayout());

        frame.add(title, BorderLayout.NORTH);

        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}