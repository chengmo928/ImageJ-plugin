package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * 基于 OpenCV 双边滤波的降噪器。
 * 实现 ImageDenoiser 接口，可无缝接入粒子追踪处理管道。
 */
public final class BilateralDenoiser implements ImageDenoiser {

    static {
        OpenCVLoader.load();  // 复用之前的自动加载逻辑
    }

    @Override
    public String getName() {
        return "Bilateral Filter 双边滤波 (OpenCV)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null");
        }

        // 将 parameter 拆解为双边滤波的三个参数
        // 这里用约定：parameter 作为 sigmaColor，sigmaSpace = sigmaColor / 2，d 自动计算
        // 你也可以改成其他拆分方式
        double sigmaColor = parameter;
        double sigmaSpace = parameter / 2.0;
        int d = (int) Math.max(3, Math.ceil(sigmaColor * 2));

        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Bilateral Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor processor = stack.getProcessor(slice);

            // 将 ImageJ 的 ImageProcessor 转为 OpenCV Mat
            Mat srcMat = imageProcessorToMat(processor);
            Mat dstMat = new Mat();

            // 执行双边滤波
            Imgproc.bilateralFilter(srcMat, dstMat, d, sigmaColor, sigmaSpace);

            // 将 OpenCV Mat 转回 ImageJ ImageProcessor
            ImageProcessor resultProcessor = matToImageProcessor(dstMat, processor);
            stack.setProcessor(resultProcessor, slice);

            // 释放 OpenCV 资源
            srcMat.release();
            dstMat.release();
        }

        return denoised;
    }

    /**
     * ImageJ ImageProcessor → OpenCV Mat
     */
    private Mat imageProcessorToMat(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        BufferedImage bi = ip.getBufferedImage();

        // 将 BufferedImage 转为 byte 数组
        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

        // 根据图像类型创建 Mat
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        return mat;
    }

    /**
     * OpenCV Mat → ImageJ ImageProcessor
     */
    private ImageProcessor matToImageProcessor(Mat mat, ImageProcessor template) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        // 将 Mat 转为 BufferedImage
        BufferedImage bi;
        if (channels == 3) {
            // 彩色图像
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            bi.getRaster().setDataElements(0, 0, width, height, data);
        } else {
            // 灰度图像
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bi.getRaster().setDataElements(0, 0, width, height, data);
        }

        // 复制数据到 ImageProcessor
        ImageProcessor result = template.createProcessor(width, height);
        result.setPixels(bi.getRaster().getDataBuffer().getData());
        return result;
    }
}
