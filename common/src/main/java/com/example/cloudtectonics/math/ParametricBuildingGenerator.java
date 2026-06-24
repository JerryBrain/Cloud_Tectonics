package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * 华夏古建参数化生成器 (方块风格，纯色材质)
 * 翻译自 HTML5/Three.js 歇山营造大木作数学模型
 */
public class ParametricBuildingGenerator {

    // 纯色材质颜色常量定义
    public static final int COLOR_VERMILION = 0xFF9A2A22;      // 柱、额枋 (朱红)
    public static final int COLOR_JADE_GREEN = 0xFF1E5A44;     // 绿旋子彩画
    public static final int COLOR_INDIGO_BLUE = 0xFF1D3557;    // 蓝旋子彩画
    public static final int COLOR_GOLD_PAINT = 0xFFCDA234;     // 描金、兽、斗
    public static final int COLOR_NATURAL_WOOD = 0xFFB58450;   // 梁、檩、椽 (原木)
    public static final int COLOR_SLATE_GREY = 0xFF707A80;     // 瓦片 (青灰)
    public static final int COLOR_GLAZED_GOLD_TILE = 0xFFD4AF37;// 琉璃正脊 (金黄)
    public static final int COLOR_STONE_BASE = 0xFF7B828A;     // 柱础、台基 (石灰)
    public static final int COLOR_GABLE_WALL = 0xFFBF5D38;     // 山花板 (红土)
    public static final int COLOR_YOUNG_CORNER_BEAM = 0xFFD4A76A;// 仔角梁 (浅木)

    public static class SkeletonNodeGraph {
        private final java.util.Map<String, Vector3f> nodes = new java.util.HashMap<>();

        public void put(String key, float x, float y, float z) {
            nodes.put(key, new Vector3f(x, y, z));
        }

        public Vector3f get(String key) {
            Vector3f node = nodes.get(key);
            if (node == null) {
                return new Vector3f(0, 0, 0);
            }
            return new Vector3f(node);
        }
    }

    public static class BuildingConfig {
        public final int bays;
        public final int depths;
        public final float widthMid;
        public final float widthSide;
        public final float depthStep;
        public final float colHeight;
        public final float roofPitch;
        public final float eavesLen;
        public final float gableSetback;
        public final float cornerLift;
        public final int dougongLv;

        public final float caiSize;
        public final float fenSize;

        public float baseY = 0.0f;
        public int storyIndex = 0;
        public int totalStories = 1;
        public final SkeletonNodeGraph nodeGraph = new SkeletonNodeGraph();

        public float W;
        public float D;
        public float[] gridX;
        public float[] gridZ;
        public float X_gable;
        public float Z_gable_limit;

        public BuildingConfig(int bays, int depths, float widthMid, float widthSide, float depthStep,
                              float colHeight, float roofPitch, float eavesLen, float gableSetback,
                              float cornerLift, int dougongLv) {
            this.bays = bays;
            this.depths = depths;
            this.widthMid = widthMid;
            this.widthSide = widthSide;
            this.depthStep = depthStep;
            this.colHeight = colHeight;
            this.roofPitch = roofPitch;
            this.eavesLen = eavesLen;
            this.gableSetback = gableSetback;
            this.cornerLift = cornerLift;
            this.dougongLv = dougongLv;

            // Calculate proportional Cai-Fen modules: 1 Cai = 15 Fen.
            // Cai scales with column height (e.g. 4.0m height -> 0.20m Cai).
            // We align Cai size to pixel subdivisions (1/16 block) for pixel accuracy.
            this.caiSize = Math.max(0.125f, Math.round((colHeight / 20.0f) * 16.0f) / 16.0f);
            this.fenSize = this.caiSize / 15.0f;

            // 计算柱网间距
            List<Float> xList = new ArrayList<>();
            if (bays == 3) {
                xList.add(-widthMid / 2.0f - widthSide);
                xList.add(-widthMid / 2.0f);
                xList.add(widthMid / 2.0f);
                xList.add(widthMid / 2.0f + widthSide);
            } else { // 5 bays
                xList.add(-widthMid / 2.0f - 2.0f * widthSide);
                xList.add(-widthMid / 2.0f - widthSide);
                xList.add(-widthMid / 2.0f);
                xList.add(widthMid / 2.0f);
                xList.add(widthMid / 2.0f + widthSide);
                xList.add(widthMid / 2.0f + 2.0f * widthSide);
            }
            gridX = new float[xList.size()];
            for (int i = 0; i < xList.size(); i++) {
                gridX[i] = xList.get(i);
            }

            this.D = depths * depthStep;
            gridZ = new float[depths + 1];
            for (int i = 0; i <= depths; i++) {
                gridZ[i] = -this.D / 2.0f + i * depthStep;
            }

            this.W = gridX[gridX.length - 1] - gridX[0];
            this.X_gable = this.W / 2.0f - gableSetback;
            this.Z_gable_limit = this.D / 2.0f - gableSetback;

            buildSkeletonNodes();
        }

        public void buildSkeletonNodes() {
            float baseH = 0.6f;
            int numX = gridX.length;
            int numZ = gridZ.length;
            
            // Column base and tops
            for (int ix = 0; ix < numX; ix++) {
                float x = gridX[ix];
                for (int iz = 0; iz < numZ; iz++) {
                    float z = gridZ[iz];
                    boolean isEaveCol = (iz == 0 || iz == numZ - 1 || ix == 0 || ix == numX - 1);
                    float h = isEaveCol ? colHeight : colHeight + 0.3f;
                    
                    nodeGraph.put("col_base_" + ix + "_" + iz, x, baseY + baseH, z);
                    nodeGraph.put("col_top_" + ix + "_" + iz, x, baseY + baseH + h, z);
                }
            }

            // Purlin nodes
            for (int iz = 0; iz < numZ; iz++) {
                float z = gridZ[iz];
                float y = getRoofHeight(0.0f, z, this) - 0.26f;
                nodeGraph.put("purlin_center_" + iz, 0.0f, y, z);
                for (int ix = 0; ix < numX; ix++) {
                    float x = gridX[ix];
                    float yx = getRoofHeight(x, z, this) - 0.26f;
                    nodeGraph.put("purlin_" + ix + "_" + iz, x, yx, z);
                }
            }

            // Ridge node
            float H_ridge = getRoofHeight(0.0f, 0.0f, this);
            nodeGraph.put("ridge_center", 0.0f, H_ridge, 0.0f);
        }
    }

    public static List<StructureComponent> generate(int bays, int depths, float widthMid, float widthSide, float depthStep,
                                                    float colHeight, float roofPitch, float eavesLen, float gableSetback,
                                                    float cornerLift, int dougongLv,
                                                    boolean showRoof, boolean showRafters, boolean showPurlins,
                                                    boolean showBeams, boolean showDougong, boolean showColumns, boolean showBase) {
        return generate(bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv,
                showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase, 1);
    }

    public static List<StructureComponent> generate(int bays, int depths, float widthMid, float widthSide, float depthStep,
                                                    float colHeight, float roofPitch, float eavesLen, float gableSetback,
                                                    float cornerLift, int dougongLv,
                                                    boolean showRoof, boolean showRafters, boolean showPurlins,
                                                    boolean showBeams, boolean showDougong, boolean showColumns, boolean showBase,
                                                    int stories) {
        
        List<StructureComponent> list = new ArrayList<>();
        float currentBaseY = 0.0f;

        for (int story = 0; story < stories; story++) {
            // Shrink the column net grid slightly for upper stories to prevent visual stiffness (收分)
            float shrink = story * 0.5f;
            float currentWidthMid = Math.max(2.0f, widthMid - shrink);
            float currentWidthSide = Math.max(1.5f, widthSide - shrink);
            float currentDepthStep = Math.max(1.5f, depthStep - shrink);
            
            BuildingConfig config = new BuildingConfig(
                bays, depths, currentWidthMid, currentWidthSide, currentDepthStep,
                colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv
            );
            config.baseY = currentBaseY;
            config.storyIndex = story;
            config.totalStories = stories;
            config.buildSkeletonNodes(); // Recompute skeleton node coordinates relative to this story's baseY

            if (story == 0 && showBase) buildBase(list, config);
            if (showColumns) buildColumns(list, config);
            if (showDougong) buildDougong(list, config);
            if (showBeams) buildBeamsAndTies(list, config);
            
            if (story == stories - 1) {
                // Top story has the main roof structure
                IRoofGenerator roofGenerator = new XieshanRoofGenerator();
                list.addAll(roofGenerator.generate(config, showRoof, showRafters, showPurlins));
            } else {
                // Mezzanine stories have flat waist eave decks
                buildFloorDeck(list, config);
            }

            if (showBeams || showPurlins) {
                buildPurlinSupports(list, config);
            }

            // Next story baseY starts above column top of this story plus floor beam stack depth
            currentBaseY += colHeight + 0.9f;
        }

        return list;
    }

    // 1. 台基与柱础 (石台基)
    private static void buildBase(List<StructureComponent> list, BuildingConfig config) {
        float plinthExtend = 1.0f;
        float baseW = config.W + plinthExtend * 2.0f;
        float baseD = config.D + plinthExtend * 2.0f;
        float baseH = 0.6f;

        // 大台基
        addBox(list, 0.0f, config.baseY + baseH / 2.0f, 0.0f, baseW, baseH, baseD, COLOR_STONE_BASE);

        // 柱础石 (每个立柱下方)
        for (float x : config.gridX) {
            for (float z : config.gridZ) {
                list.addAll(ColumnFactory.createPlinth(x, config.baseY + baseH, z, COLOR_STONE_BASE, config.caiSize));
            }
        }
    }

    // 2. 柱网 (朱红圆柱，做成方形方块柱风格)
    private static void buildColumns(List<StructureComponent> list, BuildingConfig config) {
        float baseH = 0.6f;
        int numX = config.gridX.length;
        int numZ = config.gridZ.length;

        for (int colIdxX = 0; colIdxX < numX; colIdxX++) {
            float x = config.gridX[colIdxX];
            for (int colIdxZ = 0; colIdxZ < numZ; colIdxZ++) {
                float z = config.gridZ[colIdxZ];
                boolean isEaveCol = (colIdxZ == 0 || colIdxZ == numZ - 1 || colIdxX == 0 || colIdxX == numX - 1);
                // 金柱/中柱略高于檐柱，体现抬梁升起
                float h = isEaveCol ? config.colHeight : config.colHeight + 0.3f;

                list.addAll(ColumnFactory.createColumn(x, config.baseY + baseH, z, h, COLOR_VERMILION, config.caiSize));
            }
        }
    }

    // 3. 梁架与额枋 (使用“材/分”比例制尺寸)
    private static void buildBeamsAndTies(List<StructureComponent> list, BuildingConfig config) {
        float baseH = 0.6f;
        float H_col = config.baseY + config.colHeight + baseH;
        float D = config.D;
        int numX = config.gridX.length;
        int numZ = config.gridZ.length;

        float fen = config.fenSize;

        // A. 额枋/金枋 (横向联系梁) — 沿着 X 轴平行
        for (int colIdxZ = 0; colIdxZ < numZ; colIdxZ++) {
            float z = config.gridZ[colIdxZ];
            boolean isEaveZ = (colIdxZ == 0 || colIdxZ == numZ - 1);
            float tieH = isEaveZ ? config.baseY + config.colHeight + baseH - 0.15f : config.baseY + config.colHeight + baseH + 0.15f;

            // 贯穿通长额枋 (18 Fen wide & deep)
            list.addAll(BeamFactory.createTie(0.0f, tieH, z, config.W, 18.0f * fen, 18.0f * fen, COLOR_VERMILION));

            // 枋两端的绿、蓝彩画条带装饰
            for (float x : config.gridX) {
                if (Math.abs(x) < config.W / 2.0f) {
                    list.addAll(BeamFactory.createTieDecoration(x, tieH, z, COLOR_JADE_GREEN, COLOR_INDIGO_BLUE));
                }
            }
        }

        // B. 抬梁式叠落梁架 — 沿着 Z 轴进深方向，每个内侧柱网位置排列
        float X_gable = config.W / 2.0f - config.gableSetback;
        for (float x : config.gridX) {
            // 只在山花墙以内架梁
            if (Math.abs(x) > X_gable + 0.1f) continue;

            float ds = config.depthStep;
            
            // 随梁枋 (12 Fen wide, 15 Fen deep)
            addBox(list, x, H_col - 11.0f * fen, 0.0f, 12.0f * fen, 15.0f * fen, D, COLOR_VERMILION);

            if (config.depths == 2) {
                // 底部主梁：五架梁 (跨越 Z = -ds 至 ds)
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.25f, -ds, ds, 16.0f * fen, 26.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                // 柁头瓜柱
                list.addAll(BeamFactory.createStrut(x, H_col + 0.425f, -ds / 2.0f, 0.35f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                list.addAll(BeamFactory.createStrut(x, H_col + 0.425f, ds / 2.0f, 0.35f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                // 上层：三架梁 (跨越 -ds/2 至 ds/2)
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.8f, -ds / 2.0f, ds / 2.0f, 13.5f * fen, 21.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                // 脊瓜柱
                list.addAll(BeamFactory.createStrut(x, H_col + 0.95f, 0.0f, 0.4f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));

            } else if (config.depths == 3) {
                // 七架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.25f, -1.5f * ds, 1.5f * ds, 18.0f * fen, 28.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                list.addAll(BeamFactory.createStrut(x, H_col + 0.44f, -0.5f * ds, 0.38f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                list.addAll(BeamFactory.createStrut(x, H_col + 0.44f, 0.5f * ds, 0.38f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                // 五架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.85f, -0.75f * ds, 0.75f * ds, 15.0f * fen, 22.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                list.addAll(BeamFactory.createStrut(x, H_col + 1.0f, -0.3f * ds, 0.3f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                list.addAll(BeamFactory.createStrut(x, H_col + 1.0f, 0.3f * ds, 0.3f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                // 三架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 1.3f, -0.35f * ds, 0.35f * ds, 12.0f * fen, 18.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                // 脊瓜柱
                list.addAll(BeamFactory.createStrut(x, H_col + 1.42f, 0.0f, 0.4f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));

            } else if (config.depths == 4) {
                // 九架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.25f, -2.0f * ds, 2.0f * ds, 20.0f * fen, 30.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                list.addAll(BeamFactory.createStrut(x, H_col + 0.45f, -ds, 0.4f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                list.addAll(BeamFactory.createStrut(x, H_col + 0.45f, ds, 0.4f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                // 七架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 0.85f, -ds, ds, 15.0f * fen, 24.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                list.addAll(BeamFactory.createStrut(x, H_col + 1.01f, -ds / 2.0f, 0.32f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                list.addAll(BeamFactory.createStrut(x, H_col + 1.01f, ds / 2.0f, 0.32f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                // 五架梁
                list.addAll(BeamFactory.createBeamSegment(x, H_col + 1.35f, -ds / 2.0f, ds / 2.0f, 12.0f * fen, 20.0f * fen, COLOR_NATURAL_WOOD, COLOR_VERMILION));
                // 脊瓜柱
                list.addAll(BeamFactory.createStrut(x, H_col + 1.48f, 0.0f, 0.42f, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
            }
        }
    }

    // 4. 斗拱层 (外部檐柱柱头放置)
    private static void buildDougong(List<StructureComponent> list, BuildingConfig config) {
        if (config.dougongLv == 0) return;
        int numX = config.gridX.length;
        int numZ = config.gridZ.length;
        float baseH = 0.6f;

        for (int colIdxX = 0; colIdxX < numX; colIdxX++) {
            float x = config.gridX[colIdxX];
            for (int colIdxZ = 0; colIdxZ < numZ; colIdxZ++) {
                float z = config.gridZ[colIdxZ];
                boolean isEaveCol = (colIdxZ == 0 || colIdxZ == numZ - 1 || colIdxX == 0 || colIdxX == numX - 1);
                if (isEaveCol) {
                    float y = config.colHeight + baseH;

                    boolean isLeft = (colIdxX == 0);
                    boolean isRight = (colIdxX == numX - 1);
                    boolean isBack = (colIdxZ == 0);
                    boolean isFront = (colIdxZ == numZ - 1);

                    // 确定角部或面部斗拱偏转角度
                    float rotY = 0.0f;
                    if (isFront && isRight) rotY = (float)Math.toRadians(45);
                    else if (isFront && isLeft) rotY = (float)Math.toRadians(-45);
                    else if (isBack && isRight) rotY = (float)Math.toRadians(135);
                    else if (isBack && isLeft) rotY = (float)Math.toRadians(-135);
                    else if (isFront) rotY = 0.0f;
                    else if (isBack) rotY = (float)Math.toRadians(180);
                    else if (isRight) rotY = (float)Math.toRadians(90);
                    else if (isLeft) rotY = (float)Math.toRadians(-90);

                    list.addAll(DougongFactory.createDougong(x, config.baseY + y, z, config.dougongLv, rotY, COLOR_VERMILION, COLOR_JADE_GREEN, COLOR_INDIGO_BLUE, COLOR_GOLD_PAINT, config.caiSize));
                }
            }
        }
    }

    // 腰檐/楼板平台 (Waist Eaves / floor deck)
    private static void buildFloorDeck(List<StructureComponent> list, BuildingConfig config) {
        float baseH = 0.6f;
        float y = config.baseY + config.colHeight + baseH;
        float deckW = config.W + config.eavesLen * 1.3f;
        float deckD = config.D + config.eavesLen * 1.3f;
        
        // 瓦灰色铺面 (腰檐瓦面效果)
        addBox(list, 0.0f, y + 0.04f, 0.0f, deckW, 0.08f, deckD, COLOR_SLATE_GREY);
        // 朱红木质支撑梁底
        addBox(list, 0.0f, y - 0.02f, 0.0f, deckW - 0.2f, 0.04f, deckD - 0.2f, COLOR_VERMILION);
    }

    // 5b. 檩梁垂直支撑小瓜柱
    private static void buildPurlinSupports(List<StructureComponent> list, BuildingConfig config) {
        float baseH = 0.6f;
        float H_col = config.baseY + config.colHeight + baseH;
        float ds = config.depthStep;
        float purlinRadius = 0.13f;
        float X_gable = config.X_gable;

        for (float z : config.gridZ) {
            float beamTop = getBeamTopAtZ(z, H_col, ds, config.depths);
            for (float x : config.gridX) {
                if (Math.abs(x) > X_gable + 0.1f) continue;
                float y_purlin_at_x = getRoofHeight(x, z, config) - 0.26f;
                float gap = y_purlin_at_x - purlinRadius - beamTop;
                if (gap > 0.15f) {
                    list.addAll(BeamFactory.createPurlinStrut(x, beamTop, z, gap, COLOR_GOLD_PAINT, COLOR_VERMILION, COLOR_NATURAL_WOOD, config.caiSize));
                }
            }
        }
    }

    private static float getBeamTopAtZ(float z, float H_col, float ds, int depths) {
        float absZ = Math.abs(z);
        if (depths == 2) {
            if (absZ < 0.1f) return H_col + 0.95f + 0.4f;
            if (absZ <= ds / 2.0f + 0.01f) return H_col + 0.8f + 0.28f;
            if (absZ <= ds + 0.01f) return H_col + 0.25f + 0.35f;
            return H_col;
        } else if (depths == 3) {
            if (absZ < 0.1f) return H_col + 1.42f + 0.4f;
            if (absZ <= 0.35f * ds + 0.01f) return H_col + 1.3f + 0.24f;
            if (absZ <= 0.75f * ds + 0.01f) return H_col + 0.85f + 0.30f;
            if (absZ <= 1.5f * ds + 0.01f) return H_col + 0.25f + 0.38f;
            return H_col;
        } else { // depths == 4
            if (absZ < 0.1f) return H_col + 1.48f + 0.42f;
            if (absZ <= ds / 2.0f + 0.01f) return H_col + 1.35f + 0.26f;
            if (absZ <= ds + 0.01f) return H_col + 0.85f + 0.32f;
            if (absZ <= 2.0f * ds + 0.01f) return H_col + 0.25f + 0.40f;
            return H_col;
        }
    }


    // 基础三维高度数学计算方法集
    public static float getFrontRoofHeight(float x, float z, BuildingConfig config) {
        float W_half = config.W / 2.0f;
        float D_half = config.D / 2.0f;
        float W_roof_half = W_half + config.eavesLen;
        float D_roof_half = D_half + config.eavesLen;

        float H_eaves = config.colHeight + 0.6f + config.baseY;
        float H_ridge = config.colHeight + 0.6f + config.baseY + D_roof_half * config.roofPitch;

        float u_z = Math.abs(z) / D_roof_half;
        float clamped_uz = Math.min(1.0f, Math.max(0.0f, u_z));
        float y = H_eaves + (H_ridge - H_eaves) * (float)Math.pow(1.0f - clamped_uz, 1.5f);

        // 翼角起翘 (Corner Lift)
        float abs_x = Math.abs(x);
        float abs_z = Math.abs(z);
        float lift_start_x = W_half * 0.4f;
        float lift_start_z = D_half * 0.4f;
        if (abs_x > lift_start_x && abs_z > lift_start_z) {
            float lift_factor_x = (abs_x - lift_start_x) / (W_roof_half - lift_start_x);
            float lift_factor_z = (abs_z - lift_start_z) / (D_roof_half - lift_start_z);
            float lift_mult = (float)Math.pow(Math.max(0.0f, lift_factor_x), 2.0f) * (float)Math.pow(Math.max(0.0f, lift_factor_z), 2.0f);
            y += config.cornerLift * lift_mult;
        }
        return y;
    }

    public static float getSideRoofHeight(float x, float z, BuildingConfig config) {
        float W_half = config.W / 2.0f;
        float D_half = config.D / 2.0f;
        float X_gable = config.X_gable;
        float W_roof_half = W_half + config.eavesLen;
        float D_roof_half = D_half + config.eavesLen;
        float Z_gable_limit = config.Z_gable_limit;

        float H_eaves = config.colHeight + 0.6f + config.baseY;
        float H_ridge = config.colHeight + 0.6f + config.baseY + D_roof_half * config.roofPitch;

        float u_z = Z_gable_limit / D_roof_half;
        float yG = H_eaves + (H_ridge - H_eaves) * (float)Math.pow(1.0f - u_z, 1.5f);

        float total_side_span = W_roof_half - X_gable;
        float dist_from_edge = W_roof_half - Math.abs(x);
        float u_x = Math.max(0.0f, Math.min(1.0f, dist_from_edge / total_side_span));
        float y = H_eaves + (yG - H_eaves) * (float)Math.pow(u_x, 1.25f);

        // 翼角起翘
        float abs_x = Math.abs(x);
        float abs_z = Math.abs(z);
        float lift_start_x = W_half * 0.4f;
        float lift_start_z = D_half * 0.4f;
        if (abs_x > lift_start_x && abs_z > lift_start_z) {
            float lift_factor_x = (abs_x - lift_start_x) / (W_roof_half - lift_start_x);
            float lift_factor_z = (abs_z - lift_start_z) / (D_roof_half - lift_start_z);
            float lift_mult = (float)Math.pow(Math.max(0.0f, lift_factor_x), 2.0f) * (float)Math.pow(Math.max(0.0f, lift_factor_z), 2.0f);
            y += config.cornerLift * lift_mult;
        }
        return y;
    }

    public static float getRoofHeight(float x, float z, BuildingConfig config) {
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;
        float abs_x = Math.abs(x);
        float abs_z = Math.abs(z);

        if (abs_x > X_gable) {
            if (abs_z > Z_gable_limit) {
                float W_roof_half = config.W / 2.0f + config.eavesLen;
                float D_roof_half = config.D / 2.0f + config.eavesLen;
                float dx = W_roof_half - X_gable;
                float dz = D_roof_half - Z_gable_limit;
                float tx = (abs_x - X_gable) / dx;
                float tz = (abs_z - Z_gable_limit) / dz;
                if (tz > tx) {
                    return getFrontRoofHeight(x, z, config);
                } else {
                    return getSideRoofHeight(x, z, config);
                }
            } else {
                float overhang = 0.6f;
                if (abs_x <= X_gable + overhang) {
                    return getFrontRoofHeight(x, z, config);
                } else {
                    return getSideRoofHeight(x, z, config);
                }
            }
        } else {
            return getFrontRoofHeight(x, z, config);
        }
    }

    public static boolean isFrontSlope(float x, float z, BuildingConfig config) {
        float abs_x = Math.abs(x);
        float abs_z = Math.abs(z);
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        if (abs_x > X_gable) {
            if (abs_z > Z_gable_limit) {
                float W_roof_half = config.W / 2.0f + config.eavesLen;
                float D_roof_half = config.D / 2.0f + config.eavesLen;
                float dx = W_roof_half - X_gable;
                float dz = D_roof_half - Z_gable_limit;
                float tx = (abs_x - X_gable) / dx;
                float tz = (abs_z - Z_gable_limit) / dz;
                return tz > tx;
            } else {
                float overhang = 0.6f;
                return abs_x <= X_gable + overhang;
            }
        }
        return true;
    }

    public static float getDiagZLimit(float x, BuildingConfig config) {
        float abs_x = Math.abs(x);
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;
        if (abs_x <= X_gable) return Z_gable_limit;

        float W_roof_half = config.W / 2.0f + config.eavesLen;
        float D_roof_half = config.D / 2.0f + config.eavesLen;
        float dx = W_roof_half - X_gable;
        float dz = D_roof_half - Z_gable_limit;

        return Z_gable_limit + (abs_x - X_gable) * (dz / dx);
    }

    public static float getDiagXLimit(float z, BuildingConfig config) {
        float abs_z = Math.abs(z);
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;
        if (abs_z <= Z_gable_limit) return X_gable;

        float W_roof_half = config.W / 2.0f + config.eavesLen;
        float D_roof_half = config.D / 2.0f + config.eavesLen;
        float dx = W_roof_half - X_gable;
        float dz = D_roof_half - Z_gable_limit;

        return X_gable + (abs_z - Z_gable_limit) * (dx / dz);
    }

    public static boolean shouldDrawFrontSegment(float x, float zMid, BuildingConfig config) {
        float abs_x = Math.abs(x);
        float abs_z = Math.abs(zMid);
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        if (abs_x > X_gable && abs_z > Z_gable_limit) {
            float z_lim = getDiagZLimit(x, config);
            if (abs_z < z_lim + 0.15f) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldDrawSideSegment(float z, float xMid, BuildingConfig config) {
        float abs_x = Math.abs(xMid);
        float abs_z = Math.abs(z);
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        if (abs_x > X_gable && abs_z > Z_gable_limit) {
            float x_lim = getDiagXLimit(z, config);
            if (abs_x < x_lim + 0.15f) {
                return false;
            }
        }
        return true;
    }

    // 基础三维组件构建与变换辅助方法集
    private static void addBox(List<StructureComponent> list, float x, float y, float z, float sx, float sy, float sz, int color) {
        list.add(new StructureComponent(new Vector3f(x, y, z), new Vector3f(sx, sy, sz), new Quaternionf(), "", 0, 0, 16, 16, color));
    }
}
