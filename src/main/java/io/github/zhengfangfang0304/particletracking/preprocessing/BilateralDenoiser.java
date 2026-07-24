package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * 基于 OpenCV 双边滤波的降噪器。
 * 实现 ImageDenoiser 接口，可无缝接入粒子追踪处理管道。
 * <p>
 * 参数说明：{@code parameter} 作为 sigmaColor，sigmaSpace 自动取 parameter/2，
 * 滤波窗口直径 d 自适应为 max(3, ceil(sigmaColor * 2))。
 * </p>
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
        // ----- 参数校验 -----
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException(
                    "双边滤波参数 (sigmaColor) 必须是大于 0 的有限数字。"
            );
        }

        // ----- 参数映射 -----
        double sigmaColor = parameter;
        double sigmaSpace = parameter / 2.0;
        int d = (int) Math.max(3, Math.ceil(sigmaColor * 2));

        // ----- 复制图像并准备处理 -----
        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Bilateral Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        // ----- 逐层处理 -----
        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor processor = stack.getProcessor(slice);

            // 转换 ImageProcessor -> OpenCV Mat
            Mat srcMat = imageProcessorToMat(processor);
            Mat dstMat = new Mat();

            try {
                // 双边滤波
                Imgproc.bilateralFilter(srcMat, dstMat, d, sigmaColor, sigmaSpace);

                // 转换 OpenCV Mat -> ImageProcessor
                ImageProcessor resultProcessor = matToImageProcessor(dstMat, processor);
                stack.setProcessor(resultProcessor, slice);

            } finally {
                // 确保释放本地内存
                srcMat.release();
                dstMat.release();
            }

            // 显示进度（与 MedianDenoiser 一致）
            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0); // 完成进度

        return denoised;
    }


    //  私有转换工具方法（可考虑提取到公共工具类中复用）
    /**
     * 将 ImageJ 的 ImageProcessor 转换为 OpenCV Mat。
     * 支持 8 位灰度图像和 8 位 RGB 彩色图像。
     *
     * @param ip ImageProcessor 实例
     * @return 对应的 Mat 对象（需调用者负责 release）
     */
    private Mat imageProcessorToMat(ImageProcessor ip) {
        BufferedImage bi = ip.getBufferedImage();
        int width = bi.getWidth();
        int height = bi.getHeight();

        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(height, width, CvType.CV_8UC1);
            mat.put(0, 0, data);
            return mat;
        } else {
            // 若为彩色，按 3 通道 BGR 处理（默认 ImageJ 彩色为 TYPE_3BYTE_BGR）
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(height, width, CvType.CV_8UC3);
            mat.put(0, 0, data);
            return mat;
        }
    }

    /**
     * 将 OpenCV Mat 转换回 ImageJ 的 ImageProcessor。
     * 根据 Mat 通道数自动选择灰度或彩色，并使用传入的模板创建同类型处理器。
     *
     * @param mat      源 Mat（不得为 null）
     * @param template 用于创建新处理器的模板（提供尺寸和类型）
     * @return 转换后的 ImageProcessor
     */
    private ImageProcessor matToImageProcessor(Mat mat, ImageProcessor template) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        BufferedImage bi;
        if (channels == 3) {
            // 彩色图像：直接创建 ColorProcessor
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            bi.getRaster().setDataElements(0, 0, width, height, data);
            return new ColorProcessor(bi);
        } else {
            // 灰度图像
            byte[] data = new byte[width * height];
            mat.get(0, 0, data);
            bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bi.getRaster().setDataElements(0, 0, width, height, data);
            ImageProcessor result = template.createProcessor(width, height);
            result.setPixels(((DataBufferByte) bi.getRaster().getDataBuffer()).getData());
            return result;
        }
    }
}
