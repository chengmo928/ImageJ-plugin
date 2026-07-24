package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.*;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * 基于 OpenCV 双边滤波的降噪器。
 * 支持 8 位和 16 位灰度图像，以及 8 位彩色图像。
 * <p>
 * 参数说明：{@code parameter} 作为 sigmaColor 的基准值（针对 8 位图）。
 * 对于 16 位图，会根据图像实际最大值自动缩放 sigmaColor。
 * sigmaSpace 自动取 sigmaColor / 2，滤波窗口直径 d 自适应计算。
 * </p>
 */
public final class BilateralDenoiser implements ImageDenoiser {

    static {
        OpenCV.loadLocally();
    }

    @Override
    public String getName() {
        return "Bilateral Filter 双边滤波 (OpenCV)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException(
                    "双边滤波参数 (sigmaColor) 必须是大于 0 的有限数字。"
            );
        }

        // 获取第一帧的位深度和最大值（用于参数缩放）
        ImageProcessor firstIp = image.getProcessor();
        int bitDepth = firstIp.getBitDepth();
        double maxVal = firstIp.getMax(); // 实际最大值
        double scale = 1.0;
        if (bitDepth == 16 && maxVal > 0) {
            scale = maxVal / 255.0; // 将 8 位范围的参数缩放到 16 位范围
        } else if (bitDepth == 32) {
            // 对于 32 位，可能需要更复杂的缩放，这里简单处理
            scale = maxVal / 255.0;
        }
        // 对于 8 位，scale 保持 1.0

        // 对参数进行缩放
        double sigmaColor = parameter * scale;
        double sigmaSpace = sigmaColor / 2.0;
        int d = (int) Math.max(3, Math.ceil(sigmaColor * 2));

        // 可选：打印缩放信息，便于调试
        IJ.log("Bilateral: 位深度=" + bitDepth + ", maxVal=" + maxVal + ", scale=" + scale + ", 实际sigmaColor=" + sigmaColor);

        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Bilateral Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor ip = stack.getProcessor(slice);

            if (ip instanceof ColorProcessor) {
                Mat srcMat = colorProcessorToMat((ColorProcessor) ip);
                Mat dstMat = new Mat();
                try {
                    Imgproc.bilateralFilter(srcMat, dstMat, d, sigmaColor, sigmaSpace);
                    ColorProcessor result = matToColorProcessor(dstMat);
                    stack.setProcessor(result, slice);
                } finally {
                    srcMat.release();
                    dstMat.release();
                }
            } else {
                // 灰度图像（8/16/32 位）
                int depth = ip.getBitDepth();
                Mat srcMat = grayProcessorToMat(ip, depth);
                Mat dstMat = new Mat();
                try {
                    Imgproc.bilateralFilter(srcMat, dstMat, d, sigmaColor, sigmaSpace);
                    ImageProcessor result = matToGrayProcessor(dstMat, depth, ip.getWidth(), ip.getHeight());
                    stack.setProcessor(result, slice);
                } finally {
                    srcMat.release();
                    dstMat.release();
                }
            }

            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0);
        return denoised;
    }

    // ---------- 彩色转换 ----------
    private Mat colorProcessorToMat(ColorProcessor cp) {
        BufferedImage bi = cp.getBufferedImage();
        int w = bi.getWidth(), h = bi.getHeight();
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(h, w, CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private ColorProcessor matToColorProcessor(Mat mat) {
        int w = mat.cols(), h = mat.rows();
        byte[] data = new byte[w * h * 3];
        mat.get(0, 0, data);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        bi.getRaster().setDataElements(0, 0, w, h, data);
        return new ColorProcessor(bi);
    }

    // ---------- 灰度转换（支持 8/16/32 位） ----------
    private Mat grayProcessorToMat(ImageProcessor ip, int bitDepth) {
        int w = ip.getWidth(), h = ip.getHeight();
        if (bitDepth == 8) {
            byte[] data = (byte[]) ip.getPixels();
            Mat mat = new Mat(h, w, CvType.CV_8UC1);
            mat.put(0, 0, data);
            return mat;
        } else if (bitDepth == 16) {
            short[] data = (short[]) ip.getPixels();
            Mat mat = new Mat(h, w, CvType.CV_16UC1);
            mat.put(0, 0, data);
            return mat;
        } else if (bitDepth == 32) {
            float[] data = (float[]) ip.getPixels();
            Mat mat = new Mat(h, w, CvType.CV_32FC1);
            mat.put(0, 0, data);
            return mat;
        } else {
            throw new IllegalArgumentException("不支持的位深度: " + bitDepth);
        }
    }

    private ImageProcessor matToGrayProcessor(Mat mat, int bitDepth, int width, int height) {
        if (bitDepth == 8) {
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            ByteProcessor bp = new ByteProcessor(width, height);
            bp.setPixels(data);
            return bp;
        } else if (bitDepth == 16) {
            short[] data = new short[width * height];
            mat.get(0, 0, data);
            ShortProcessor sp = new ShortProcessor(width, height);
            sp.setPixels(data);
            return sp;
        } else if (bitDepth == 32) {
            float[] data = new float[width * height];
            mat.get(0, 0, data);
            FloatProcessor fp = new FloatProcessor(width, height);
            fp.setPixels(data);
            return fp;
        } else {
            throw new IllegalArgumentException("不支持的位深度: " + bitDepth);
        }
    }
}
