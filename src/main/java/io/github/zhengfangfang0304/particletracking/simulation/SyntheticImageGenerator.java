package io.github.zhengfangfang0304.particletracking.simulation;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import java.util.Random;

public final class SyntheticImageGenerator {

    public static final int DEFAULT_WIDTH = 256;
    public static final int DEFAULT_HEIGHT = 256;
    public static final int DEFAULT_FRAMES = 30;
    public static final int DEFAULT_PARTICLES = 8;

    private SyntheticImageGenerator() {
    }

    public static ImagePlus createDefaultMovie() {
        return createMovie(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_FRAMES,
                DEFAULT_PARTICLES,
                2.0,
                180.0,
                20.0,
                8.0,
                12345L
        );
    }

    public static ImagePlus createMovie(
            int width,
            int height,
            int frames,
            int particles,
            double sigma,
            double amplitude,
            double background,
            double noiseLevel,
            long randomSeed
    ) {
        Random random = new Random(randomSeed);

        double[] x = new double[particles];
        double[] y = new double[particles];
        double[] vx = new double[particles];
        double[] vy = new double[particles];

        for (int i = 0; i < particles; i++) {
            x[i] = 30 + random.nextDouble() * (width - 60);
            y[i] = 30 + random.nextDouble() * (height - 60);

            vx[i] = -1.5 + random.nextDouble() * 3.0;
            vy[i] = -1.5 + random.nextDouble() * 3.0;
        }

        ImageStack stack = new ImageStack(width, height);

        for (int t = 0; t < frames; t++) {
            float[] pixels = new float[width * height];

            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = (float) (
                        background + random.nextGaussian() * noiseLevel
                );
            }

            for (int p = 0; p < particles; p++) {
                drawGaussianSpot(
                        pixels,
                        width,
                        height,
                        x[p],
                        y[p],
                        amplitude,
                        sigma
                );

                x[p] += vx[p];
                y[p] += vy[p];

                if (x[p] < 10 || x[p] > width - 10) {
                    vx[p] = -vx[p];
                }

                if (y[p] < 10 || y[p] > height - 10) {
                    vy[p] = -vy[p];
                }
            }

            FloatProcessor fp = new FloatProcessor(width, height, pixels);
            stack.addSlice("Frame " + (t + 1), fp);
        }

        ImagePlus image = new ImagePlus(
                "Synthetic Single Particle Movie",
                stack
        );
        image.setDimensions(1, 1, frames);
        image.setOpenAsHyperStack(true);
        image.setDisplayRange(0, 255);

        return image;
    }

    private static void drawGaussianSpot(
            float[] pixels,
            int width,
            int height,
            double centerX,
            double centerY,
            double amplitude,
            double sigma
    ) {
        int radius = (int) Math.ceil(3 * sigma);

        int xMin = Math.max(0, (int) Math.floor(centerX - radius));
        int xMax = Math.min(width - 1, (int) Math.ceil(centerX + radius));
        int yMin = Math.max(0, (int) Math.floor(centerY - radius));
        int yMax = Math.min(height - 1, (int) Math.ceil(centerY + radius));

        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                double dx = x - centerX;
                double dy = y - centerY;

                double value = amplitude * Math.exp(
                        -(dx * dx + dy * dy) / (2 * sigma * sigma)
                );

                int index = y * width + x;
                pixels[index] += (float) value;
            }
        }
    }
}
