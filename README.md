# Cloud Tectonics (云构营造)

A multi-platform, parametric architectural generation framework for Minecraft, designed to procedurally reconstruct traditional Chinese timber-frame structures ("Da Mu Zuo" 大木作) with pixel-level grid precision.

这是一个面向 Minecraft 的多平台参数化华夏营造生成框架，旨在高精度方块网格下以程序化方式复原华夏大木作木构建筑体系。

---

## 🏛️ What We Implement / 项目实现

`Cloud Tectonics` implements a parametric math compiler that translates classical Chinese architectural treatises (such as the Song Dynasty *Yingzao Fashi* 《营造法式》) into structural voxel components in Minecraft:

* **Da Mu Zuo Construction (大木作结构生成)**: Dynamic generation of column grids, primary beams, purlins, rafters, and the complex Gong-Dou bracket set (斗拱) system.
* **Classical Roof Shapes (华夏大屋顶形制)**: Parameterized mathematical models for curved roof slopes, corner lift (翼角起翘), and eaves extensions, including classical forms such as the Xieshan Roof (歇山顶).
* **Modular Proportion System (历史材份制比例)**: Scaling of all timber components (diameters, thickness, span) using the historically accurate *Cai-Fen* (材份制) module units ($1 \text{ Cai} = 15 \text{ Fen}$).
* **Multi-story Structures (多层楼阁叠落)**: Support for vertical stacking and grid shrinkage (收分) for multi-story pavilions, automated with floor decks and waist eaves (腰檐).
* **Wedge Geometry Rendering (斜面“昂”几何体)**: Custom wedge shape rendering for the slanted cantilever structures ("Ang" 昂) of Dougong, baked directly into GPU Vertex Buffers (VBOs).

---

## 🏗️ Architecture / 系统架构

The framework is structured as a multi-platform modular project using Architectury, decoupling pure mathematical layout calculation from platform-specific rendering and voxel manipulation:

```
.
├── common/             # Main Architecture Engine & Mathematical Generator
│   ├── math/           # Cai-Fen calculations, Skeleton Node Graph, Component baking
│   ├── block/          # Anchor control logic & proxy blocks
│   └── client/         # Client-side UI & O(1) Draw Call static VBO renderer
├── forge/              # Forge-specific platform implementations & proxy hookup
└── fabric/             # Fabric-specific platform implementations & event handler hooks
```

### Key Architectural Pillars:
1. **Skeletal Node Graph (骨架节点树)**: Component positions are not calculated using absolute coordinates, but rather anchored to a skeletal node tree (columns, purlin centers, ridge lines). When parameters change, the nodes shift, and components adapt dynamically to prevent clipping.
2. **Static Vertex Baking (静态顶点烘焙)**: Instead of costly tick-by-tick rendering, all component geometries are baked into a single `VertexBuffer` (VBO) on-demand, achieving $O(1)$ Draw Call performance for complex structures.
3. **Decoupled Proxy Blocks (解耦代理方块)**: A central control block manages the structure's state, while lightweight "Proxy Blocks" handle local collision shapes and in-world interaction on-demand.

---

## 🗺️ Roadmap & Future / 技术路线与未来规划

Our roadmap is focused on transitioning from individual structural generation to an expressive, culturally accurate voxel engine:

### 🌐 Phase 1: Structural Expansion (形制扩展 - 近期)
* **More Roof Forms (多种屋顶支持)**: Implement additional classical roof types (e.g., Wudian/庑殿顶, Xuanshan/悬山顶, Yingshan/硬山顶, and Cuanjian/攒尖顶).
* **Interior & Partition Systems (室内与隔断系统)**: Add parametric generation of doors, windows, decorative screens (Gehan/隔扇), and ceilings (Zaojing/藻井).
* **Material Palette Mapping (材质画板映射)**: Create modular material presets matching historical periods (e.g., Ming/Qing vermilion-gold paint vs. Song/Yuan natural timbers).

### 🚀 Phase 2: Procedural Layouts & AI (程序化布设与人工智能 - 中期)
* **Compound Layouts (合院组群生成)**: Support generating entire compound complexes (Siheyuan/四合院) and temple layouts dynamically with corridors and gates.
* **AI-Assisted Reconstruction (AI 辅助营造)**: Combine text-based prompts or sketches with our parametric constraint compiler to auto-generate customized historical buildings.
* **Terrain Adaptation (地形自适应台基)**: Implement automated terrace and foundation level alignment based on the slope of the Minecraft terrain.

### 🌌 Phase 3: Large-Scale Simulation (大规模城池模拟 - 远期)
* **Ancient City Procedural Generation (古建村落与城池生成)**: Generate entire historical towns or city grids (similar to ancient Chang'an/长安) dynamically.
* **Dynamic Weathering (动态自然风化)**: Introduce visual weathering, moss growth, and material decay algorithms to simulate the passage of time on timber structures.
