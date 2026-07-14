package io.github.zhengfangfang0304.particletracking.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A complete simulation scenario.
 *
 * It combines:
 * 1. global simulation parameters;
 * 2. motion timeline segments;
 * 3. visibility events such as blinking and photobleaching.
 */
public class SimulationScenario {

    private final SimulationConfig baseConfig;

    private final List<MotionSegment> motionSegments =
            new ArrayList<>();

    private final List<VisibilityEvent> visibilityEvents =
            new ArrayList<>();

    public SimulationScenario(
            SimulationConfig baseConfig
    ) {
        if (baseConfig == null) {
            throw new IllegalArgumentException("baseConfig cannot be null.");
        }

        this.baseConfig = baseConfig;
    }

    /**
     * Create a default scenario from a SimulationConfig.
     *
     * The default scenario has one motion segment:
     * frame 1 to frame N use config.motionMode.
     */
    public static SimulationScenario fromConfig(
            SimulationConfig config
    ) {
        SimulationScenario scenario =
                new SimulationScenario(config);

        scenario.addMotionSegment(
                new MotionSegment(
                        1,
                        config.frames,
                        config.motionMode,
                        config.diffusionCoefficientUm2PerSecond,
                        0.0,
                        0.0,
                        config.confinementRadius,
                        "default_motion"
                )
        );

        return scenario;
    }

    public SimulationConfig getBaseConfig() {
        return baseConfig;
    }

    public void addMotionSegment(
            MotionSegment segment
    ) {
        if (segment == null) {
            throw new IllegalArgumentException("MotionSegment cannot be null.");
        }

        motionSegments.add(segment);
    }

    public void addVisibilityEvent(
            VisibilityEvent event
    ) {
        if (event == null) {
            throw new IllegalArgumentException("VisibilityEvent cannot be null.");
        }

        visibilityEvents.add(event);
    }

    public List<MotionSegment> getMotionSegments() {
        return Collections.unmodifiableList(motionSegments);
    }

    public List<VisibilityEvent> getVisibilityEvents() {
        return Collections.unmodifiableList(visibilityEvents);
    }

    /**
     * Find the motion segment used at a specific frame.
     *
     * @param frame 1-based frame index
     * @return the matching motion segment
     */
    public MotionSegment getMotionSegmentForFrame(
            int frame
    ) {
        for (MotionSegment segment : motionSegments) {
            if (segment.containsFrame(frame)) {
                return segment;
            }
        }

        throw new IllegalArgumentException(
                "No motion segment found for frame: " + frame
        );
    }

    /**
     * Find all visibility events active at a specific frame.
     *
     * @param frame 1-based frame index
     * @return active visibility events
     */
    public List<VisibilityEvent> getVisibilityEventsForFrame(
            int frame
    ) {
        List<VisibilityEvent> activeEvents =
                new ArrayList<>();

        for (VisibilityEvent event : visibilityEvents) {
            if (event.containsFrame(frame)) {
                activeEvents.add(event);
            }
        }

        return activeEvents;
    }

    /**
     * Validate the motion timeline.
     *
     * First version:
     * 1. each segment must stay inside the total frame range;
     * 2. every frame must be covered by at least one segment;
     * 3. overlapping segments are not allowed.
     */
    public void validate() {
        if (motionSegments.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one motion segment is required."
            );
        }

        boolean[] covered =
                new boolean[baseConfig.frames + 1];

        for (MotionSegment segment : motionSegments) {
            if (segment.getStartFrame() < 1) {
                throw new IllegalArgumentException(
                        "Motion segment start frame must be >= 1."
                );
            }

            if (segment.getEndFrame() > baseConfig.frames) {
                throw new IllegalArgumentException(
                        "Motion segment end frame exceeds total frame count."
                );
            }

            for (
                    int frame = segment.getStartFrame();
                    frame <= segment.getEndFrame();
                    frame++
            ) {
                if (covered[frame]) {
                    throw new IllegalArgumentException(
                            "Overlapping motion segments at frame: " + frame
                    );
                }

                covered[frame] = true;
            }
        }

        for (int frame = 1; frame <= baseConfig.frames; frame++) {
            if (!covered[frame]) {
                throw new IllegalArgumentException(
                        "No motion segment covers frame: " + frame
                );
            }
        }

        for (VisibilityEvent event : visibilityEvents) {
            if (event.getStartFrame() < 1) {
                throw new IllegalArgumentException(
                        "Visibility event start frame must be >= 1."
                );
            }

            if (event.getEndFrame() > baseConfig.frames) {
                throw new IllegalArgumentException(
                        "Visibility event end frame exceeds total frame count."
                );
            }
        }
    }
}