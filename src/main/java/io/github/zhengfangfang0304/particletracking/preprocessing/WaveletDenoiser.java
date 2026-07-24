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
import ij.process.ColorProcessor;

import java.awt.image.BufferedImage;
package io.github.zhengfangfang0304.particletracking.preprocessing;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.struct.image.GrayF32;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.*;

/**
 * 基于 BoofCV 小波变换（Visu Shrink）的降噪器。
 * 支持 8 位和 16 位灰度图像，以及 8 位彩色图像。
 * <p>
 * 参数说明：{@code parameter} 为分解层数（整数），推荐 3~5。
 * </p>
 */
public final class WaveletDenoiser implements ImageDenoiser {

    @Override
    public String getName() {
        return "Wavelet Denoise (Visu Shrink)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException("小波降噪参数（层数）必须是大于 0 的有限数字。");
        }
        int numLevels = (int) Math.round(parameter);
        if (numLevels < 1) numLevels = 1;

        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - Wavelet Denoised");
        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor ip = stack.getProcessor(slice);

            // 处理彩色图像（仅 8 位 RGB）
            if (ip instanceof ColorProcessor) {
                ColorProcessor cp = (ColorProcessor) ip;
                java.awt.image.BufferedImage bi = cp.getBufferedImage();
                java.awt.image.BufferedImage resultBi = denoiseColorVisu(bi, numLevels);
                ColorProcessor resultCp = new ColorProcessor(resultBi);
                stack.setProcessor(resultCp, slice);
                IJ.showProgress(slice, totalSlices);
                continue;
            }

            // 灰度图像：提取 float 数组
            int bitDepth = ip.getBitDepth();
            float[] src = extractFloatArray(ip);
            int width = ip.getWidth(), height = ip.getHeight();

            // 创建 GrayF32
            GrayF32 gray = new GrayF32(width, height);
            gray.data = src;

            // 执行小波降噪
            WaveletDenoiseFilter<GrayF32> denoiser =
                    FactoryImageDenoise.waveletVisu(GrayF32.class, numLevels, -1, 0);
            GrayF32 result = gray.createSameShape();
            denoiser.process(gray, result);

            // 写回 ImageProcessor
            float[] resultData = result.data;
            ImageProcessor resultIp = createProcessorFromFloatArray(resultData, width, height, bitDepth);
            stack.setProcessor(resultIp, slice);
            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0);
        return denoised;
    }

    // 提取 float 数组（通用）
    private float[] extractFloatArray(ImageProcessor ip) {
        if (ip instanceof ByteProcessor) {
            byte[] bytes = (byte[]) ip.getPixels();
            float[] result = new float[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = bytes[i] & 0xFF;
            }
            return result;
        } else if (ip instanceof ShortProcessor) {
            short[] shorts = (short[]) ip.getPixels();
            float[] result = new float[shorts.length];
            for (int i = 0; i < shorts.length; i++) {
                result[i] = shorts[i] & 0xFFFF;
            }
            return result;
        } else if (ip instanceof FloatProcessor) {
            return (float[]) ip.getPixels();
        } else {
            throw new IllegalArgumentException("不支持的图像类型: " + ip.getClass().getName());
        }
    }

    // 从 float[] 创建对应位深度的 ImageProcessor
    private ImageProcessor createProcessorFromFloatArray(float[] data, int width, int height, int bitDepth) {
        if (bitDepth == 8) {
            byte[] bytes = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                int val = Math.round(data[i]);
                if (val < 0) val = 0;
                if (val > 255) val = 255;
                bytes[i] = (byte) val;
            }
            ByteProcessor bp = new ByteProcessor(width, height);
            bp.setPixels(bytes);
            return bp;
        } else if (bitDepth == 16) {
            short[] shorts = new short[data.length];
            for (int i = 0; i < data.length; i++) {
                int val = Math.round(data[i]);
                if (val < 0) val = 0;
                if (val > 65535) val = 65535;
                shorts[i] = (short) val;
            }
            ShortProcessor sp = new ShortProcessor(width, height);
            sp.setPixels(shorts);
            return sp;
        } else if (bitDepth == 32) {
            FloatProcessor fp = new FloatProcessor(width, height);
            fp.setPixels(data);
            return fp;
        } else {
            throw new IllegalArgumentException("不支持的位深度: " + bitDepth);
        }
    }

    // 彩色图像降噪（仅 8 位 RGB）
    private java.awt.image.BufferedImage denoiseColorVisu(java.awt.image.BufferedImage input, int numLevels) {
        int w = input.getWidth();
        int h = input.getHeight();
        boofcv.struct.image.Planar<GrayF32> color = new boofcv.struct.image.Planar<>(GrayF32.class, w, h, 3);
        boofcv.io.image.ConvertBufferedImage.convertFromPlanar(input, color, true, GrayF32.class);
        boofcv.struct.image.Planar<GrayF32> result = color.createSameShape();
        for (int band = 0; band < color.getNumBands(); band++) {
            GrayF32 channel = color.getBand(band);
            WaveletDenoiseFilter<GrayF32> denoiser =
                    FactoryImageDenoise.waveletVisu(GrayF32.class, numLevels, -1, 0);
            GrayF32 denoised = channel.createSameShape();
            denoiser.process(channel, denoised);
            result.setBand(band, denoised);
        }
        return boofcv.io.image.ConvertBufferedImage.convertTo(result, null, true);
    }
}
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
            ImageProcessor resultProcessor;
            if (processor instanceof ColorProcessor) {
                resultProcessor = new ColorProcessor(dstBi);
            } else {
                resultProcessor = processor.createProcessor(dstBi.getWidth(), dstBi.getHeight());
                resultProcessor.setPixels(
                    ((java.awt.image.DataBufferByte) dstBi.getRaster().getDataBuffer()).getData()
                );
            }
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
