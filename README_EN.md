# Cloud Tectonics (云构营造)

[🌐 中文版](README.md)

`Cloud Tectonics` is a parametric Chinese architecture generator mod for Minecraft (1.20.1) built on the Architectury multi-platform loader (supporting both Forge and Fabric). It translates classical Chinese architectural treatises (such as the Song Dynasty *Yingzao Fashi* 《营造法式》) into high-precision, performant structural voxel assemblies.

---

## 🏛️ What We Implement / Project Capabilities

The mod implements a parametric architectural formula engine that dynamically compiles timber-frame structures directly in-game based on configurable parameters:

* **Da Mu Zuo Assembly (大木作构件程序化拼装)**: Procedurally generates column grids, ties (额枋), primary trusses (three, five, or seven-step beams), purlins (檩), rafters (椽), and roof tiling.
* **Dougong Bracket System (斗拱铺作系统)**: Parametric building of traditional Gong-Dou bracket sets, supporting complex configurations (such as Ang cantilevers utilizing Wedge/slanted geometries).
* **Multi-story Timber Towers (重檐与多层楼阁堆叠)**: Supports vertical stacking of pavilions with floor decks, waist eaves (腰檐), and automatic column grid shrinkage (收分) for upper levels.
* **Collision Proxy Grid (物理碰撞与防空气墙代理)**: Spawns server-side collision proxy blocks to handle block breaking and interaction. Uses the **Separating Axis Theorem (SAT)** to bind proxy grids tightly to OBB boundaries, completely avoiding annoying invisible walls.

---

## 🌟 Key Features

* **1/16 Voxel-Pixel Snapping (1/16 像素级高精对齐)**: Rounds all local offsets and sizes to the $1/16$ block grid ($0.0625$ units), ensuring clean joints and preventing rendering gaps.
* **Cai-Fen Proportion System (大木作“材份”模块比例)**: Uses the song-era modular system ($1 \text{ Cai} = 15 \text{ Fen}$). Component scale, height, and spacing automatically adapt, enabling seamless architectural scaling.
* **Skeletal Node Graph (骨架节点树驱动)**: Defines central skeletal nodes (column tops, purlins, ridges). Components hook directly onto these nodes, ensuring correct stretching and preventing floating or clipping.
* **O(1) Static VBO Rendering (VBO 烘焙高性能渲染)**: Bakes thousands of timber components into a single GPU `VertexBuffer` (VBO) when parameters change. This reduces draw calls to $O(1)$, ensuring zero frame-rate drops.
* **Ponder-style 3D Editor (3D 交互营造编辑器)**: Open a real-time 3D interactive UI by right-clicking the Anchor Block to customize parameters (bays, depths, height, pitch, etc.) and preview the building.

---

## 🎮 How to Use / Usage Instructions

1. **Building Wand (手持“建筑棒”)**:
   * **Shift + Right-Click**: Opens the local JSON Preset Selection Screen to save or load layout profiles.
   * **Right-Click ground**: Places a 3D hologram preview of the building at the targeted position.
   * **Mouse Wheel**: Rotates the hologram in $15^\circ$ increments.
2. **Anchor Block (建筑中心锚点方块)**:
   * Right-click the placed Anchor Block to open the **3D Interactive Editor UI**. Adjust structural parameters (bays, depths, height, pitch, Dougong level) in real time.
   * Clicking "Confirm" spawns server-side proxy blocks and bakes the final VBO render meshes.
3. **Debug Render (物理碰撞可视化)**:
   * Press **F10** (in debug mode) to toggle an X-Ray overlay visualizing the exact bounding boxes of the physical collision proxies in the world.

---

## 🏗️ System Architecture / Project Structure

The project utilizes the **Architectury Multi-loader** structure, decoupling math and generation logic from platform-specific Minecraft APIs:

* **`common/` (Common Core)**:
  * `math/`: Core engine containing Cai-Fen calculations, `SkeletonNodeGraph`, OBB SAT collision math, and Xieshan roof parametric formulas.
  * `block/` & `blockentity/`: Anchor block logic and proxy packet sync.
  * `client/`: Interactive editors, preset managers, and the VBO block renderer.
* **`forge/` (Forge Subproject)**:
  * Platform entry point, registry handlers, keybindings, and the `ProxyBlockBakedModel` dynamic override.
* **`fabric/` (Fabric Subproject)**:
  * Platform entry point, registry handlers, and Fabric API event listeners (world rendering, HUD overlay, and tick events).

---

## 🗺️ Technical Roadmap & Evolution / Future Plans

Our roadmap is focused on transitioning from individual structural generation to an expressive, culturally accurate voxel engine:

1. **Component-Level Partial Destruction**: High-precision raycast intersection with sub-components. Left-clicking a proxy block will calculate intersection points with all sub-component AABBs, allowing players to destroy individual tiles or beams while keeping the surrounding structure intact.
2. **Snapping Interior Decoration**: Virtual decorations stored as local coordinates. Placing furniture snaps items to surfaces (walls/floors) using raycasting, and merges voxel shapes for precise player collision.
3. **Custom Plaques, Couplets & Paintings**: Standardized 3D models for Chinese plaques and couplets. Render anti-aliased calligraphy fonts (`.ttf`/`.otf`) dynamically onto surfaces using Java AWT `Graphics2D` and auto-crop local images to map onto paintings.
4. **Specialized World Editor**: Lightweight non-destructive copy/paste operations using the Building Wand. Supports axis nudges, mirroring structures, and undo/redo transaction stacks.
5. **Radial Quick Menu & HUD Dock**: Hotkey-triggered frosted-glass radial menu for quick actions (rotate, duplicate, undo). HUD docks and Ctrl/Shift + scroll modifiers provide swift, keyboard-free building.
6. **Component Grouping & Prefabs**: Group components into templates defined in JSON configs (e.g. table-chair sets, truss segments) using nested matrix trees.

---

## 🛠️ Build & Run / Compilation

* **Compile Java Code (Verify source compile)**:
  ```powershell
  ./gradlew compileJava
  ```
* **Build production JARs (Assemble all jars)**:
  ```powershell
  ./gradlew build -x test
  ```
* **Run local sandbox client**:
  * **Forge**: `./gradlew :forge:runClient`
  * **Fabric**: `./gradlew :fabric:runClient`
