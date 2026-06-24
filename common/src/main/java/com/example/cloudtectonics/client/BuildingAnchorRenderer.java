package com.example.cloudtectonics.client;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.math.GomedricTransformer;
import com.example.cloudtectonics.math.StructureComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 建筑主控制方块渲染器。
 * 使用静态顶点烘焙（Vertex Baking）至 VertexBuffer（VBO），实现 O(1) 的 Draw Call，支持同屏多复杂建筑无卡顿。
 */
public class BuildingAnchorRenderer implements BlockEntityRenderer<BuildingAnchorBlockEntity> {

    // 缓存每个 BlockEntity 对应的 VertexBuffer
    private static final Map<BuildingAnchorBlockEntity, VertexBuffer> VBO_CACHE = new ConcurrentHashMap<>();
    private static final Map<BuildingAnchorBlockEntity, Boolean> DIRTY_FLAGS = new ConcurrentHashMap<>();

    public BuildingAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BuildingAnchorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // 清理已被移除的实体的 VBO 防止内存泄漏
        cleanupCache();

        VertexBuffer vbo = VBO_CACHE.get(pBlockEntity);
        boolean dirty = DIRTY_FLAGS.getOrDefault(pBlockEntity, true);

        if (vbo == null || dirty) {
            if (vbo != null) {
                vbo.close();
            }
            vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            bakeVertices(vbo, pBlockEntity, pPackedLight, pPackedOverlay);
            VBO_CACHE.put(pBlockEntity, vbo);
            DIRTY_FLAGS.put(pBlockEntity, false);
        }

        // 绑定材质，准备渲染
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        // 使用原生的白色混凝土纹理作为纯色材质贴图
        RenderSystem.setShaderTexture(0, net.minecraft.client.Minecraft.getInstance().getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getId());
        
        pPoseStack.pushPose();
        
        // 显式启用深度测试，防止透视和X光穿透效果
        RenderSystem.enableDepthTest();
        
        // 如果是预览虚影状态，开启混合模式以支持透明度
        boolean isPreview = pBlockEntity.isPreview();
        if (isPreview) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // 在着色器层面也做一定的透明度调制
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.6f);
            // 启用深度写入，使瓦片等前端结构遮挡后面的瓦片和内部木作，消除混乱的透视感
            RenderSystem.depthMask(true);
        } else {
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.depthMask(true);
        }

        vbo.bind();
        vbo.drawWithShader(pPoseStack.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionColorTexLightmapShader());
        VertexBuffer.unbind();

        if (isPreview) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }

        pPoseStack.popPose();
    }

    /**
     * 将所有组件进行矩阵变换，计算并进行防 Z-Fighting 处理，最后一次性写入 Buffer
     */
    private void bakeVertices(VertexBuffer vbo, BuildingAnchorBlockEntity entity, int light, int overlay) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        
        // 我们使用位置、颜色、纹理、光照的顶点格式
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        org.joml.Quaternionf anchorRot = entity.getRotation();
        int alpha = entity.isPreview() ? 150 : 255;

        for (StructureComponent comp : entity.getComponents()) {
            // Apply a tiny visual geometry inflation (0.005 blocks / 0.08 pixels) to close sub-pixel rasterization cracks
            float inflate = 0.005f;
            Vector3f visualSize = new Vector3f(
                comp.size.x + inflate,
                comp.size.y + inflate,
                comp.size.z + inflate
            );
            Matrix4f transform = new Matrix4f()
                    .rotate(anchorRot)
                    .translate(comp.localPos)
                    .rotate(comp.rotation)
                    .scale(visualSize);
            
            Matrix3f normalTransform = new Matrix3f(transform).invert().transpose();

            if (comp.shape == StructureComponent.ShapeType.WEDGE) {
                buildWedgeQuads(builder, transform, normalTransform, comp, light, alpha);
            } else {
                // 生成基础的六个面的 Quad
                buildCubeQuads(builder, transform, normalTransform, comp, light, alpha);
            }
        }

        BufferBuilder.RenderedBuffer renderedBuffer = builder.end();
        vbo.bind();
        vbo.upload(renderedBuffer);
        VertexBuffer.unbind();
    }

    private void buildWedgeQuads(BufferBuilder builder, Matrix4f transform, Matrix3f normalTransform, StructureComponent comp, int light, int alpha) {
        // 昂 (Wedge) 的五个面定义:
        // 0: Bottom face, normal [0, -1, 0]
        // 1: Back face, normal [0, 0, -1]
        // 2: Slanted face, normal [0, 1, 1] normalized (approx [0, 0.707f, 0.707f])
        // 3: Left face (triangular, collapsed quad), normal [-1, 0, 0]
        // 4: Right face (triangular, collapsed quad), normal [1, 0, 0]

        // 获取特定方块贴图图集的 UV 坐标
        String texPath = com.example.cloudtectonics.math.StructureComponent.getTexturePath(comp);
        net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = 
            net.minecraft.client.Minecraft.getInstance()
                .getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                .apply(new net.minecraft.resources.ResourceLocation("minecraft", texPath));
        
        float u0 = sprite.getU0(), v0 = sprite.getV0();
        float u1 = sprite.getU1(), v1 = sprite.getV1();

        // 解析 ARGB 颜色
        int r = (comp.color >> 16) & 0xFF;
        int g = (comp.color >> 8) & 0xFF;
        int b = comp.color & 0xFF;
        int compA = (comp.color >> 24) & 0xFF;
        int finalAlpha = (compA * alpha) / 255;

        // 定义每个面的 4 个顶点局部坐标 [vx, vy, vz]
        // 注意：有些面在 WEDGE 中是三角形，通过合并最后两个点实现 QUAD 兼容
        float[][][] faceVertices = {
            // 0: Bottom face (Normal: 0, -1, 0)
            {
                {-0.5f, -0.5f, -0.5f},
                { 0.5f, -0.5f, -0.5f},
                { 0.5f, -0.5f,  0.5f},
                {-0.5f, -0.5f,  0.5f}
            },
            // 1: Back face (Normal: 0, 0, -1)
            {
                {-0.5f, -0.5f, -0.5f},
                {-0.5f,  0.5f, -0.5f},
                { 0.5f,  0.5f, -0.5f},
                { 0.5f, -0.5f, -0.5f}
            },
            // 2: Slanted face (Normal: 0, 0.707f, 0.707f)
            {
                {-0.5f,  0.5f, -0.5f},
                {-0.5f, -0.5f,  0.5f},
                { 0.5f, -0.5f,  0.5f},
                { 0.5f,  0.5f, -0.5f}
            },
            // 3: Left face (Normal: -1, 0, 0) (triangular, collapse 2nd & 3rd point)
            {
                {-0.5f, -0.5f, -0.5f},
                {-0.5f, -0.5f,  0.5f},
                {-0.5f, -0.5f,  0.5f},
                {-0.5f,  0.5f, -0.5f}
            },
            // 4: Right face (Normal: 1, 0, 0) (triangular, collapse 3rd & 4th point)
            {
                { 0.5f, -0.5f, -0.5f},
                { 0.5f,  0.5f, -0.5f},
                { 0.5f, -0.5f,  0.5f},
                { 0.5f, -0.5f,  0.5f}
            }
        };

        float[][] faceNormals = {
            { 0, -1,  0}, // Bottom
            { 0,  0, -1}, // Back
            { 0,  0.7071f,  0.7071f}, // Slanted
            {-1,  0,  0}, // Left
            { 1,  0,  0}  // Right
        };

        // UV 映射方案（为了简化，直接将 [u0, u1] 映射到各面）
        float[][] uvMap = {
            {0, 0, 1, 0, 1, 1, 0, 1}, // Bottom
            {0, 1, 0, 0, 1, 0, 1, 1}, // Back
            {0, 0, 0, 1, 1, 1, 1, 0}, // Slanted
            {0, 1, 1, 1, 1, 1, 0, 0}, // Left (triangular)
            {0, 1, 0, 0, 1, 1, 1, 1}  // Right (triangular)
        };

        for (int i = 0; i < 5; i++) {
            Vector3f normal = new Vector3f(faceNormals[i][0], faceNormals[i][1], faceNormals[i][2]);
            normal.mul(normalTransform).normalize();

            // 漫反射系数
            float diffuse = 0.6f;
            float absZ = Math.abs(normal.z());
            if (normal.y() > 0.05f) {
                diffuse = 0.8f + 0.2f * normal.y();
            } else if (normal.y() < -0.05f) {
                diffuse = 0.5f + 0.1f * normal.y();
            } else {
                diffuse = 0.6f + 0.2f * absZ;
            }

            int finalR = Math.max(0, Math.min(255, (int)(r * diffuse)));
            int finalG = Math.max(0, Math.min(255, (int)(g * diffuse)));
            int finalB = Math.max(0, Math.min(255, (int)(b * diffuse)));

            for (int v = 0; v < 4; v++) {
                float vx = faceVertices[i][v][0];
                float vy = faceVertices[i][v][1];
                float vz = faceVertices[i][v][2];

                float uOffset = uvMap[i][v * 2];
                float vOffset = uvMap[i][v * 2 + 1];
                float u = u0 + (u1 - u0) * uOffset;
                float v_uv = v0 + (v1 - v0) * vOffset;

                Vector4f pos = new Vector4f(vx, vy, vz, 1.0f).mul(transform);
                Vector3f offsetPos = GomedricTransformer.applyZFightingOffset(normal, new Vector3f(pos.x, pos.y, pos.z));

                builder.vertex(offsetPos.x, offsetPos.y, offsetPos.z)
                       .color(finalR, finalG, finalB, finalAlpha)
                       .uv(u, v_uv)
                       .uv2(light)
                       .endVertex();
            }
        }
    }

    private void buildCubeQuads(BufferBuilder builder, Matrix4f transform, Matrix3f normalTransform, StructureComponent comp, int light, int alpha) {
        float[][] normals = {
            { 0,  0,  1}, // Front
            { 0,  0, -1}, // Back
            {-1,  0,  0}, // Left
            { 1,  0,  0}, // Right
            { 0,  1,  0}, // Top
            { 0, -1,  0}  // Bottom
        };

        // 获取特定方块贴图图集的 UV 坐标
        String texPath = com.example.cloudtectonics.math.StructureComponent.getTexturePath(comp);
        net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = 
            net.minecraft.client.Minecraft.getInstance()
                .getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                .apply(new net.minecraft.resources.ResourceLocation("minecraft", texPath));
        
        float u0 = sprite.getU0(), v0 = sprite.getV0();
        float u1 = sprite.getU1(), v1 = sprite.getV1();

        // 解析 ARGB 颜色
        int r = (comp.color >> 16) & 0xFF;
        int g = (comp.color >> 8) & 0xFF;
        int b = comp.color & 0xFF;
        int compA = (comp.color >> 24) & 0xFF;
        int finalAlpha = (compA * alpha) / 255;

        for (int i = 0; i < 6; i++) {
            Vector3f normal = new Vector3f(normals[i][0], normals[i][1], normals[i][2]);
            normal.mul(normalTransform).normalize();

            // 计算世界空间法线的漫反射系数 (Manual Diffuse Lighting)
            float diffuse = 0.6f;
            float absZ = Math.abs(normal.z());
            if (normal.y() > 0.05f) {
                // 朝上：最亮 (1.0)
                diffuse = 0.8f + 0.2f * normal.y();
            } else if (normal.y() < -0.05f) {
                // 朝下：最暗 (0.4)
                diffuse = 0.5f + 0.1f * normal.y();
            } else {
                // 侧面：根据Z/X朝向在 0.6 到 0.8 之间过渡
                diffuse = 0.6f + 0.2f * absZ;
            }

            int finalR = Math.max(0, Math.min(255, (int)(r * diffuse)));
            int finalG = Math.max(0, Math.min(255, (int)(g * diffuse)));
            int finalB = Math.max(0, Math.min(255, (int)(b * diffuse)));

            // 根据每个面 i 动态分割为 1x1 方块面单元渲染，防止 UV 拉伸和贴图混乱
            float W_val = 1.0f;
            float H_val = 1.0f;
            switch (i) {
                case 0: // Front
                case 1: // Back
                    W_val = comp.size.x;
                    H_val = comp.size.y;
                    break;
                case 2: // Left
                case 3: // Right
                    W_val = comp.size.z;
                    H_val = comp.size.y;
                    break;
                case 4: // Top
                case 5: // Bottom
                    W_val = comp.size.x;
                    H_val = comp.size.z;
                    break;
            }

            float stepU = 1.0f;
            float stepV = 1.0f;
            for (float u_start = 0.0f; u_start < W_val; u_start += stepU) {
                float u_end = Math.min(u_start + stepU, W_val);
                float cell_w = u_end - u_start;
                float tx0 = (u_start / W_val) - 0.5f;
                float tx1 = (u_end / W_val) - 0.5f;

                for (float v_start = 0.0f; v_start < H_val; v_start += stepV) {
                    float v_end = Math.min(v_start + stepV, H_val);
                    float cell_h = v_end - v_start;
                    float ty0 = (v_start / H_val) - 0.5f;
                    float ty1 = (v_end / H_val) - 0.5f;

                    for (int v = 0; v < 4; v++) {
                        float vx = 0, vy = 0, vz = 0;
                        float tx_val = 0, ty_val = 0;

                        switch (i) {
                            case 0: // Front
                                tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                                ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                                vx = tx_val;
                                vy = ty_val;
                                vz = 0.5f;
                                break;
                            case 1: // Back
                                tx_val = (v == 0 || v == 3) ? tx1 : tx0;
                                ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                                vx = tx_val;
                                vy = ty_val;
                                vz = -0.5f;
                                break;
                            case 2: // Left
                                tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                                ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                                vx = -0.5f;
                                vy = ty_val;
                                vz = tx_val;
                                break;
                            case 3: // Right
                                tx_val = (v == 0 || v == 3) ? tx1 : tx0;
                                ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                                vx = 0.5f;
                                vy = ty_val;
                                vz = tx_val;
                                break;
                            case 4: // Top
                                tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                                ty_val = (v == 0 || v == 1) ? ty1 : ty0;
                                vx = tx_val;
                                vy = 0.5f;
                                vz = ty_val;
                                break;
                            case 5: // Bottom
                                tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                                ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                                vx = tx_val;
                                vy = -0.5f;
                                vz = ty_val;
                                break;
                        }

                        float u = (tx_val == tx0) ? u0 : u0 + (u1 - u0) * cell_w;
                        float v_uv = (ty_val == ty0) ? v0 : v0 + (v1 - v0) * cell_h;

                        Vector4f pos = new Vector4f(vx, vy, vz, 1.0f).mul(transform);
                        Vector3f offsetPos = GomedricTransformer.applyZFightingOffset(normal, new Vector3f(pos.x, pos.y, pos.z));

                        builder.vertex(offsetPos.x, offsetPos.y, offsetPos.z)
                               .color(finalR, finalG, finalB, finalAlpha)
                               .uv(u, v_uv)
                               .uv2(light)
                               .endVertex();
                    }
                }
            }
        }
    }

    /**
     * 标记某个实体的 VBO 为脏数据，在下一帧强制重新烘焙
     */
    public static void markDirty(BuildingAnchorBlockEntity entity) {
        DIRTY_FLAGS.put(entity, true);
    }

    /**
     * 清理内存：定期遍历并关闭已经被打碎或卸载的实体的 VBO
     */
    private void cleanupCache() {
        Iterator<Map.Entry<BuildingAnchorBlockEntity, VertexBuffer>> it = VBO_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BuildingAnchorBlockEntity, VertexBuffer> entry = it.next();
            BuildingAnchorBlockEntity entity = entry.getKey();
            if (entity.isRemoved()) {
                entry.getValue().close();
                DIRTY_FLAGS.remove(entity);
                it.remove();
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BuildingAnchorBlockEntity pBlockEntity) {
        // 由于建筑可能非常大，超过了一个 Chunk，我们开启屏幕外渲染支持
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256; // 提供足够的视距，大型建筑能在远处被看到
    }
}
