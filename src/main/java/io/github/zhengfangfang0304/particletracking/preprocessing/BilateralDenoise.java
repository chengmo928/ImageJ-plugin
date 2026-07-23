package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * 基于 OpenCV 双边滤波的降噪器。
 * 实现 ImageDenoiser 接口，可无缝接入粒子追踪处理管道。
 */
public final class BilateralDenoiser implements ImageDenoiser {

    static {
        // 使用 OpenCV 官方加载器，自动提取并加载本地库
        OpenCV.loadLocally();
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

        // parameter 作为 sigmaColor，sigmaSpace 自动取一半，d 自适应
        double sigmaColor = parameter;
        double sigmaSpace = parameter / 2.0;
        int d = (int) Math.max(3, Math.ceil(sigmaColor * 2));

        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Bilateral Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor processor = stack.getProcessor(slice);

            // ImageProcessor → OpenCV Mat
            Mat srcMat = imageProcessorToMat(processor);
            Mat dstMat = new Mat();

            // 双边滤波
            Imgproc.bilateralFilter(srcMat, dstMat, d, sigmaColor, sigmaSpace);

            // OpenCV Mat → ImageProcessor
            ImageProcessor resultProcessor = matToImageProcessor(dstMat, processor);
            stack.setProcessor(resultProcessor, slice);

            srcMat.release();
            dstMat.release();
        }

        return denoised;
    }

    // ========== 转换工具方法 ==========

    private Mat imageProcessorToMat(ImageProcessor ip) {
        BufferedImage bi = ip.getBufferedImage();
        int width = bi.getWidth();
        int height = bi.getHeight();

        // 根据图像类型创建 Mat
        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(height, width, CvType.CV_8UC1);
            mat.put(0, 0, data);
            return mat;
        } else {
            // 彩色图像 (假设 TYPE_3BYTE_BGR)
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(height, width, CvType.CV_8UC3);
            mat.put(0, 0, data);
            return mat;
        }
    }

    private ImageProcessor matToImageProcessor(Mat mat, ImageProcessor template) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        BufferedImage bi;
        if (channels == 3) {
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            bi.getRaster().setDataElements(0, 0, width, height, data);
        } else {
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bi.getRaster().setDataElements(0, 0, width, height, data);
        }

        ImageProcessor result = template.createProcessor(width, height);
        result.setPixels(bi.getRaster().getDataBuffer().getData());
        return result;
    }
}
