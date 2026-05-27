package com.yourname.imagejgui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Simple_GUI implements PlugIn {

    private final Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 16);
    private final Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 22);

    private JComboBox<String> denoiseMethodBox;
    private JTextField denoiseParameterField;

    @Override
    public void run(String arg) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {

        JFrame frame = new JFrame("My ImageJ GUI Plugin");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel("单颗粒分子识别 GUI Demo", SwingConstants.CENTER);
        title.setFont(titleFont);

        JButton generateButton = new JButton("生成测试图像");
        JButton imageButton = new JButton("读取当前图像");
        JButton denoiseButton = new JButton("执行降噪");
        JButton detectButton = new JButton("识别颗粒");
        JButton trackButton = new JButton("简单追踪");
        JButton closeButton = new JButton("关闭");

        generateButton.setFont(chineseFont);
        imageButton.setFont(chineseFont);
        denoiseButton.setFont(chineseFont);
        detectButton.setFont(chineseFont);
        trackButton.setFont(chineseFont);
        closeButton.setFont(chineseFont);

        denoiseMethodBox = new JComboBox<>(new String[]{
                "Gaussian Blur 高斯滤波",
                "Median Filter 中值滤波"
        });
        denoiseMethodBox.setFont(chineseFont);

        denoiseParameterField = new JTextField("1.0", 6);
        denoiseParameterField.setFont(chineseFont);

        JLabel methodLabel = new JLabel("降噪方法：");
        methodLabel.setFont(chineseFont);

        JLabel parameterLabel = new JLabel("参数：");
        parameterLabel.setFont(chineseFont);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        generateButton.addActionListener(e -> generateTestImage(logArea));

        imageButton.addActionListener(e -> readCurrentImage(logArea));

        denoiseButton.addActionListener(e -> denoiseCurrentImage(logArea));

        detectButton.addActionListener(e -> {
            logArea.append("识别颗粒功能将在 v0.4 添加。\n\n");
        });

        trackButton.addActionListener(e -> {
            logArea.append("简单追踪功能将在 v0.5 添加。\n\n");
        });

        closeButton.addActionListener(e -> frame.dispose());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel row1 = new JPanel();
        row1.add(generateButton);
        row1.add(imageButton);
        row1.add(denoiseButton);

        JPanel row2 = new JPanel();
        row2.add(methodLabel);
        row2.add(denoiseMethodBox);
        row2.add(parameterLabel);
        row2.add(denoiseParameterField);

        JPanel row3 = new JPanel();
        row3.add(detectButton);
        row3.add(trackButton);
        row3.add(closeButton);

        controlPanel.add(row1);
        controlPanel.add(row2);
        controlPanel.add(row3);

        frame.setLayout(new BorderLayout());
        frame.add(title, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.CENTER);
        frame.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        frame.setVisible(true);
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
}