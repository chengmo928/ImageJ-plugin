package io.github.zhengfangfang0304.particletracking.preprocessing;

import ij.ImagePlus;

/**
 * 图像降噪算法统一接口。
 *
 * 所有降噪算法都应该实现这个接口，
 * 这样GUI和Controller可以用统一方式调用不同降噪方法。
 */
public interface ImageDenoiser {

    /**
     * 返回降噪算法名称。
     *
     * @return 算法名称
     */
    String getName();

    /**
     * 对输入图像执行降噪。
     *
     * @param image     输入图像
     * @param parameter 降噪参数
     * @return 降噪后的新图像
     */
    ImagePlus denoise(
            ImagePlus image,
            double parameter
    );
}