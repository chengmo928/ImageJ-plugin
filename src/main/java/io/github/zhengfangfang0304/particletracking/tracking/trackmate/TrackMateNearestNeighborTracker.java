package io.github.zhengfangfang0304.particletracking.tracking.trackmate;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kdtree.NearestNeighborTrackerFactory;

import ij.ImagePlus;

import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.tracking.ParticleTracker;
import io.github.zhengfangfang0304.particletracking.tracking.TrackingParameters;

import java.util.List;
import java.util.Map;

/**
 * 调用 TrackMate 的最近邻追踪器。
 */
public final class TrackMateNearestNeighborTracker
        implements ParticleTracker {

    /**
     * 返回追踪器名称。
     */
    @Override
    public String getName() {
        return "TrackMate Nearest Neighbor";
    }

    /**
     * 使用 TrackMate 最近邻方法连接检测点。
     */
    @Override
    public List<Track> track(
            List<Detection> detections,
            ImagePlus image,
            TrackingParameters parameters
    ) {
        /*
         * 检查输入数据。
         */
        if (detections == null || detections.isEmpty()) {
            throw new IllegalArgumentException(
                    "检测点列表不能为空。"
            );
        }

        if (image == null) {
            throw new IllegalArgumentException(
                    "输入图像不能为 null。"
            );
        }

        if (parameters == null) {
            throw new IllegalArgumentException(
                    "追踪参数不能为 null。"
            );
        }

        double maxLinkingDistance =
                parameters.maximumLinkingDistance();

        /*
         * Double.isFinite()可以排除：
         * NaN、正无穷和负无穷。
         */
        if (!Double.isFinite(maxLinkingDistance)
                || maxLinkingDistance <= 0.0) {

            throw new IllegalArgumentException(
                    "最大连接距离必须是大于0的有限数字。"
            );
        }

        /*
         * 优先使用时间维度的帧数。
         *
         * 对于没有设置为HyperStack的普通ImageJ stack，
         * 当前插件仍把每个slice视为一帧。
         */
        int totalFrames = image.getNFrames();

        if (totalFrames <= 1
                && image.getStackSize() > 1) {

            totalFrames = image.getStackSize();
        }

        /*
         * 最近邻算法不使用Spot半径参与连接计算。
         * 但TrackMate Spot构造时必须提供半径，
         * 因此暂时设为1 pixel。
         */
        double spotRadius = 1.0;

        /*
         * 将本程序的Detection转换为TrackMate SpotCollection。
         */
        TrackMateSpotConverter.ConversionResult input =
                TrackMateSpotConverter.convert(
                        detections,
                        totalFrames,
                        spotRadius
                );

        /*
         * 创建TrackMate数据模型。
         */
        Model model = new Model();

        /*
         * 当前Detection的x、y使用像素坐标，
         * 最大连接距离也由GUI以pixel为单位输入。
         */
        model.setPhysicalUnits(
                "pixel",
                "frame"
        );

        /*
         * 把已经检测完成的Spot放入Model。
         * false表示此时不发送Model变化通知。
         */
        model.setSpots(
                input.spots(),
                false
        );

        /*
         * 根据当前图像创建TrackMate设置。
         */
        Settings settings =
                new Settings(image);

        /*
         * 创建TrackMate最近邻追踪器工厂。
         */
        NearestNeighborTrackerFactory factory =
                new NearestNeighborTrackerFactory();

        /*
         * 先取得TrackMate 8.1.6提供的默认参数。
         */
        Map<String, Object> trackerSettings =
                factory.getDefaultSettings();

        /*
         * 覆盖最大连接距离。
         *
         * TrackMate要求这个参数是Double类型。
         */
        trackerSettings.put(
                TrackerKeys.KEY_LINKING_MAX_DISTANCE,
                Double.valueOf(maxLinkingDistance)
        );

        /*
         * TrackMate 8.1.6的新参数检查接口。
         *
         * 参数正确时返回null；
         * 参数错误时返回错误说明。
         */
        String settingsError =
                factory.checkSettings(
                        trackerSettings
                );

        if (settingsError != null) {
            throw new IllegalArgumentException(
                    "TrackMate最近邻参数无效："
                            + settingsError
            );
        }

        /*
         * 将追踪器和参数交给TrackMate。
         */
        settings.trackerFactory = factory;
        settings.trackerSettings = trackerSettings;

        TrackMate trackMate =
                new TrackMate(
                        model,
                        settings
                );

        /*
         * 这里只执行追踪，不重新执行检测。
         */
        boolean success =
                trackMate.execTracking();

        if (!success) {
            throw new IllegalStateException(
                    "TrackMate最近邻追踪失败："
                            + trackMate.getErrorMessage()
            );
        }

        /*
         * 将TrackMate Model中的轨迹转换回本程序的Track。
         */
        return TrackMateResultConverter.convert(
                model,
                input.spotToDetection()
        );
    }
}