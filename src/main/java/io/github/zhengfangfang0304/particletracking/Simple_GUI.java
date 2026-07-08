package io.github.zhengfangfang0304.particletracking;

import io.github.zhengfangfang0304.particletracking.controller.ParticleTrackingController;
import io.github.zhengfangfang0304.particletracking.gui.ParticleTrackingFrame;

import ij.plugin.PlugIn;

import javax.swing.SwingUtilities;

/**
 * Fiji/ImageJ 插件入口。
 *
 * 这个类只负责启动主界面。
 *
 * 真正的 GUI、按钮事件和功能逻辑
 * 已经迁移到 ParticleTrackingFrame 中。
 */
public class Simple_GUI implements PlugIn {

    @Override
    public void run(String arg) {
        SwingUtilities.invokeLater(
                () -> {
                    ParticleTrackingController controller =
                            new ParticleTrackingController();

                    ParticleTrackingFrame frame =
                            new ParticleTrackingFrame(
                                    controller
                            );

                    frame.setVisible(
                            true
                    );
                }
        );
    }
}