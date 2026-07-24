package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * 非局部均值 (Non-Local Means) 降噪器 - 手动实现（纯 Java）
 * 实现 ImageDenoiser 接口，可无缝接入粒子追踪处理管道。
 * <p>
 * 参数说明：{@code parameter} 作为滤波强度 h，推荐范围 10~30。
 * 搜索半径固定为 15，块半径固定为 3，可取得较好平衡。
 * 仅支持灰度图像（彩色图像会自动转换为灰度处理）。
 * </p>
 */
public final class NlmDenoiser implements ImageDenoiser {

    @Override
    public String getName() {
        return "Non-Local Means (NLM)";
    }

    @Override
    public ImagePlus denoise(ImagePlus image, double parameter) {
        // ----- 参数校验 -----
        if (image == null) {
            throw new IllegalArgumentException("输入图像不能为 null。");
        }
        if (!Double.isFinite(parameter) || parameter <= 0.0) {
            throw new IllegalArgumentException("NLM 参数 (h) 必须是大于 0 的有限数字。");
        }
        float h = (float) parameter;
        int searchRadius = 15; // 固定，可提供较好降噪效果
        int blockRadius = 3;

        // ----- 复制图像并准备处理 -----
        ImagePlus denoised = image.duplicate();
        denoised.setTitle(image.getTitle() + " - NLM Denoised");

        ImageStack stack = denoised.getStack();
        int totalSlices = stack.getSize();

        // ----- 逐层处理 -----
        for (int slice = 1; slice <= totalSlices; slice++) {
            ImageProcessor processor = stack.getProcessor(slice);

            // 转为 BufferedImage（若为彩色则转灰度）
            BufferedImage srcBi = processor.getBufferedImage();
            if (srcBi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
                srcBi = convertToGray(srcBi);
            }

            // 执行 NLM 降噪
            BufferedImage dstBi = denoiseGray(srcBi, h, searchRadius, blockRadius);

            // 将结果写回 ImageProcessor
            ImageProcessor resultProcessor = processor.createProcessor(dstBi.getWidth(), dstBi.getHeight());
            resultProcessor.setPixels(((DataBufferByte) dstBi.getRaster().getDataBuffer()).getData());
            stack.setProcessor(resultProcessor, slice);

            IJ.showProgress(slice, totalSlices);
        }

        IJ.showProgress(1.0);
        return denoised;
    }

    // ============================================================
    //  私有 NLM 算法实现（移植自原 GrayNLMDemo）
    // ============================================================

    /**
     * 对灰度 BufferedImage 执行 NLM 降噪。
     */
    private BufferedImage denoiseGray(BufferedImage input, float h, int searchRadius, int blockRadius) {
        int width = input.getWidth();
        int height = input.getHeight();
        float[] src = bufferedImageToFloatArray(input);
        float[] dst = new float[src.length];

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
        return floatArrayToBufferedImage(dst, width, height);
    }

    /**
     * 计算两个图像块之间的欧氏距离（归一化平方和）。
     */
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

    // ============================================================
    //  辅助转换方法
    // ============================================================

    /**
     * 将灰度 BufferedImage 转换为 float 数组 (0~255)。
     */
    private float[] bufferedImageToFloatArray(BufferedImage bi) {
        int w = bi.getWidth(), h = bi.getHeight();
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        float[] result = new float[w * h];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] & 0xFF;
        }
        return result;
    }

    /**
     * 将 float 数组转换为灰度 BufferedImage（值限幅至 0~255）。
     */
    private BufferedImage floatArrayToBufferedImage(float[] data, int w, int h) {
        byte[] pixels = new byte[w * h];
        for (int i = 0; i < data.length; i++) {
            int val = Math.round(data[i]);
            if (val < 0) val = 0;
            if (val > 255) val = 255;
            pixels[i] = (byte) val;
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        bi.getRaster().setDataElements(0, 0, w, h, pixels);
        return bi;
    }

    /**
     * 将彩色 BufferedImage 转为灰度。
     */
    private BufferedImage convertToGray(BufferedImage color) {
        BufferedImage gray = new BufferedImage(color.getWidth(), color.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(color, 0, 0, null);
        return gray;
    }
}
