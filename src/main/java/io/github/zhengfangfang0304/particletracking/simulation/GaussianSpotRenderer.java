package io.github.zhengfangfang0304.particletracking.simulation;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import java.util.List;
import java.util.Random;

/**
 * 将模拟轨迹渲染成 ImageJ 图像序列。
 */
public class GaussianSpotRenderer {

    public ImagePlus render(SyntheticDataset dataset, SimulationConfig config) {
        ImageStack stack = new ImageStack(config.width, config.height);
        Random random = new Random(config.randomSeed);

        for (int frame = 1; frame <= config.frames; frame++) {
            float[] pixels = new float[config.width * config.height];

            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (float) (
                        config.background + random.nextGaussian() * config.noiseSigma
                );
            }

            List<SyntheticParticle> particles = dataset.getParticlesInFrame(frame);

            for (SyntheticParticle particle : particles) {
                if (!particle.isVisible()) {
                    continue;
                }
                //意思是ground truth 里仍然保存这个粒子，但图像里不渲染这个粒子
                drawGaussianSpot(
                        pixels,
                        config.width,
                        config.height,
                        particle.getX(),
                        particle.getY(),
                        particle.getIntensity(),
                        config.psfSigma
                );
            }

            FloatProcessor fp = new FloatProcessor(
                    config.width,
                    config.height,
                    pixels
            );

            stack.addSlice("Frame " + frame, fp);
        }

        ImagePlus image = new ImagePlus(config.title, stack);
        image.setDimensions(1, 1, config.frames);
        image.setOpenAsHyperStack(true);
        image.setDisplayRange(0, 255);

        return image;
    }

    private void drawGaussianSpot(
            float[] pixels,
            int width,
            int height,
            double centerX,
            double centerY,
            double amplitude,
            double sigma
    ) {
        int radius = (int) Math.ceil(3.0 * sigma);

        int xMin = Math.max(0, (int) Math.floor(centerX - radius));
        int xMax = Math.min(width - 1, (int) Math.ceil(centerX + radius));
        int yMin = Math.max(0, (int) Math.floor(centerY - radius));
        int yMax = Math.min(height - 1, (int) Math.ceil(centerY + radius));

        double twoSigmaSquared = 2.0 * sigma * sigma;

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                double dx = x - centerX;
                double dy = y - centerY;

                double value = amplitude * Math.exp(
                        -(dx * dx + dy * dy) / twoSigmaSquared
                );

                int index = y * width + x;
                pixels[index] += (float) value;
            }
        }
    }
}
