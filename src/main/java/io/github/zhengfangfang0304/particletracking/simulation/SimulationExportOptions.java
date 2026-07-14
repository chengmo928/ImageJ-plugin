package io.github.zhengfangfang0304.particletracking.simulation;

/**
 * Options controlling which synthetic dataset files should be exported.
 */
public class SimulationExportOptions {

    public boolean exportMovie = true;

    public boolean exportGroundTruthDetections = true;

    public boolean exportGroundTruthTracks = true;

    public boolean exportGroundTruthVisibility = true;

    public boolean exportGroundTruthVisibilityEvents = true;

    public boolean exportGroundTruthMotionSegments = true;

    public boolean exportSimulationConfig = true;

    public boolean exportScenarioConfig = true;

    public boolean exportTheoreticalMsd = true;

    public boolean exportGroundTruthMsd = true;

    public static SimulationExportOptions exportAll() {
        return new SimulationExportOptions();
    }
}
