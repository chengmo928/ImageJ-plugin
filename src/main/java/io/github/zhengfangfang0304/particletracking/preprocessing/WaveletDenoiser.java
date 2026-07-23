package io.github.zhengfangfang0304.particletracking.preprocessing;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;

/**
 * 基于 BoofCV 小波变换（Visu Shrink 阈值）的降噪器。
 * 实现 ImageDenoiser 接口，可无缝接入粒子追踪处理管道。
 * <p>
 * 参数说明：{@code parameter} 代表小波分解层数，必须是大于 0 的整数（自动四舍五入取整）。
 * </p>
 */
public final class WaveletDenoiser implements ImageDenoiser {

    @Override
    public String getName() {
        return "Wavelet Denoise (Visu Shrink)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        // ----- 参数校验 -----
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException(
                    "小波降噪参数（层数）必须是大于 0 的有限数字。"
            );
        }
        int numLevels = (int) Math.round(parameter);
        if (numLevels < 1) {
            numLevels = 1; // 确保至少 1 层
        }

        // ----- 复制图像并准备处理 -----
        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Wavelet Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        // ----- 逐层处理 -----
        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor processor = stack.getProcessor(slice);

            // 1. ImageProcessor → BufferedImage
            BufferedImage srcBi = processor.getBufferedImage();

            // 2. 判断图像类型并调用相应小波降噪
            BufferedImage dstBi;
            if (srcBi.getType() == BufferedImage.TYPE_BYTE_GRAY
                    || srcBi.getType() == BufferedImage.TYPE_USHORT_GRAY) {
                // 灰度图像
                dstBi = denoiseGrayVisu(srcBi, numLevels);
            } else {
                // 彩色图像（RGB）
                dstBi = denoiseColorVisu(srcBi, numLevels);
            }

            // 3. BufferedImage → ImageProcessor
            ImageProcessor resultProcessor = processor.createProcessor(
                    dstBi.getWidth(), dstBi.getHeight()
            );
            // 直接将 BufferedImage 的像素数据设置到 processor
            resultProcessor.setPixels(
                    ((java.awt.image.DataBufferByte) dstBi.getRaster().getDataBuffer()).getData()
            );
            // 若为 16 位灰度，需额外处理，但这里假设为 8 位
            stack.setProcessor(resultProcessor, slice);

            // 显示进度
            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0);
        return denoised;
    }


    //  私有小波降噪方法（移植自原工具类，仅调整可见性）
    /**
     * 灰度图像小波降噪（Visu Shrink）。
     */
    private BufferedImage denoiseGrayVisu(BufferedImage input, int numLevels) {
        GrayF32 gray = ConvertBufferedImage.convertFromSingle(input, null, GrayF32.class);
        WaveletDenoiseFilter<GrayF32> denoiser =
                FactoryImageDenoise.waveletVisu(GrayF32.class, numLevels, -1, 0);
        GrayF32 result = gray.createSameShape();
        denoiser.process(gray, result);
        return ConvertBufferedImage.convertTo(result, null, true);
    }

    /**
     * 彩色图像小波降噪（Visu Shrink），逐通道处理。
     */
    private BufferedImage denoiseColorVisu(BufferedImage input, int numLevels) {
        int w = input.getWidth();
        int h = input.getHeight();

        Planar<GrayF32> color = new Planar<>(GrayF32.class, w, h, 3);
        ConvertBufferedImage.convertFromPlanar(input, color, true, GrayF32.class);

        Planar<GrayF32> result = color.createSameShape();
        for (int band = 0; band < color.getNumBands(); band++) {
            GrayF32 channel = color.getBand(band);
            WaveletDenoiseFilter<GrayF32> denoiser =
                    FactoryImageDenoise.waveletVisu(GrayF32.class, numLevels, -1, 0);
            GrayF32 denoised = channel.createSameShape();
            denoiser.process(channel, denoised);
            result.setBand(band, denoised);
        }

        return ConvertBufferedImage.convertTo(result, null, true);
    }
}
