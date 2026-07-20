package io.github.zhengfangfang0304.particletracking;

import io.github.zhengfangfang0304.particletracking.controller.ParticleTrackingController;
import io.github.zhengfangfang0304.particletracking.gui.ParticleTrackingFrame;

import ij.plugin.PlugIn;

import javax.swing.SwingUtilities;

/**
 * SPTurbo 的 Fiji/ImageJ 插件入口。
 *
 * 这个类只负责从 Fiji 菜单启动主界面。
 * 真正的 GUI 和功能逻辑位于 ParticleTrackingFrame 和各功能模块中。
 */
public class SPTurbo_Plugin implements PlugIn {

    @Override
    public void run(String arg) {
        SwingUtilities.invokeLater(
                () -> {
                    ParticleTrackingController controller =
                            new ParticleTrackingController();

                    ParticleTrackingFrame.openFromPlugin(
                            controller
                    );
                }
        );
    }
}
