from __future__ import annotations

from rich.traceback import install
install(show_locals=False)

from scipy.ndimage import gaussian_filter

import json
from dataclasses import asdict, dataclass
from pathlib import Path

import numpy as np
import pandas as pd
import tifffile


@dataclass(frozen=True)#创建SimulationConfig类，frozen=True表示该类的实例是不可变的，防止程序运行中参数意外修改
class SimulationConfig:#创建SimulationConfig类对象时定义里面的参数默认值
    width: int = 256 #默认值
    height: int = 256 #默认值
    frames: int = 100 #默认值
    particles: int = 25 #默认值

    # Gaussian particle image
    psf_sigma: float = 1.8 #默认值,PSF点扩散函数高斯宽度σ=1.8，控制光斑大小
    mean_amplitude: float = 170.0 #默认值,粒子平均强度
    amplitude_std: float = 25.0 #默认值,粒子亮度标准差，让粒子明暗有差异，更贴近真实图像
    
    # Particle image variability,不同粒子之间的成像差异
    psf_sigma_std: float =0.20
    amplitude_frame_cv: float =0.08 #同一个粒子在不同帧之间具有大约8%的亮度波动

    # Background and camera noise
    background: float = 25.0 #默认值,全局基础背景亮度
    background_gradient: float = 15.0 #默认值,背景渐变强度，模拟视野边缘明暗不均
    gaussian_noise_sigma: float = 7.0 #默认值.图像高斯噪声强度σ=7，模拟显微镜相机底噪
    use_poisson_noise: bool = True #默认值,是否开启泊松噪声（荧光光子固有随机噪声，默认开启）

    # Realistic illumination and background,真实照明和背景
    illumination_falloff: float = 0.25 
    # 照明衰减，控制图像从中心到边缘的照明衰减比例；
    # 比如图像中心为1，所以1-illumination_falloff=0.75，图像边缘亮度约为中心亮度的75%值越小。
    background_texture_amplitude: float = 8.0 
    #背景纹理强度,数值越大，明暗起伏，噪点越明显；数值越小，背景越平滑，几乎看不出纹理；控制纹理有多强
    background_texture_scale: float = 28.0
    #背景纹理的空间尺度和平滑程度，单位近似理解为像素，数值越小，局部明暗区域越小；数值越大，亮暗发生在更大的区域；控制纹理有多大
    background_temporal_sigma: float = 2.0 
    #控制每一帧之间，整个背景发生的随机整体亮度变化，单位是图像灰度值。服从正态分布，随机帧产生偏移量的范围。
    background_drift_per_frame: float = 0.01 #每经过一帧，背景平均值增加或者减少的数量

    # Particle motion(布朗运动＋定向运动)
    diffusion_px2_per_frame: float = 0.8 #默认值.单帧扩散系数0.8像素²/帧，控制粒子布朗运动快慢
    directed_fraction: float = 0.25 #默认值，有25%的粒子带有定向直线运动（其余纯布朗扩散）
    max_directed_speed: float = 1.5 #默认值，定向运动粒子的最大移动速度（像素/帧）

    # Photophysics，荧光光物理（闪烁、漂白，追踪核心难点）
    blinking_probability: float = 0.04 #默认值，单帧粒子荧光闪烁概率4%，分子临时暗灭
    recovery_probability: float = 0.35 #默认值，闪烁消失后，重新恢复发光的概率35%
    bleaching_probability: float = 0.001 #默认值，单帧分子永久光漂白概率0.1%，分子彻底失活

    # Reproducibility
    random_seed: int = 20260625 #默认值，固定随机种子


def draw_gaussian_spot(
    image: np.ndarray,#一整帧画面
    center_x: float,#粒子中心x坐标
    center_y: float,#粒子中心y坐标
    amplitude: float,#粒子亮度
    sigma: float,#PSF点扩散函数高斯宽度σ
) -> None:# 返回值为空，直接修改原image画布
    """Add one Gaussian particle image to a 2D frame."""#在一张二维图像帧上绘制单个高斯荧光粒子光斑。
    radius = int(np.ceil(4.0 * sigma))

#计算绘制高斯光斑的边界坐标，确保光斑不会超出图像范围
    x_min = max(0, int(np.floor(center_x)) - radius)
    x_max = min(image.shape[1] - 1, int(np.floor(center_x)) + radius)
    y_min = max(0, int(np.floor(center_y)) - radius)
    y_max = min(image.shape[0] - 1, int(np.floor(center_y)) + radius)

    if x_min > x_max or y_min > y_max:#粒子跑出画面，不绘制
        return

    yy, xx = np.mgrid[y_min : y_max + 1, x_min : x_max + 1]

    spot = amplitude * np.exp(
        -(
            (xx - center_x) ** 2
            + (yy - center_y) ** 2
        )
        / (2.0 * sigma**2)
    )

    image[y_min : y_max + 1, x_min : x_max + 1] += spot


def reflect_position(
    position: float,
    velocity: float,
    lower: float,#下边界
    upper: float,#上边界
) -> tuple[float, float]:#返回新坐标，新速度
    """Reflect a particle from image boundaries."""
    if position < lower:
        position = lower + (lower - position)
        velocity = abs(velocity)

    if position > upper:
        position = upper - (position - upper)
        velocity = -abs(velocity)

    return position, velocity


def generate_dataset(
    output_directory: Path,
    config: SimulationConfig,
) -> None:
    output_directory.mkdir(parents=True, exist_ok=True)#mkdir：创建文件夹；parents=True：自动创建多级父目录；exist_ok=True：文件夹已存在不报错

    rng = np.random.default_rng(config.random_seed)#rng = 独立随机数生成器

    # 估计可能出现的较大PSF，防止大光斑太靠近图像边缘
    estimated_max_sigma = (
        config.psf_sigma
        + 3.0 * config.psf_sigma_std
    )

    margin = max(
        8.0,
        5.0 * estimated_max_sigma,
    )

    x = rng.uniform(
        margin,
        config.width - margin,
        config.particles,
    )
    #均匀随机分布；所有粒子初始位置在 margin ~ 画面宽/高-margin 之间，不会贴边。

    y = rng.uniform(
        margin,
        config.height - margin,
        config.particles,
    )

    #normal：正态随机生成亮度；np.maximum：亮度最低限制 30，避免出现极暗无意义粒子。
    amplitude = np.maximum(
        30.0,
        rng.normal(
            config.mean_amplitude,
            config.amplitude_std,
            config.particles,
        ),
    )
    # 为每个粒子生成独立的PSF宽度
    particle_sigma = np.clip(
        rng.normal(
            loc=config.psf_sigma,
            scale=config.psf_sigma_std,
            size=config.particles,
        ),
        0.8,
        3.5,
    )    
    #0.8和3.5防止随机数产生过小或过大的异常光斑

# 默认全部粒子为布朗运动 brownian
    motion_type = np.full(
        config.particles,
        "brownian",
        dtype=object,
    )

# 计算定向运动粒子总数
    directed_count = int(
        round(config.particles * config.directed_fraction)
    )

# 随机选出对应数量粒子，改为定向运动 directed
    directed_indices = rng.choice(
        config.particles,
        directed_count,
        replace=False,#不会重复选中同一个粒子
    )

    motion_type[directed_indices] = "directed"

    angles = rng.uniform(
        0.0,
        2.0 * np.pi,
        config.particles,
    )#随机生成每个粒子运动方向角度，范围 0 ~ 2π 弧度

    speeds = np.zeros(config.particles)#默认所有粒子速度为 0，布朗运动粒子不需要速度

    speeds[directed_indices] = rng.uniform(
        0.3,
        config.max_directed_speed,
        directed_count,
    )#随机生成定向运动粒子速度，范围 0.3 ~ 最大速度，避免出现过慢粒子。

    vx = speeds * np.cos(angles)
    vy = speeds * np.sin(angles)#计算每个粒子在 x 和 y 方向的速度分量，vx = v * cos(θ)，vy = v * sin(θ)

    blinking = np.zeros(config.particles, dtype=bool)#是否荧光闪烁（临时看不见）
    bleached = np.zeros(config.particles, dtype=bool)#否永久光漂白（彻底消失

    movie = np.zeros(
        (
            config.frames,
            config.height,
            config.width,
        ),
        dtype=np.float32,
    )#movie：三维数组 [帧数, 高, 宽]，存放整套时序图像。

    truth_rows: list[dict[str, object]] = []

    # In 2D Brownian motion:
    # each coordinate step has variance 2D.
    brownian_step_sigma = np.sqrt(
        2.0 * config.diffusion_px2_per_frame
    )
    #二维布朗运动公式，计算每帧粒子随机游走幅度


    # ============================================================
    # 生成更加真实的固定空间背景
    # 包括：
    # 1. 中心和边缘照明差异
    # 2. 水平方向渐变
    # 3. 平滑的低频随机背景纹理
    # ============================================================

    background_y, background_x = np.mgrid[0 : config.height,0 : config.width]

    image_center_x = (config.width - 1) / 2.0
    image_center_y = (config.height - 1) / 2.0

    # 把坐标归一化到大约 -1 到 1
    normalized_x = (
        background_x - image_center_x
    ) / max(image_center_x, 1.0)

    normalized_y = (
        background_y - image_center_y
    ) / max(image_center_y, 1.0)

    radius_squared = (
        normalized_x**2
        + normalized_y**2
    )

    # 模拟显微镜照明不均匀：
    # 中心较亮，边缘较暗
    illumination_profile = (
        1.0
        - config.illumination_falloff
        * np.clip(
            radius_squared,
            0.0,
            1.0,
        )
    )

    # 保留原有的水平方向背景渐变
    gradient_x = np.linspace(
        0.0,
        config.background_gradient,
        config.width,
        dtype=np.float32,
    )

    # 先生成随机白噪声
    background_texture = rng.normal(
        loc=0.0,
        scale=1.0,
        size=(
            config.height,
            config.width,
        ),
    ).astype(np.float32)

    # 对白噪声进行高斯平滑，
    # 得到缓慢变化的低频背景纹理
    background_texture = gaussian_filter(
        background_texture,
        sigma=config.background_texture_scale,
    )

    # 让纹理的平均值变成0
    background_texture -= np.mean(
        background_texture
    )

    # 将纹理归一化到标准差约为1
    texture_std = float(
        np.std(background_texture)
    )

    if texture_std > 0.0:
        background_texture /= texture_std

    # 设置背景纹理的实际强度
    background_texture *= (
        config.background_texture_amplitude
    )

    # 合成固定空间背景
    background_image = (
        config.background
        * illumination_profile
        + gradient_x[np.newaxis, :]
        + background_texture
    ).astype(np.float32)

    # 防止背景出现负值
    background_image = np.clip(
        background_image,
        0.0,
        None,
    )

#这样每帧的背景不再完全相同，会产生一个很慢的长期漂移
    for frame in range(config.frames):
        # 每一帧整体背景的随机波动
        random_background_offset = rng.normal(
            loc=0.0,
            scale=config.background_temporal_sigma,
        )

        # 背景随时间缓慢漂移
        gradual_background_drift = (
            frame
            * config.background_drift_per_frame
        )

        temporal_background_offset = (
            random_background_offset
            + gradual_background_drift
        )

        frame_image = (
            background_image
            + temporal_background_offset
        ).astype(np.float32)

        frame_image = np.clip(
            frame_image,
            0.0,
            None,
        )
        for particle in range(config.particles):
            if frame > 0:# 生成x、y布朗随机位移
                dx_brownian = rng.normal(
                    0.0,
                    brownian_step_sigma,
                )

                dy_brownian = rng.normal(
                    0.0,
                    brownian_step_sigma, # 原有坐标 + 布朗位移 + 定向分速度
                )

                x[particle] += (
                    dx_brownian + vx[particle]
                )

                y[particle] += (
                    dy_brownian + vy[particle]
                )

                x[particle], vx[particle] = reflect_position(
                    x[particle],
                    vx[particle],
                    margin,
                    config.width - margin,
                )

                y[particle], vy[particle] = reflect_position(
                    y[particle],
                    vy[particle],
                    margin,
                    config.height - margin,
                )

            if (
                not bleached[particle]
                and rng.random()
                < config.bleaching_probability
            ):
                bleached[particle] = True

            if not bleached[particle]:
                if blinking[particle]:
                    if (
                        rng.random()
                        < config.recovery_probability
                    ):
                        blinking[particle] = False
                else:
                    if (
                        rng.random()
                        < config.blinking_probability
                    ):
                        blinking[particle] = True

            visible = (
                not blinking[particle]
                and not bleached[particle]
            )

            if visible:
                # 模拟同一个粒子在不同帧之间的轻微亮度波动
                amplitude_factor = rng.normal(
                    loc=1.0,
                    scale=config.amplitude_frame_cv,
                )

                # 防止极少数情况下产生负值或过低亮度
                amplitude_factor = max(
                    0.15,
                    amplitude_factor,
                )

                current_amplitude = (
                    float(amplitude[particle])
                    * amplitude_factor
                )
            else:
                current_amplitude = 0.0

            if visible:
                draw_gaussian_spot(
                    frame_image,
                    center_x=float(x[particle]),
                    center_y=float(y[particle]),
                    amplitude=current_amplitude,
                    sigma=float(particle_sigma[particle]),
                )
                #绘图时使用每个粒子自己sigma

            truth_rows.append(
                {
                    "particle": particle,
                    "frame": frame,
                    "x": float(x[particle]),
                    "y": float(y[particle]),
                    "base_intensity": float(amplitude[particle]),
                    "intensity": current_amplitude,
                    "visible": int(visible),
                    "blinking": int(blinking[particle]),
                    "bleached": int(bleached[particle]),
                    "motion_type": motion_type[particle],
                    "vx": float(vx[particle]),
                    "vy": float(vy[particle]),
                    "psf_sigma": float(particle_sigma[particle]),
                    "background_offset": float(temporal_background_offset),
                }
            )

        if config.use_poisson_noise:
            frame_image = rng.poisson(
                np.clip(frame_image, 0.0, None)
            ).astype(np.float32)

        frame_image += rng.normal(
            0.0,
            config.gaussian_noise_sigma,
            frame_image.shape,
        ).astype(np.float32)

        movie[frame] = np.clip(
            frame_image,
            0.0,
            65535.0,
        )

    movie_uint16 = movie.astype(np.uint16)

    tiff_path = (
        output_directory
        / "synthetic_movie.tif"
    )

    csv_path = (
        output_directory
        / "ground_truth.csv"
    )

    parameter_path = (
        output_directory
        / "parameters.json"
    )

    tifffile.imwrite(
        tiff_path,
        movie_uint16,
        imagej=True,
        metadata={
            "axes": "TYX",
            "unit": "pixel",
            "fps": 1.0,
        },
    )

    truth_table = pd.DataFrame(truth_rows)
    truth_table.to_csv(
        csv_path,
        index=False,
        encoding="utf-8-sig",
    )

    with parameter_path.open(
        "w",
        encoding="utf-8",
    ) as file:
        json.dump(
            asdict(config),
            file,
            indent=2,
            ensure_ascii=False,
        )

    visible_rows = int(
        truth_table["visible"].sum()
    )

    print("Dataset generation completed.")
    print(f"Movie: {tiff_path}")
    print(f"Ground truth: {csv_path}")
    print(f"Parameters: {parameter_path}")
    print(f"Frames: {config.frames}")
    print(f"Particles: {config.particles}")
    print(f"Visible detections: {visible_rows}")


if __name__ == "__main__":
    project_root = Path(__file__).resolve().parent.parent

    output_directory = (
        project_root
        / "synthetic_dataset"
        / "dataset_001"#输出数据文件夹的命名
    )

    #simulationConfig是模拟配置类，专门用来存放生成合成显微影像的全部仿真参数。configuration是自定义变量名
    configuration = SimulationConfig(
        frames=50,
        particles=20,
        background_gradient=10.0,
        illumination_falloff=0.20,
        background_texture_amplitude=5.0,
        background_temporal_sigma=1.5,
        gaussian_noise_sigma=7.0,
        blinking_probability=0.02,
        bleaching_probability=0.0005,
        diffusion_px2_per_frame=0.8,

    )

    generate_dataset(
        output_directory,
        configuration,
    )