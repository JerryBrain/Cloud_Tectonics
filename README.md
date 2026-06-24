# Cloud Tectonics (云构营造)

`Cloud Tectonics` is a parametric Chinese architecture generator mod for Minecraft (1.20.1) built on the Architectury multi-platform loader (supporting both Forge and Fabric). It translates classical Chinese architectural treatises (such as the Song Dynasty *Yingzao Fashi* 《营造法式》) into high-precision, performant structural voxel assemblies.

`云构营造` 是一个基于 Architectury 多端框架（支持 Forge 与 Fabric）的 Minecraft (1.20.1) 参数化华夏营造模组。它将华夏大木作营造规则（如宋代《营造法式》）转化为高精度、高性能的参数化三维木构与大屋顶体素结构。

---

## 🌟 Core Features & Capabilities / 项目特色与模组功能

### 1. What This Mod Can Do / 模组功能用途
* **Da Mu Zuo Procedural Assembly (大木作程序化生成)**: Dynamically generates columns, ties (额枋), primary beams (梁架), purlins (檩), rafters (椽), and classical eaves layers based on configurable architectural templates.
* **Dougong Bracket Sets (斗拱铺作系统)**: Parametric building of traditional Gong-Dou bracket sets, supporting complex configurations (e.g., Ang cantilevers using Wedge geometries/斜向三角昂).
* **Multi-story Timber Towers (多层阁楼重檐)**: Vertical stack generator supporting automatic column net shrinkage (收分), mezzanine waist eaves (腰檐), and floor decks.
* **Collision Proxy Grid (物理碰撞代理)**: Spawns lightweight, accurate physical proxy blocks mapped onto the complex OBB boundaries of the building, preventing large "invisible wall" artifacts while protecting structures.

### 2. Project Features / 项目特色
* **1/16 Voxel-Pixel Snapping (1/16 像素级高精对齐)**: Locks all component boundaries to the $1/16$ block grid ($0.0625$ units), preventing model joints from overlapping or floating.
* **Cai-Fen Proportion System (营造材份比例制)**: Sells all component thickness, spacing, and diameters proportionally based on standard *Cai* and *Fen* units ($1 \text{ Cai} = 15 \text{ Fen}$).
* **Skeletal Node Graph (骨架节点树驱动)**: Anchors components (beams, purlins, tiles) to dynamically shifting nodes (e.g. column tops) to prevent clipping and floating when changing dimensions.
* **O(1) Static VBO Rendering (VBO 烘焙高性能渲染)**: Bakes thousands of timber components into a single GPU `VertexBuffer` (VBO), reducing draw calls to $O(1)$ and preventing tick-by-tick lag.
* **Ponder-style 3D Editor (3D 交互营造编辑器)**: Allows developers/players to configure timber dimensions, roof pitch, bays, and depths inside a real-time 3D interactive UI.

---

## 🎮 How to Use / 使用说明

1. **Building Wand (建筑棒)**:
   * **Shift + Right-Click (Shift+右击)**: Opens the local JSON Preset Selection Screen (营造预设选择面板) to browse or save layout profiles.
   * **Right-Click ground (右击地面)**: Spawns a 3D hologram preview (虚影) of the target building.
   * **Mouse Wheel (单滚轮)**: Rotates the holographic preview in $15^\circ$ steps.
2. **Anchor Block (建筑中心锚点方块)**:
   * Right-click the placed Anchor Block to open the **3D Interactive Editor UI** (3D 交互营造编辑器). Adjust the number of bays, depths, column height, roof pitch, and Dougong level in real time.
   * Confirming the build (确认建造) triggers the server to place physical proxy blocks and bakes the final VBO meshes.
3. **Debug Render (物理碰撞可视化)**:
   * Press **F10** (in debug mode) to trigger an X-Ray overlay visualizing the exact bounding box range of the physical collision proxies.

---

## 🏗️ Architecture & Decoupling / 架构设计与平台解耦

The codebase utilizes the **Architectury Multi-loader** structure, decoupling core mathematical logic from platform-specific APIs:

* **`common/` (公共核心模块)**:
  * `math/`: Core generator engine containing Cai-Fen calculations, `SkeletonNodeGraph`, component boundary snapping, and OBB collision intersection using the **Separating Axis Theorem (SAT)**.
  * `block/` & `blockentity/`: Anchor block logic and proxy coordinate forwarding.
  * `client/`: Interactive editors, preset managers, and the VBO-baked block atlas renderer.
* **`forge/` (Forge 平台模块)**:
  * Platform entry point, registry redirects, and Forge-specific events (e.g. keybindings, preview rendering, and `ProxyBlockBakedModel` dynamic override).
* **`fabric/` (Fabric 平台模块)**:
  * Platform entry point, registry redirects, and Fabric API event hook listeners (world rendering, HUD overlay, and tick listeners).

---

## 🗺️ Technical Roadmap & Evolution / 技术路线与未来演进

Based on the core engine, the project is planned to evolve along the following roadmap:

### 1. Component Partial Destruction (构件级局部破坏系统)
* **EN**: Raycast-based high-precision line-of-sight intersection. Clicking a proxy block calculates intersection points with all sub-component AABBs, allowing players to destroy individual tiles or beams while keeping the surrounding structure intact.
* **ZH**: 视角射线求交检测。左击破坏物理代理方块时，自动判定与子构件包围盒（AABB）的真实交点，允许玩家单独敲下部分瓦片或横梁而保留其他木构，动态更新渲染 VBO。

### 2. Snapping Interior Decoration (高精度吸附式内饰系统)
* **EN**: Support virtual decorations (`DecorationComponent`) with dynamic floating-point coordinates. Utilize raycasting to snap furniture to timber faces (wall-snapping/ground-alignment) and merge voxel shapes for precise collision.
* **ZH**: 虚拟内饰子组件，使用局部浮点数空间三维坐标。在放置时进行表面几何碰撞计算，使其精准吸附于构件面并进行多内饰碰撞箱融合。

### 3. Custom Plaques, Couplets & Paintings (牌匾对联与挂画自定义)
* **EN**: Render custom text and image uploads directly onto in-game plaques, scrolls, and couplets. Utilizes AWT `Graphics2D` to dynamically layout calligraphy fonts (`.ttf`/`.otf`) and clip image ratios onto dynamic textures.
* **ZH**: 牌匾、对联与中式挂画的自定义文字/图片渲染。在客户端通过 Java AWT 的 `Graphics2D` 在内存中渲染反走样书法字体与自适应裁剪的本地图片，实时上传至动态纹理。

### 4. Specialized World Editor (专属古建世界编辑器)
* **EN**: Provides non-destructive, single-frame NBT copy/paste operations using the Building Wand. Supports axis nudges, mirroring structures, and transaction stacks for complete undo/redo capabilities.
* **ZH**: 专属参数化轻量级编辑器。手持建筑棒可一键克隆/粘贴锚点 NBT，支持方向键无卡顿轴向平移、对称镜像，以及防地形压碎的撤销/重做操作栈。

### 5. Radial Quick Menu & HUD Dock (免指令轮盘与 HUD 快速交互)
* **EN**: A frosted-glass-styled Radial Menu triggered via hotkeys to execute actions (rotate, duplicate, mirror, undo). Dock layouts and combinations of modifier keys (Ctrl/Shift + scroll) make building interactive and friction-free.
* **ZH**: 玻璃拟态环形轮盘与 HUD Dock 栏。支持快捷热键呼出极坐标扇区轮盘进行编辑操作，使用 Ctrl/Shift+滚轮微调高度及角度。

### 6. Component Grouping & Prefabs (构件组合与内饰预制件)
* **EN**: Group components into templates (e.g. table-chair sets, truss segments) defined via JSON. Support layout transformations using nesting trees or flat matrices.
* **ZH**: 支持从 NBT/JSON 结构导入预制件组合（如桌椅茶具套件、特定开间梁架组），在渲染与碰撞系统中使用嵌套矩阵树递归计算。

---

## 🛠️ Build & Run / 编译与调试

* **Compile Java Code (编译验证)**:
  ```powershell
  ./gradlew compileJava
  ```
* **Build remap distribution JARs (构建分发包)**:
  ```powershell
  ./gradlew build -x test
  ```
* **Run client locally (本地沙盒调试)**:
  * **Forge**: `./gradlew :forge:runClient`
  * **Fabric**: `./gradlew :fabric:runClient`
