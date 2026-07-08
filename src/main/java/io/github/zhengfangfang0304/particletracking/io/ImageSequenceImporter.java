package io.github.zhengfangfang0304.particletracking.io;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.FolderOpener;
import ij.process.ImageProcessor;

/**
 * 图像序列导入器。
 *
 * 这个类只负责：
 * 1. 从文件夹读取图像序列；
 * 2. 转换为8位灰度；
 * 3. 根据需要执行反相；
 * 4. 返回整理后的 ImagePlus。
 *
 * 不负责：
 * 1. 弹出文件夹选择框；
 * 2. 显示图像；
 * 3. 写GUI日志；
 * 4. 清空检测或追踪结果。
 */
public final class ImageSequenceImporter {

    private ImageSequenceImporter() {
    }

    /**
     * 从文件夹导入图像序列。
     *
     * @param directoryPath 图像序列文件夹路径
     * @param invertImage   是否反相
     * @return 导入结果
     */
    public static ImportResult importFromDirectory(
            String directoryPath,
            boolean invertImage
    ) {
        if (directoryPath == null
                || directoryPath.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "图像序列文件夹路径不能为空。"
            );
        }

        ImagePlus sourceSequence =
                FolderOpener.open(directoryPath);

        if (sourceSequence == null
                || sourceSequence.getStackSize() == 0) {

            throw new IllegalArgumentException(
                    "没有从所选文件夹读取到图像。"
            );
        }

        ImageStack sourceStack =
                sourceSequence.getStack();

        int width =
                sourceSequence.getWidth();

        int height =
                sourceSequence.getHeight();

        int totalFrames =
                sourceStack.getSize();

        ImageStack preparedStack =
                new ImageStack(
                        width,
                        height
                );

        for (int frame = 1;
             frame <= totalFrames;
             frame++) {

            ImageProcessor sourceProcessor =
                    sourceStack.getProcessor(
                            frame
                    );

            /*
             * 转成8位灰度。
             *
             * false表示不要对每一帧单独进行强度拉伸，
             * 避免不同帧被分别归一化。
             */
            ImageProcessor preparedProcessor =
                    sourceProcessor.convertToByteProcessor(
                            false
                    );

            if (invertImage) {
                preparedProcessor.invert();
            }

            String sliceLabel =
                    sourceStack.getSliceLabel(
                            frame
                    );

            preparedStack.addSlice(
                    sliceLabel,
                    preparedProcessor
            );

            IJ.showProgress(
                    frame,
                    totalFrames
            );
        }

        ImagePlus preparedSequence =
                new ImagePlus(
                        "Imported Image Sequence - Prepared",
                        preparedStack
                );

        preparedSequence.setDimensions(
                1,
                1,
                totalFrames
        );

        preparedSequence.setOpenAsHyperStack(
                true
        );

        preparedSequence.setDisplayRange(
                0,
                255
        );

        return new ImportResult(
                preparedSequence,
                directoryPath,
                width,
                height,
                totalFrames,
                invertImage
        );
    }

    /**
     * 图像序列导入结果。
     *
     * @param image       整理后的图像序列
     * @param directory   原始文件夹路径
     * @param width       图像宽度
     * @param height      图像高度
     * @param frames      帧数
     * @param inverted    是否执行了反相
     */
    public record ImportResult(
            ImagePlus image,
            String directory,
            int width,
            int height,
            int frames,
            boolean inverted
    ) {
    }
}