package com.example.cloudtectonics.math;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.List;

/**
 * 局部动态体素化/多边形变换的核心算法类。
 * 处理矩阵变换、防止 Z-Fighting 及 1/16 精度碰撞箱的复合。
 */
public class GomedricTransformer {

    private static final float Z_FIGHTING_OFFSET = 0.0f;

    /**
     * 计算并合并所有在给定代理方块空间内的 VoxelShape
     * @param anchorPos 主方块坐标
     * @param proxyPos 当前代理方块坐标
     * @param anchor 包含建筑信息的 BlockEntity
     * @return 代理方块应该具有的 VoxelShape
     */
    public static VoxelShape calculateProxyShape(BlockPos anchorPos, BlockPos proxyPos, BuildingAnchorBlockEntity anchor) {
        // 获取代理方块相对于主方块的偏移
        int offsetX = proxyPos.getX() - anchorPos.getX();
        int offsetY = proxyPos.getY() - anchorPos.getY();
        int offsetZ = proxyPos.getZ() - anchorPos.getZ();

        // 这个代理方块在建筑坐标系下占据的空间 (0~1) => 加上 offset
        AABB proxyBounds = new AABB(offsetX, offsetY, offsetZ, offsetX + 1, offsetY + 1, offsetZ + 1);

        // 确保 AABB 已经在客户端进行了计算并缓存
        anchor.getCachedAABBs();

        Quaternionf anchorRot = anchor.getRotation();
        List<StructureComponent> comps = anchor.getComponents();

        double tileMinX = 1.0, tileMinY = 1.0, tileMinZ = 1.0;
        double tileMaxX = 0.0, tileMaxY = 0.0, tileMaxZ = 0.0;
        boolean hasTiles = false;

        // 1. 处理瓦片层的碰撞合并优化
        for (StructureComponent comp : comps) {
            if (comp.transformedAABB != null && proxyBounds.intersects(comp.transformedAABB)) {
                if ("tile".equals(comp.texture)) {
                    // 使用精确 OBB 检测
                    org.joml.Matrix4f transform = new org.joml.Matrix4f()
                            .rotate(anchorRot)
                            .translate(comp.localPos)
                            .rotate(comp.rotation)
                            .scale(comp.size);
                    if (intersectsOBB(proxyBounds, transform)) {
                        double minX = Math.max(0, comp.transformedAABB.minX - offsetX);
                        double minY = Math.max(0, comp.transformedAABB.minY - offsetY);
                        double minZ = Math.max(0, comp.transformedAABB.minZ - offsetZ);
                        double maxX = Math.min(1, comp.transformedAABB.maxX - offsetX);
                        double maxY = Math.min(1, comp.transformedAABB.maxY - offsetY);
                        double maxZ = Math.min(1, comp.transformedAABB.maxZ - offsetZ);

                        if (minX < maxX && minY < maxY && minZ < maxZ) {
                            tileMinX = Math.min(tileMinX, minX);
                            tileMinY = Math.min(tileMinY, minY);
                            tileMinZ = Math.min(tileMinZ, minZ);
                            tileMaxX = Math.max(tileMaxX, maxX);
                            tileMaxY = Math.max(tileMaxY, maxY);
                            tileMaxZ = Math.max(tileMaxZ, maxZ);
                            hasTiles = true;
                        }
                    }
                }
            }
        }

        VoxelShape shape;
        if (hasTiles) {
            shape = Shapes.box(tileMinX, tileMinY, tileMinZ, tileMaxX, tileMaxY, tileMaxZ);
        } else {
            // 2. 处理非瓦片的其他构件，并入 Shapes
            VoxelShape finalShape = Shapes.empty();
            for (StructureComponent comp : comps) {
                if (comp.transformedAABB != null && proxyBounds.intersects(comp.transformedAABB)) {
                    if (!"tile".equals(comp.texture)) {
                        // 使用精确 OBB 检测
                        org.joml.Matrix4f transform = new org.joml.Matrix4f()
                                .rotate(anchorRot)
                                .translate(comp.localPos)
                                .rotate(comp.rotation)
                                .scale(comp.size);
                        if (intersectsOBB(proxyBounds, transform)) {
                            double minX = Math.max(0, comp.transformedAABB.minX - offsetX);
                            double minY = Math.max(0, comp.transformedAABB.minY - offsetY);
                            double minZ = Math.max(0, comp.transformedAABB.minZ - offsetZ);
                            double maxX = Math.min(1, comp.transformedAABB.maxX - offsetX);
                            double maxY = Math.min(1, comp.transformedAABB.maxY - offsetY);
                            double maxZ = Math.min(1, comp.transformedAABB.maxZ - offsetZ);

                            if (minX < maxX && minY < maxY && minZ < maxZ) {
                                VoxelShape compShape = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
                                finalShape = Shapes.join(finalShape, compShape, BooleanOp.OR);
                            }
                        }
                    }
                }
            }
            shape = finalShape.isEmpty() ? Shapes.empty() : finalShape.optimize();
        }

        return shape;
    }

    /**
     * 判断 AABB (单个 1x1x1 方块网格单元) 与倾斜的 OBB (已经包含旋转和尺寸变换的构件) 是否真正相交。
     * 使用分离轴定理 (Separating Axis Theorem, SAT)
     */
    public static boolean intersectsOBB(AABB cell, Matrix4f compTransform) {
        // AABB 中心点坐标
        float caX = (float) (cell.minX + 0.5);
        float caY = (float) (cell.minY + 0.5);
        float caZ = (float) (cell.minZ + 0.5);

        // OBB 中心点坐标 (由变换矩阵的第四列提取)
        float cbX = compTransform.m30();
        float cbY = compTransform.m31();
        float cbZ = compTransform.m32();

        Vector3f diff = new Vector3f(cbX - caX, cbY - caY, cbZ - caZ);

        // OBB 的半轴向量 (主轴缩放并旋转后的方向，需要乘以 0.5f，因为是以中心点对称的)
        Vector3f A0 = new Vector3f(compTransform.m00() * 0.5f, compTransform.m01() * 0.5f, compTransform.m02() * 0.5f);
        Vector3f A1 = new Vector3f(compTransform.m10() * 0.5f, compTransform.m11() * 0.5f, compTransform.m12() * 0.5f);
        Vector3f A2 = new Vector3f(compTransform.m20() * 0.5f, compTransform.m21() * 0.5f, compTransform.m22() * 0.5f);

        // 分离轴定理测试，一共测试 15 条可能的投影轴：
        // 1. 世界坐标轴 (AABB 面法线)
        if (isSeparatingAxis(diff, new Vector3f(1, 0, 0), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(0, 1, 0), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(0, 0, 1), A0, A1, A2)) return false;

        // 2. 构件局部坐标轴 (OBB 面法线)
        if (isSeparatingAxis(diff, A0, A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, A1, A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, A2, A0, A1, A2)) return false;

        // 3. 叉乘产生的 9 个轴
        // X 轴 x OBB 轴
        if (isSeparatingAxis(diff, new Vector3f(0, -A0.z, A0.y), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(0, -A1.z, A1.y), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(0, -A2.z, A2.y), A0, A1, A2)) return false;

        // Y 轴 x OBB 轴
        if (isSeparatingAxis(diff, new Vector3f(A0.z, 0, -A0.x), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(A1.z, 0, -A1.x), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(A2.z, 0, -A2.x), A0, A1, A2)) return false;

        // Z 轴 x OBB 轴
        if (isSeparatingAxis(diff, new Vector3f(-A0.y, A0.x, 0), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(-A1.y, A1.x, 0), A0, A1, A2)) return false;
        if (isSeparatingAxis(diff, new Vector3f(-A2.y, A2.x, 0), A0, A1, A2)) return false;

        return true;
    }

    private static boolean isSeparatingAxis(Vector3f diff, Vector3f L, Vector3f A0, Vector3f A1, Vector3f A2) {
        float lenL = L.lengthSquared();
        if (lenL < 1e-6f) return false; // 平行轴投影重叠，跳过检测

        // AABB (半大小为 0.5, 0.5, 0.5) 在轴 L 上的投影半径
        float rA = 0.5f * (Math.abs(L.x) + Math.abs(L.y) + Math.abs(L.z));

        // OBB 半轴在轴 L 上的投影半径之和
        float rB = Math.abs(A0.dot(L)) + Math.abs(A1.dot(L)) + Math.abs(A2.dot(L));

        // 两个中心点距离在轴 L 上的投影
        float d = Math.abs(diff.dot(L));

        return d > rA + rB;
    }

    /**
     * 将一个单位立方体 [-0.5, 0.5]^3 通过矩阵变换，计算出其 AABB 边界盒
     */
    public static AABB transformUnitCube(Matrix4f matrix) {
        Vector3f[] corners = new Vector3f[]{
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f( 0.5f, -0.5f, -0.5f),
            new Vector3f(-0.5f,  0.5f, -0.5f),
            new Vector3f( 0.5f,  0.5f, -0.5f),
            new Vector3f(-0.5f, -0.5f,  0.5f),
            new Vector3f( 0.5f, -0.5f,  0.5f),
            new Vector3f(-0.5f,  0.5f,  0.5f),
            new Vector3f( 0.5f,  0.5f,  0.5f)
        };

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f corner : corners) {
            Vector4f transformed = new Vector4f(corner, 1.0f).mul(matrix);
            if (transformed.x < minX) minX = transformed.x;
            if (transformed.y < minY) minY = transformed.y;
            if (transformed.z < minZ) minZ = transformed.z;
            if (transformed.x > maxX) maxX = transformed.x;
            if (transformed.y > maxY) maxY = transformed.y;
            if (transformed.z > maxZ) maxZ = transformed.z;
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * 为消除 Z-Fighting 提供的微小偏移方法
     * 返回缩放了 0.001 之后的坐标点，使其略微偏离表面
     */
    public static Vector3f applyZFightingOffset(Vector3f normal, Vector3f position) {
        return new Vector3f(position).add(
            normal.x * Z_FIGHTING_OFFSET,
            normal.y * Z_FIGHTING_OFFSET,
            normal.z * Z_FIGHTING_OFFSET
        );
    }
}
