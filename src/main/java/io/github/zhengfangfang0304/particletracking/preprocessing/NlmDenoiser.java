package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.*;

/**
 * 非局部均值 (NLM) 降噪器 
 * 支持 8 位和 16 位灰度图像（彩色自动转灰度）。
 * <p>
 * 参数说明：{@code parameter} 作为滤波强度 h 的基准值（针对 8 位图）。
 * 对于 16 位图，会根据图像实际最大值自动缩放 h。
 * 搜索半径固定为 15，块半径固定为 3。
 * </p>
 */
public final class NlmDenoiser implements ImageDenoiser {

    @Override
    public String getName() {
        return "Non-Local Means (NLM)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException("NLM 参数 (h) 必须是大于 0 的有限数字。");
        }

        // 获取第一帧的位深度和最大值（用于参数缩放）
        ImageProcessor firstIp = image.getProcessor();
        int bitDepth = firstIp.getBitDepth();
        double maxVal = firstIp.getMax();
        double scale = 1.0;
        if (bitDepth == 16 && maxVal > 0) {
            scale = maxVal / 255.0;
        } else if (bitDepth == 32) {
            scale = maxVal / 255.0;
        }
        // 对参数进行缩放
        float h = (float) (parameter * scale);
        IJ.log("NLM: 位深度=" + bitDepth + ", maxVal=" + maxVal + ", scale=" + scale + ", 实际h=" + h);

        int searchRadius = 15;
        int blockRadius = 3;

        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - NLM Denoised");
        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor ip = stack.getProcessor(slice);

            // 处理彩色（转为灰度）
            if (ip instanceof ColorProcessor) {
                ip = ((ColorProcessor) ip).convertToByteProcessor();
            }

            // 提取浮点数组
            float[] src = extractFloatArray(ip);
            float[] dst = new float[src.length];
            int width = ip.getWidth(), height = ip.getHeight();

            // 执行 NLM
            denoiseFloat(src, dst, width, height, h, searchRadius, blockRadius);

            // 写回 ImageProcessor（保留原始位深度）
            ImageProcessor result = createProcessorFromFloatArray(dst, width, height, bitDepth);
            stack.setProcessor(result, slice);
            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0);
        return denoised;
    }

    // 提取 float[]（兼容 8/16/32 位）
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

    // 通用 NLM 浮点实现
    private void denoiseFloat(float[] src, float[] dst, int width, int height,
                              float h, int searchRadius, int blockRadius) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                float sumWeights = 0f;
                float sumPixels = 0f;

                int yStart = Math.max(0, y - searchRadius);
                int yEnd = Math.min(height - 1, y + searchRadius);
                int xStart = Math.max(0, x - searchRadius);
                int xEnd = Math.min(width - 1, x + searchRadius);

                for (int ny = yStart; ny <= yEnd; ny++) {
                    for (int nx = xStart; nx <= xEnd; nx++) {
                        int nIdx = ny * width + nx;
                        float blockDist = computeBlockDistance(src, width, height, x, y, nx, ny, blockRadius);
                        float weight = (float) Math.exp(-blockDist / (h * h));
                        sumWeights += weight;
                        sumPixels += weight * src[nIdx];
                    }
                }
                dst[idx] = sumPixels / sumWeights;
            }
        }
    }

    private float computeBlockDistance(float[] img, int w, int h,
                                       int x1, int y1, int x2, int y2, int blockRadius) {
        float sum = 0f;
        int count = 0;
        for (int dy = -blockRadius; dy <= blockRadius; dy++) {
            for (int dx = -blockRadius; dx <= blockRadius; dx++) {
                int px1 = x1 + dx, py1 = y1 + dy;
                int px2 = x2 + dx, py2 = y2 + dy;
                if (px1 >= 0 && px1 < w && py1 >= 0 && py1 < h &&
                    px2 >= 0 && px2 < w && py2 >= 0 && py2 < h) {
                    float v1 = img[py1 * w + px1];
                    float v2 = img[py2 * w + px2];
                    float diff = v1 - v2;
                    sum += diff * diff;
                    count++;
                }
            }
        }
        return count > 0 ? sum / count : 0f;
    }
}
