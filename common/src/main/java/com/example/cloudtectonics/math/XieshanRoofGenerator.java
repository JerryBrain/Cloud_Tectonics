package com.example.cloudtectonics.math;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

import static com.example.cloudtectonics.math.ParametricBuildingGenerator.*;

/**
 * 歇山顶（折面、起翘、正脊、垂脊、戗脊、博风板）几何生成器
 */
public class XieshanRoofGenerator implements IRoofGenerator {

    @Override
    public List<StructureComponent> generate(BuildingConfig config, boolean showRoof, boolean showRafters, boolean showPurlins) {
        List<StructureComponent> list = new ArrayList<>();

        if (showPurlins) {
            buildPurlins(list, config);
        }

        if (showRafters) {
            buildRafters(list, config);
        }

        if (showRoof) {
            buildRoof(list, config);
            buildBofengAndRuyi(list, config);
        }

        return list;
    }

    // 5. 檩条 (平行于 W 横向拉结，以及山面横向铺设)
    private void buildPurlins(List<StructureComponent> list, BuildingConfig config) {
        float purlinRadius = 0.13f;
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        // A. 前后檐檩与金檩 (横向檩条) — 沿着 X 轴平行
        for (float z : config.gridZ) {
            float y_purlin = getRoofHeight(0.0f, z, config) - 0.26f;
            float pLen;
            boolean isShort = false;

            float abs_z = Math.abs(z);
            if (abs_z < Z_gable_limit) {
                pLen = X_gable * 2.0f + 1.2f;
                isShort = true;
            } else if (abs_z >= config.D / 2.0f - 0.01f) {
                pLen = config.W + 0.4f;
            } else {
                float x_lim = getDiagXLimit(z, config);
                pLen = x_lim * 2.0f;
            }

            addBox(list, 0.0f, y_purlin, z, pLen, purlinRadius * 2.0f, purlinRadius * 2.0f, COLOR_NATURAL_WOOD);

            // 承托替木
            for (float x : config.gridX) {
                if (!isShort || Math.abs(x) <= X_gable) {
                    addBox(list, x, y_purlin - purlinRadius - 0.04f, z, 0.8f, 0.08f, 0.18f, COLOR_VERMILION);
                }
            }
        }

        // B. 山面金桁与檐檩 (侧向檩条) — 沿着 Z 轴进深方向
        // B.1. 山面外侧檐檩
        float[] sideXs = {-config.W / 2.0f, config.W / 2.0f};
        for (float x : sideXs) {
            float pLen = config.D + 0.4f;
            int steps = 10;
            float zStep = pLen / steps;
            float startZ = -config.D / 2.0f - 0.2f;
            for (int i = 0; i < steps; i++) {
                float z1 = startZ + i * zStep;
                float z2 = startZ + (i + 1) * zStep;
                float y1 = getRoofHeight(x, z1, config) - 0.26f;
                float y2 = getRoofHeight(x, z2, config) - 0.26f;

                addRotatedBox(list, new Vector3f(x, y1, z1), new Vector3f(x, y2, z2), purlinRadius * 2.0f, purlinRadius * 2.0f, COLOR_NATURAL_WOOD);
            }
        }

        // B.2. 踩步金 (山面斜跨桁梁) at x = ±X_gable
        float[] cbjXs = {-X_gable, X_gable};
        for (float x : cbjXs) {
            int cbj_steps = 8;
            float zSpan = Z_gable_limit;
            float yG = getFrontRoofHeight(x, Z_gable_limit, config);
            float y_cbj = yG - 0.32f;
            for (int i = 0; i < cbj_steps; i++) {
                float z1 = -zSpan + (2.0f * zSpan / cbj_steps) * i;
                float z2 = -zSpan + (2.0f * zSpan / cbj_steps) * (i + 1);
                addRotatedBox(list, new Vector3f(x, y_cbj, z1), new Vector3f(x, y_cbj, z2), 0.24f, 0.28f, COLOR_NATURAL_WOOD);
            }

            // 端头替木块
            addBox(list, x, y_cbj, -zSpan, 0.28f, 0.12f, 0.20f, COLOR_VERMILION);
            addBox(list, x, y_cbj, zSpan, 0.28f, 0.12f, 0.20f, COLOR_VERMILION);
        }

        // B.3. 侧向中间金桁
        float sideSpan = config.W / 2.0f - X_gable;
        int numIntPurlins = Math.max(1, (int)Math.floor(sideSpan / 1.8f));
        for (int p = 1; p <= numIntPurlins; p++) {
            float t = (float)p / (numIntPurlins + 1);
            float x_pos = X_gable + t * sideSpan;
            float[] sxs = {-1, 1};
            for (float sx : sxs) {
                float x = sx * x_pos;
                float zRange = getDiagZLimit(x, config);
                int seg_steps = 8;
                for (int i = 0; i < seg_steps; i++) {
                    float z1 = -zRange + (2.0f * zRange / seg_steps) * i;
                    float z2 = -zRange + (2.0f * zRange / seg_steps) * (i + 1);
                    float y1 = getSideRoofHeight(x, z1, config) - 0.26f;
                    float y2 = getSideRoofHeight(x, z2, config) - 0.26f;

                    addRotatedBox(list, new Vector3f(x, y1, z1), new Vector3f(x, y2, z2), purlinRadius * 1.8f, purlinRadius * 1.8f, COLOR_NATURAL_WOOD);
                }
            }
        }
    }

    // 6. 双向布椽及翼角放射椽
    private void buildRafters(List<StructureComponent> list, BuildingConfig config) {
        float rafterSpacing = 0.28f;
        float rafterRad = 0.05f;
        float W_roof_half = config.W / 2.0f + config.eavesLen;
        float D_roof_half = config.D / 2.0f + config.eavesLen;
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        // A. 前后坡檐椽 — 沿着 Z 轴拉伸
        float mainBodyW = X_gable * 2.0f;
        int numRaftersX = (int)Math.floor(mainBodyW / rafterSpacing);
        float startX = -X_gable + (mainBodyW - (numRaftersX * rafterSpacing)) / 2.0f;
        for (int i = 0; i <= numRaftersX; i++) {
            float x = startX + i * rafterSpacing;
            createRafterLine(list, x, 0.0f, D_roof_half, rafterRad, config);
            createRafterLine(list, x, 0.0f, -D_roof_half, rafterRad, config);
        }

        // B. 山面檐椽 — 沿着 X 轴拉伸
        float sideBodyD = Z_gable_limit * 2.0f;
        int numRaftersZ = (int)Math.floor(sideBodyD / rafterSpacing);
        float startZ = -Z_gable_limit + (sideBodyD - (numRaftersZ * rafterSpacing)) / 2.0f;
        for (int i = 0; i <= numRaftersZ; i++) {
            float z = startZ + i * rafterSpacing;
            createSideRafterLine(list, z, X_gable, W_roof_half, rafterRad, config);
            createSideRafterLine(list, z, -X_gable, -W_roof_half, rafterRad, config);
        }

        // C. 四角翼角放射椽
        createWingCornerRafters(list, 1, 1, rafterRad, config);
        createWingCornerRafters(list, 1, -1, rafterRad, config);
        createWingCornerRafters(list, -1, 1, rafterRad, config);
        createWingCornerRafters(list, -1, -1, rafterRad, config);
    }

    private void createRafterLine(List<StructureComponent> list, float x, float zStart, float zEnd, float radius, BuildingConfig config) {
        float totalDist = Math.abs(zEnd - zStart);
        if (totalDist < 0.05f) return;
        int steps = Math.max(2, (int)Math.floor(totalDist * 2.5f));
        float zStep = (zEnd - zStart) / steps;
        for (int i = 0; i < steps; i++) {
            float z1 = zStart + i * zStep;
            float z2 = zStart + (i + 1) * zStep;
            float y1 = getRoofHeight(x, z1, config) - 0.12f;
            float y2 = getRoofHeight(x, z2, config) - 0.12f;
            addRotatedBox(list, new Vector3f(x, y1, z1), new Vector3f(x, y2, z2), radius * 2.0f, radius * 2.0f, COLOR_NATURAL_WOOD);
        }
    }

    private void createSideRafterLine(List<StructureComponent> list, float z, float xStart, float xEnd, float radius, BuildingConfig config) {
        float totalDist = Math.abs(xEnd - xStart);
        if (totalDist < 0.05f) return;
        int steps = Math.max(2, (int)Math.floor(totalDist * 2.5f));
        float xStep = (xEnd - xStart) / steps;
        for (int i = 0; i < steps; i++) {
            float x1 = xStart + i * xStep;
            float x2 = xStart + (i + 1) * xStep;
            float y1 = getSideRoofHeight(x1, z, config) - 0.12f;
            float y2 = getSideRoofHeight(x2, z, config) - 0.12f;
            addRotatedBox(list, new Vector3f(x1, y1, z), new Vector3f(x2, y2, z), radius * 2.0f, radius * 2.0f, COLOR_NATURAL_WOOD);
        }
    }

    private void createWingCornerRafters(List<StructureComponent> list, int sx, int sz, float rafterRad, BuildingConfig config) {
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;
        float W_roof_half = config.W / 2.0f + config.eavesLen;
        float D_roof_half = config.D / 2.0f + config.eavesLen;

        float fx = sx * X_gable;
        float fz = sz * Z_gable_limit;

        float cb_dx = W_roof_half - X_gable;
        float cb_dz = D_roof_half - Z_gable_limit;
        float cb_len = (float)Math.sqrt(cb_dx * cb_dx + cb_dz * cb_dz);

        float beamHalfWidth = 0.14f;

        // Front eave fan rafters
        float frontEaveLen = W_roof_half - X_gable;
        int numFrontFan = Math.max(4, (int)Math.floor(frontEaveLen / 0.26f));
        for (int i = 1; i < numFrontFan; i++) {
            float t = (float)i / numFrontFan;
            float ex = sx * (X_gable + t * frontEaveLen);
            float ez = sz * D_roof_half;

            float dx = ex - fx;
            float dz = ez - fz;
            float len = (float)Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1f) continue;

            float cross = Math.abs(dx * sz * cb_dz - dz * sx * cb_dx) / (len * cb_len);
            float angleToBeam = (float)Math.asin(Math.min(1.0f, cross));

            float startOff = beamHalfWidth / (float)Math.max(0.08f, Math.sin(Math.max(0.08f, angleToBeam)));
            startOff = Math.min(startOff, len * 0.82f);
            startOff = Math.max(startOff, 0.25f);

            float rx = fx + (dx / len) * startOff;
            float rz = fz + (dz / len) * startOff;

            float innerRad = rafterRad * 0.55f;
            createFanRafterSegments(list, rx, rz, ex, ez, innerRad, rafterRad, config);
        }

        // Side eave fan rafters
        float sideEaveLen = D_roof_half - Z_gable_limit;
        int numSideFan = Math.max(4, (int)Math.floor(sideEaveLen / 0.26f));
        for (int i = 1; i < numSideFan; i++) {
            float t = (float)i / numSideFan;
            float ex = sx * W_roof_half;
            float ez = sz * (Z_gable_limit + t * sideEaveLen);

            float dx = ex - fx;
            float dz = ez - fz;
            float len = (float)Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1f) continue;

            float cross = Math.abs(dx * sz * cb_dz - dz * sx * cb_dx) / (len * cb_len);
            float angleToBeam = (float)Math.asin(Math.min(1.0f, cross));

            float startOff = beamHalfWidth / (float)Math.max(0.08f, Math.sin(Math.max(0.08f, angleToBeam)));
            startOff = Math.min(startOff, len * 0.82f);
            startOff = Math.max(startOff, 0.25f);

            float rx = fx + (dx / len) * startOff;
            float rz = fz + (dz / len) * startOff;

            float innerRad = rafterRad * 0.55f;
            createFanRafterSegments(list, rx, rz, ex, ez, innerRad, rafterRad, config);
        }
    }

    private void createFanRafterSegments(List<StructureComponent> list, float xStart, float zStart, float xEnd, float zEnd, float radInner, float radOuter, BuildingConfig config) {
        float dx = xEnd - xStart;
        float dz = zEnd - zStart;
        float totalDist2D = (float)Math.sqrt(dx * dx + dz * dz);
        if (totalDist2D < 0.05f) return;

        int steps = Math.max(2, (int)Math.floor(totalDist2D * 2.5f));
        for (int i = 0; i < steps; i++) {
            float t1 = (float)i / steps;
            float t2 = (float)(i + 1) / steps;

            float x1 = xStart + dx * t1;
            float z1 = zStart + dz * t1;
            float x2 = xStart + dx * t2;
            float z2 = zStart + dz * t2;

            float y1 = getRoofHeight(x1, z1, config) - 0.12f;
            float y2 = getRoofHeight(x2, z2, config) - 0.12f;

            float tMid = (t1 + t2) / 2.0f;
            float rad = radInner + (radOuter - radInner) * tMid;

            addRotatedBox(list, new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), rad * 2.0f, rad * 2.0f, COLOR_NATURAL_WOOD);
        }
    }

    // 7. 瓦面、脊梁、双层角梁
    private void buildRoof(List<StructureComponent> list, BuildingConfig config) {
        float W_roof_half = config.W / 2.0f + config.eavesLen;
        float D_roof_half = config.D / 2.0f + config.eavesLen;
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        float tileSpacing = 0.22f;

        // 1. 前后坡瓦垄 (平行于 Z)
        int numTilesX = (int)Math.floor((W_roof_half * 2.0f) / tileSpacing);
        float startX = -W_roof_half + (W_roof_half * 2.0f - (numTilesX * tileSpacing)) / 2.0f;

        for (int i = 0; i <= numTilesX; i++) {
            float x = startX + i * tileSpacing;
            createTileRidge(list, x, 0.0f, D_roof_half, config, false);
            createTileRidge(list, x, 0.0f, -D_roof_half, config, false);

            if (i < numTilesX) {
                float x_concave = x + tileSpacing / 2.0f;
                createTileRidge(list, x_concave, 0.0f, D_roof_half, config, true);
                createTileRidge(list, x_concave, 0.0f, -D_roof_half, config, true);
            }
        }

        // 2. 山面瓦垄 (平行于 X)
        int numTilesZ = (int)Math.floor((D_roof_half * 2.0f) / tileSpacing);
        float startZ = -D_roof_half + (D_roof_half * 2.0f - (numTilesZ * tileSpacing)) / 2.0f;

        for (int i = 0; i <= numTilesZ; i++) {
            float z = startZ + i * tileSpacing;
            createSideTileRidge(list, z, X_gable, W_roof_half, config, false);
            createSideTileRidge(list, z, -X_gable, -W_roof_half, config, false);

            if (i < numTilesZ) {
                float z_concave = z + tileSpacing / 2.0f;
                createSideTileRidge(list, z_concave, X_gable, W_roof_half, config, true);
                createSideTileRidge(list, z_concave, -X_gable, -W_roof_half, config, true);
            }
        }

        // 3. 山花板 (左右两侧)
        float[] gableXs = {-X_gable, X_gable};
        for (float x : gableXs) {
            float y_base = config.colHeight + 0.6f + 0.35f;
            float h_max = getRoofHeight(x, 0.0f, config) - 0.1f;
            int divisions = 12;
            float zSpan = Z_gable_limit;
            float zStep = zSpan / divisions;
            for (int j = 0; j < divisions; j++) {
                float z1 = j * zStep;
                float z2 = (j + 1) * zStep;
                float zMid = (z1 + z2) / 2.0f;
                float yRoof = getRoofHeight(x, zMid, config) - 0.1f;
                float h = yRoof - y_base;
                if (h > 0.0f) {
                    addBox(list, x, y_base + h / 2.0f, zMid, 0.08f, h, zStep, COLOR_GABLE_WALL);
                    addBox(list, x, y_base + h / 2.0f, -zMid, 0.08f, h, zStep, COLOR_GABLE_WALL);
                }
            }
            // 山花装饰徽章
            addBox(list, x + (x > 0 ? 0.05f : -0.05f), y_base + (h_max - y_base) * 0.4f, 0.0f, 0.04f, 0.4f, 0.4f, COLOR_GOLD_PAINT);
        }

        // 4. 正脊
        float H_ridge = getRoofHeight(0.0f, 0.0f, config);
        float ridgeLen = X_gable * 2.0f;
        float ridgeRad = 0.22f;
        addBox(list, 0.0f, H_ridge + 0.16f, 0.0f, ridgeLen, ridgeRad * 2.0f, ridgeRad * 2.0f, COLOR_GLAZED_GOLD_TILE);

        // 5. 垂脊、戗脊、老角梁与仔角梁
        int[][] corners = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] c : corners) {
            int sx = c[0];
            int sz = c[1];
            float xG = sx * X_gable;
            float zG = sz * Z_gable_limit;
            float yG = getRoofHeight(xG, zG, config);

            float xC = sx * W_roof_half;
            float zC = sz * D_roof_half;
            float yC = getRoofHeight(xC, zC, config);

            // A. 老角梁
            addRotatedBox(list, new Vector3f(xG, yG - 0.30f, zG), new Vector3f(xC, yC - 0.30f, zC), 0.20f, 0.24f, COLOR_NATURAL_WOOD);

            // B. 仔角梁 (倾斜反宇起翘)
            Vector3f P0 = new Vector3f(xG, yG - 0.10f, zG);
            Vector3f P2 = new Vector3f(xC, yC + config.cornerLift * 0.8f, zC);
            float midX = (P0.x + P2.x) / 2.0f;
            float midZ = (P0.z + P2.z) / 2.0f;
            Vector3f P1 = new Vector3f(midX, Math.min(P0.y, P2.y) - config.cornerLift * 0.5f, midZ);

            int youngSteps = 14;
            for (int i = 0; i < youngSteps; i++) {
                float t1 = (float)i / youngSteps;
                float t2 = (float)(i + 1) / youngSteps;

                float bx1 = (1.0f - t1) * (1.0f - t1) * P0.x + 2.0f * (1.0f - t1) * t1 * P1.x + t1 * t1 * P2.x;
                float by1 = (1.0f - t1) * (1.0f - t1) * P0.y + 2.0f * (1.0f - t1) * t1 * P1.y + t1 * t1 * P2.y;
                float bz1 = (1.0f - t1) * (1.0f - t1) * P0.z + 2.0f * (1.0f - t1) * t1 * P1.z + t1 * t1 * P2.z;

                float bx2 = (1.0f - t2) * (1.0f - t2) * P0.x + 2.0f * (1.0f - t2) * t2 * P1.x + t2 * t2 * P2.x;
                float by2 = (1.0f - t2) * (1.0f - t2) * P0.y + 2.0f * (1.0f - t2) * t2 * P1.y + t2 * t2 * P2.y;
                float bz2 = (1.0f - t2) * (1.0f - t2) * P0.z + 2.0f * (1.0f - t2) * t2 * P1.z + t2 * t2 * P2.z;

                float tMid = (t1 + t2) / 2.0f;
                float beamW = 0.14f + tMid * 0.06f;

                addRotatedBox(list, new Vector3f(bx1, by1, bz1), new Vector3f(bx2, by2, bz2), beamW, 0.16f, COLOR_YOUNG_CORNER_BEAM);
            }

            // C. 垂脊 (正脊两端悬空垂下)
            createCurvedRidgeLine(list, new Vector3f(xG, H_ridge + 0.16f, 0.0f), new Vector3f(xG, yG + 0.16f, zG), 0.15f, COLOR_GLAZED_GOLD_TILE, config);

            // D. 戗脊 (檐角斜脊)
            createCurvedRidgeLine(list, new Vector3f(xG, yG + 0.16f, zG), new Vector3f(xC, yC + 0.16f, zC), 0.15f, COLOR_GLAZED_GOLD_TILE, config);

            // E. 正脊吻兽与戗兽
            if (sz == 1) {
                addBox(list, xG + (sx > 0 ? -0.1f : 0.1f), H_ridge + 0.3f, 0.0f, 0.35f, 0.45f, 0.25f, COLOR_GOLD_PAINT);
            }
            float cbX = xC - sx * 0.3f;
            float cbZ = zC - sz * 0.3f;
            float cbY = getRoofHeight(cbX, cbZ, config) + 0.12f;
            addBox(list, cbX, cbY, cbZ, 0.18f, 0.22f, 0.18f, COLOR_GOLD_PAINT);
        }
    }

    private void createTileRidge(List<StructureComponent> list, float x, float zStart, float zEnd, BuildingConfig config, boolean isConcave) {
        float totalDist = Math.abs(zEnd - zStart);
        if (totalDist < 0.05f) return;
        int steps = Math.max(2, (int)Math.floor(totalDist * 2.5f));
        float zStep = (zEnd - zStart) / steps;

        float rad = isConcave ? 0.10f : 0.08f;
        float yOffset = isConcave ? -0.02f : 0.0f;

        for (int i = 0; i < steps; i++) {
            float z1 = zStart + i * zStep;
            float z2 = zStart + (i + 1) * zStep;
            float zMid = (z1 + z2) / 2.0f;

            if (!isFrontSlope(x, zMid, config)) continue;
            if (!shouldDrawFrontSegment(x, zMid, config)) continue;

            float y1 = getRoofHeight(x, z1, config) + yOffset;
            float y2 = getRoofHeight(x, z2, config) + yOffset;

            addTileSegment(list, new Vector3f(x, y1, z1), new Vector3f(x, y2, z2), rad * 2.0f, rad, COLOR_SLATE_GREY);

            // 瓦当滴水饰件
            if (!isConcave && i == steps - 1) {
                Vector3f p2 = new Vector3f(x, y2, z2);
                Vector3f dir = new Vector3f(p2).sub(x, y1, z1).normalize();
                Vector3f wPos = new Vector3f(p2).add(new Vector3f(dir).mul(0.02f));
                addTileSegment(list, wPos, new Vector3f(wPos).add(new Vector3f(dir).mul(0.03f)), rad * 2.2f, rad * 1.1f, COLOR_GOLD_PAINT);
            }
        }
    }

    private void createSideTileRidge(List<StructureComponent> list, float z, float xStart, float xEnd, BuildingConfig config, boolean isConcave) {
        float totalDist = Math.abs(xEnd - xStart);
        if (totalDist < 0.05f) return;
        int steps = Math.max(2, (int)Math.floor(totalDist * 2.5f));
        float xStep = (xEnd - xStart) / steps;

        float rad = isConcave ? 0.10f : 0.08f;
        float yOffset = isConcave ? -0.02f : 0.0f;

        for (int i = 0; i < steps; i++) {
            float x1 = xStart + i * xStep;
            float x2 = xStart + (i + 1) * xStep;
            float xMid = (x1 + x2) / 2.0f;

            if (isFrontSlope(xMid, z, config)) continue;
            if (!shouldDrawSideSegment(z, xMid, config)) continue;

            float y1 = getSideRoofHeight(x1, z, config) + yOffset;
            float y2 = getSideRoofHeight(x2, z, config) + yOffset;

            addTileSegment(list, new Vector3f(x1, y1, z), new Vector3f(x2, y2, z), rad * 2.0f, rad, COLOR_SLATE_GREY);

            // 瓦当滴水
            if (!isConcave && i == steps - 1) {
                Vector3f p2 = new Vector3f(x2, y2, z);
                Vector3f dir = new Vector3f(p2).sub(x1, y1, z).normalize();
                Vector3f wPos = new Vector3f(p2).add(new Vector3f(dir).mul(0.02f));
                addTileSegment(list, wPos, new Vector3f(wPos).add(new Vector3f(dir).mul(0.03f)), rad * 2.2f, rad * 1.1f, COLOR_GOLD_PAINT);
            }
        }
    }

    private void createCurvedRidgeLine(List<StructureComponent> list, Vector3f pStart, Vector3f pEnd, float radius, int color, BuildingConfig config) {
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float t1 = (float)i / steps;
            float t2 = (float)(i + 1) / steps;

            float x1 = pStart.x + (pEnd.x - pStart.x) * t1;
            float z1 = pStart.z + (pEnd.z - pStart.z) * t1;
            float y1 = getRoofHeight(x1, z1, config) + 0.16f;

            float x2 = pStart.x + (pEnd.x - pStart.x) * t2;
            float z2 = pStart.z + (pEnd.z - pStart.z) * t2;
            float y2 = getRoofHeight(x2, z2, config) + 0.16f;

            addRotatedBox(list, new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), radius * 2.0f, radius * 2.0f, color);
        }
    }

    // 8. 悬山博风板与荣鱼挂件
    private void buildBofengAndRuyi(List<StructureComponent> list, BuildingConfig config) {
        float X_gable = config.X_gable;
        float Z_gable_limit = config.Z_gable_limit;

        float overhang = 0.55f;
        float x_edge = X_gable + overhang;

        float[] sxs = {-1, 1};
        for (float sx : sxs) {
            float x = sx * x_edge;
            int steps = 12;
            float zSpan = Z_gable_limit;

            // 前半檐博风板
            for (int i = 0; i < steps; i++) {
                float z1 = (zSpan / steps) * i;
                float z2 = (zSpan / steps) * (i + 1);
                float y1 = getFrontRoofHeight(x_edge, z1, config) - 0.06f;
                float y2 = getFrontRoofHeight(x_edge, z2, config) - 0.06f;
                drawBofengSegment(list, x, y1, z1, y2, z2, sx);
            }

            // 后半檐博风板
            for (int i = 0; i < steps; i++) {
                float z1 = -(zSpan / steps) * i;
                float z2 = -(zSpan / steps) * (i + 1);
                float y1 = getFrontRoofHeight(x_edge, z1, config) - 0.06f;
                float y2 = getFrontRoofHeight(x_edge, z2, config) - 0.06f;
                drawBofengSegment(list, x, y1, z1, y2, z2, sx);
            }

            // 荣鱼挂件 (Ruyi Pendant)
            float yTop = getFrontRoofHeight(x_edge, 0.0f, config) - 0.08f;
            addBox(list, x, yTop - 0.24f, 0.0f, 0.04f, 0.48f, 0.16f, COLOR_VERMILION);
            addBox(list, x + (sx > 0 ? 0.01f : -0.01f), yTop - 0.24f, 0.0f, 0.05f, 0.18f, 0.10f, COLOR_GOLD_PAINT);
            addBox(list, x + (sx > 0 ? 0.01f : -0.01f), yTop - 0.10f, 0.0f, 0.05f, 0.08f, 0.08f, COLOR_GOLD_PAINT);
        }
    }

    private void drawBofengSegment(List<StructureComponent> list, float x, float y1, float z1, float y2, float z2, float sx) {
        Vector3f p1 = new Vector3f(x, y1, z1);
        Vector3f p2 = new Vector3f(x, y2, z2);
        float dist = p1.distance(p2);
        if (dist < 0.001f) return;

        Vector3f dir = new Vector3f(p2).sub(p1).normalize();
        float boardThickness = 0.04f;
        float boardHeight = 0.22f;

        addRotatedBox(list, p1, p2, boardThickness, boardHeight, COLOR_VERMILION);

        // 描金镶边
        Vector3f center = new Vector3f(p1).add(new Vector3f(dir).mul(dist / 2.0f));
        Vector3f trimPos = new Vector3f(center);
        trimPos.x += sx * (boardThickness / 2.0f + 0.006f);
        trimPos.y += boardHeight / 2.0f - 0.02f;
        list.add(new StructureComponent(trimPos, new Vector3f(0.01f, 0.03f, dist), new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir), "", 0, 0, 16, 16, COLOR_GOLD_PAINT));
    }

    // Helper drawing methods
    private void addBox(List<StructureComponent> list, float x, float y, float z, float sx, float sy, float sz, int color) {
        list.add(new StructureComponent(new Vector3f(x, y, z), new Vector3f(sx, sy, sz), new Quaternionf(), "", 0, 0, 16, 16, color));
    }

    private void addRotatedBox(List<StructureComponent> list, Vector3f p1, Vector3f p2, float width, float height, int color) {
        float dist = p1.distance(p2);
        if (dist < 0.001f) return;
        Vector3f dir = new Vector3f(p2).sub(p1).normalize();
        Vector3f center = new Vector3f(p1).add(new Vector3f(dir).mul(dist / 2.0f));
        Quaternionf rot = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir);
        list.add(new StructureComponent(center, new Vector3f(width, height, dist), rot, "", 0, 0, 16, 16, color));
    }

    private void addTileSegment(List<StructureComponent> list, Vector3f p1, Vector3f p2, float width, float height, int color) {
        float dist = p1.distance(p2);
        if (dist < 0.001f) return;
        Vector3f vZ = new Vector3f(p2).sub(p1).normalize();
        Vector3f vX = new Vector3f(0, 1, 0).cross(vZ).normalize();
        if (vX.lengthSquared() < 0.001f) {
            vX.set(1, 0, 0);
        }
        Vector3f vY = new Vector3f(vZ).cross(vX).normalize();

        Matrix3f basis = new Matrix3f();
        basis.setColumn(0, vX);
        basis.setColumn(1, vY);
        basis.setColumn(2, vZ);
        Quaternionf rot = new Quaternionf();
        basis.getNormalizedRotation(rot);

        Vector3f center = new Vector3f(p1).add(new Vector3f(vZ).mul(dist / 2.0f));
        list.add(new StructureComponent(center, new Vector3f(width, height, dist), rot, "tile", 0, 0, 16, 16, color));
    }
}
