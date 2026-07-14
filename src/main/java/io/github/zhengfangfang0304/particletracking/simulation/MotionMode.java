package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * Motion modes used in synthetic particle simulations.
 */
public enum MotionMode {

    CONSTANT_VELOCITY("Simple Constant Velocity"),

    FREE_BROWNIAN("Free Brownian Motion"),

    CONFINED_BROWNIAN("Confined Brownian Motion"),

    DIRECTED_MOTION("Directed Motion"),

    DIRECTED_BROWNIAN("Directed Brownian Motion"),

    IMMOBILE("Immobile / Trapped");

    private final String displayName;

    MotionMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
