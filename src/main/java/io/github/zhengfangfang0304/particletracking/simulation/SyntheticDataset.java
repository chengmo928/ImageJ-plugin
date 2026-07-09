package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一组完整模拟数据的 ground truth。
 */
public class SyntheticDataset {

    private final List<SyntheticParticle> particles;

    public SyntheticDataset(List<SyntheticParticle> particles) {
        this.particles = new ArrayList<>(particles);
    }

    public List<SyntheticParticle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    public List<SyntheticParticle> getParticlesInFrame(int frame) {
        List<SyntheticParticle> result = new ArrayList<>();

        for (SyntheticParticle particle : particles) {
            if (particle.getFrame() == frame) {
                result.add(particle);
            }
        }

        return result;
    }

    public int size() {
        return particles.size();
    }
}
