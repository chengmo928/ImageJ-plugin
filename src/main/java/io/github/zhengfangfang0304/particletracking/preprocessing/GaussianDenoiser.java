package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * 基于ImageJ Gaussian Blur的高斯降噪器。
 */
public final class GaussianDenoiser implements ImageDenoiser {

    @Override
    public String getName() {
        return "Gaussian Blur 高斯滤波";
    }

    @Override
    public ImagePlus denoise(
            ImagePlus image,
            double parameter
    ) {
        if (image == null) {
            throw new IllegalArgumentException(
                    "输入图像不能为null。"
            );
        }

        if (!Double.isFinite(parameter)
                || parameter <= 0.0) {

            throw new IllegalArgumentException(
                    "高斯滤波参数必须是大于0的有限数字。"
            );
        }

        ImagePlus denoised =
                image.duplicate();

        denoised.setTitle(
                image.getTitle() + " - Gaussian Denoised"
        );

        ImageStack stack =
                denoised.getStack();

        int totalSlices =
                stack.getSize();

        for (int slice = 1;
             slice <= totalSlices;
             slice++) {

            ImageProcessor processor =
                    stack.getProcessor(slice);

            processor.blurGaussian(parameter);

            IJ.showProgress(
                    slice,
                    totalSlices
            );
        }

        return denoised;
    }
}
