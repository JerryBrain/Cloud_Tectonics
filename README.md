# Cloud Tectonics (云构营造)

A multi-platform (Forge & Fabric) Minecraft mod for parametric generation of traditional Chinese architecture, featuring high-precision block alignment, historical proportion modules (Cai-Fen), multi-story stacking, and custom wedge-shaped geometry.

这是一个支持多平台（Forge & Fabric）的 Minecraft 参数化华夏古建营造模组。它具备高精度方块网格对齐、历史比例模块（材份制）、多层重檐叠落以及自定义斜切角（昂）几何体渲染等功能。

---

## 🌟 Key Features / 主要特性

### 1. Parametric Chinese Architecture (参数化华夏营造)
- **EN**: Generates classical Chinese roof shapes (e.g., Xieshan Roof / 歇山顶) and timber frames dynamically based on configurable parameters (bays, depths, pitch, eaves length, and corner lift).
- **ZH**: 基于可配置参数（开间、进深、屋面坡度、出檐长度、翼角起翘等）动态生成经典的华夏大木作与屋顶结构（如歇山顶）。

### 2. Cai-Fen Proportion System (材份制比例比例)
- **EN**: Implements the ancient "Yingzao Fashi" modular system ($1\text{ Cai} = 15\text{ Fen}$). All component dimensions (column diameters, beam thickness, Dougong modules) scale proportionally.
- **ZH**: 引入《营造法式》中的材份制比例系统（1 材 = 15 分），所有构件尺寸（柱径、梁高、斗拱模块）均按比例完美缩放。

### 3. Pixel-Accurate Snapping (亚像素网格对齐)
- **EN**: Snaps all structural boundaries to the $1/16$ grid ($0.0625$ blocks) to guarantee perfect Minecraft-style pixel alignment with zero visual gaps or floating-point overlap errors.
- **ZH**: 将所有构件边界严格对齐至 $1/16$ 像素网格（0.0625 方块），确保 Minecraft 方块风格的完美对齐，消除悬空与错位。

### 4. Ang Wedge Geometry (斜切角“昂”结构)
- **EN**: Uses custom `WEDGE` shape rendering to support the slanted cantilever structures ("Ang" 昂) of Dougong, built using efficient GPU degenerate-quad baking.
- **ZH**: 使用自定义的 `WEDGE`（三棱镜/斜切块）几何体渲染，支持斗拱中倾斜的“昂”结构，并通过 GPU 顶点烘焙优化渲染性能。

### 5. Multi-Story Stacking (重檐多层楼阁)
- **EN**: Supports vertical stacking for multi-story pagodas/pavilions with automatic column grid shrinkage (收分) and mezzanine waist eaves (腰檐).
- **ZH**: 支持垂直链式叠落生成多层楼阁与重檐殿宇，具备柱网自动收分和楼层间腰檐、楼面铺作的自动构建。

### 6. Seamless Rendering (极小接缝消除)
- **EN**: Utilizes a visual geometry inflation of `0.005` blocks to eliminate sub-pixel rasterization seams on roof tiles and wall panels, while keeping physics collision boxes perfectly snap-aligned.
- **ZH**: 对渲染几何体应用 `0.005` 像素级微小膨胀，消除瓦片和山墙板在 GPU 栅格化时产生的亚像素缝隙，同时保持精准的物理碰撞箱。

---

## 📂 Project Structure / 项目结构

```
.
├── common/             # Common code & parametric math library (通用逻辑与大木作数学模型)
├── forge/              # Forge-specific platform implementations & proxy blocks (Forge 适配层)
├── fabric/             # Fabric-specific platform implementations (Fabric 适配层)
└── build.gradle        # Multi-platform gradle setup (多平台构建配置)
```

---

## 🛠️ Build & Run / 构建与运行

### Prerequisite / 前提条件
- **JDK 17** or higher.

### Run client locally / 本地运行开发客户端
* **Forge**:
  ```bash
  ./gradlew :forge:runClient
  ```
* **Fabric**:
  ```bash
  ./gradlew :fabric:runClient
  ```

### Build production JARs / 构建发布包
```bash
./gradlew build
```
The output mod jars will be located in `forge/build/libs/` and `fabric/build/libs/`.

---

## 📄 License / 许可证
This project is licensed under the MIT License - see the `LICENSE.txt` file for details.
