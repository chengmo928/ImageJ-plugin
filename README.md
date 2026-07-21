# ImageJ Particle Tracking Plugin

这是一个用于 ImageJ/Fiji 的单颗粒分子追踪 GUI 插件项目。

本项目当前是一个 **Java 21 的 ImageJ/Fiji 插件项目**，主要依赖 **ImageJ 1.x API** 和 **TrackMate 8.1.6 API**，最终会被打包成一个可放入 `Fiji.app/plugins/` 目录中的 `.jar` 文件。

## 项目目标

本项目旨在开发一个用于单颗粒分子追踪的 ImageJ/Fiji 插件，逐步实现从模拟数据生成、图像预处理、颗粒检测、粒子追踪到轨迹分析和结果导出的完整流程。

后续目标是形成一个可用于科研实验、算法验证和论文展示的单颗粒追踪分析程序。

## 当前功能

- 打开一个 ImageJ/Fiji 插件 GUI 窗口
- 读取当前图像名称、宽度、高度和帧数
- 生成单颗粒追踪模拟图像数据
- 导入图像序列
- 导入 CSV 轨迹数据
- 执行图像降噪
- 执行颗粒检测
- 执行自编简单追踪
- 调用 TrackMate 最近邻追踪方法
- 进行轨迹统计
- 计算 MSD
- 绘制 MSD
- 计算扩散系数
- 导出分析结果

## 技术环境

当前开发环境：

- Java 21
- Maven
- ImageJ 1.x API
- Fiji
- TrackMate 8.1.6

当前 `pom.xml` 中主要依赖包括：

```xml
<dependency>
    <groupId>net.imagej</groupId>
    <artifactId>ij</artifactId>
    <version>1.54p</version>
</dependency>

<dependency>
    <groupId>sc.fiji</groupId>
    <artifactId>TrackMate</artifactId>
    <version>8.1.6</version>
</dependency>