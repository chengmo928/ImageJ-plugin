package io.github.zhengfangfang0304.particletracking.util;

/**
 * 与坐标和距离计算有关的通用工具方法。
 */
public final class DistanceUtils {

    /**
     * 工具类不需要创建对象，因此禁止外部实例化。
     */
    private DistanceUtils() {
    }

    /**
     * 计算二维平面中两个点之间的欧氏距离。
     *
     * @param x1 第一个点的X坐标
     * @param y1 第一个点的Y坐标
     * @param x2 第二个点的X坐标
     * @param y2 第二个点的Y坐标
     * @return 两点之间的欧氏距离
     */
    public static double euclidean(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt(
                dx * dx + dy * dy
        );
    }
}
