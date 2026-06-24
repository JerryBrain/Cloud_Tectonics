package com.example.cloudtectonics.client;

import com.example.cloudtectonics.math.BuildingDebugLogger;
import com.example.cloudtectonics.math.ParametricBuildingGenerator;
import com.example.cloudtectonics.math.StructureComponent;
import com.example.cloudtectonics.network.ModMessages;
import com.example.cloudtectonics.network.ServerboundUpdateWandNBTPacket;
import com.example.cloudtectonics.registry.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * 客户端 Forge 事件监听器。
 * 实现了一直显示在屏幕上的 Debug 日志按钮，同时支持滚轮调整预设朝向、左击取消建造、世界中实时透明虚影渲染。
 */
@Mod.EventBusSubscriber(modid = com.example.cloudtectonics.CloudTectonicsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    // Redirected to ClientAccess.debugEnabled

    public static final KeyMapping DEBUG_KEY = new KeyMapping(
            "key.cloudtectonics.debug_toggle",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_F10,
            "key.categories.cloudtectonics"
    );

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!ClientAccess.debugEnabled) return; // 关闭调试功能后，不显示圆形按钮

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return; // 当 F1 隐藏 HUD 时，不进行绘制
        
        int centerX = 15;
        int centerY = 15;
        float radius = 5.0f;

        boolean logging = BuildingDebugLogger.isLogging();
        int dotColor = logging ? 0xFFFF3333 : 0xFF33FF33; // 红色正在录制，绿色没在录制
        String text = logging ? "REC (F10)" : "STANDBY (F10)";
        int textColor = logging ? 0xFFFF5555 : 0xFF888888; // 亮红色或灰色文本
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        // 绘制黑色描边底圈 (略大一点)
        drawFilledCircle(guiGraphics, centerX, centerY, radius + 1.5f, 0xFF000000);
        // 绘制中心红/绿状态圆点
        drawFilledCircle(guiGraphics, centerX, centerY, radius, dotColor);
        
        // 绘制圆点右侧的文本提示
        guiGraphics.drawString(mc.font, text, centerX + 10, centerY - 4, textColor, true);
    }

    private static void drawFilledCircle(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        
        Matrix4f matrix = guiGraphics.pose().last().pose();
        builder.vertex(matrix, centerX, centerY, 0).color(r, g, b, a).endVertex();
        
        int numSegments = 16;
        for (int i = 0; i <= numSegments; i++) {
            double angle = i * 2.0 * Math.PI / numSegments;
            float x = centerX + (float) (Math.cos(angle) * radius);
            float y = centerY + (float) (Math.sin(angle) * radius);
            builder.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }
        
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ClientAccess.debugEnabled && DEBUG_KEY.consumeClick()) {
            ClientAccess.toggleDebugLogging();
        }
    }

    // ================== 建筑虚影放置逻辑 ==================

    private static CompoundTag cachedTag = null;
    private static List<StructureComponent> cachedComponents = null;

    private static List<StructureComponent> getHologramComponents(CompoundTag activePresetTag) {
        if (cachedTag == null || !cachedTag.equals(activePresetTag)) {
            cachedTag = activePresetTag.copy();
            int bays = activePresetTag.getInt("bays");
            int depths = activePresetTag.getInt("depths");
            float widthMid = activePresetTag.getFloat("widthMid");
            float widthSide = activePresetTag.getFloat("widthSide");
            float depthStep = activePresetTag.getFloat("depthStep");
            float colHeight = activePresetTag.getFloat("colHeight");
            float roofPitch = activePresetTag.getFloat("roofPitch");
            float eavesLen = activePresetTag.getFloat("eavesLen");
            float gableSetback = activePresetTag.getFloat("gableSetback");
            float cornerLift = activePresetTag.getFloat("cornerLift");
            int dougongLv = activePresetTag.getInt("dougongLv");

            boolean showRoof = activePresetTag.getBoolean("showRoof");
            boolean showRafters = activePresetTag.getBoolean("showRafters");
            boolean showPurlins = activePresetTag.getBoolean("showPurlins");
            boolean showBeams = activePresetTag.getBoolean("showBeams");
            boolean showDougong = activePresetTag.getBoolean("showDougong");
            boolean showColumns = activePresetTag.getBoolean("showColumns");
            boolean showBase = activePresetTag.getBoolean("showBase");

            cachedComponents = ParametricBuildingGenerator.generate(
                    bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv,
                    showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase
            );
        }
        return cachedComponents;
    }
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem)) {
                stack = mc.player.getItemInHand(InteractionHand.OFF_HAND);
            }

            if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
                if (stack.hasTag() && stack.getTag().contains("ActivePreset")) {
                    CompoundTag tag = stack.getTag().getCompound("ActivePreset");
                    
                    BlockHitResult hit = com.example.cloudtectonics.item.BuildingWandItem.customRaycast(mc.player, 32.0);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockPos placePos = hit.getBlockPos().relative(hit.getDirection());
                        
                        Vec3 camPos = event.getCamera().getPosition();
                        PoseStack poseStack = event.getPoseStack();
                        
                        poseStack.pushPose();
                        // 移动到相对于相机的渲染世界坐标
                        poseStack.translate(placePos.getX() - camPos.x, placePos.getY() - camPos.y, placePos.getZ() - camPos.z);
                        
                        // 应用 NBT 里的旋转
                        int rotationYaw = tag.getInt("Rotation");
                        float rotRad = (float) Math.toRadians(rotationYaw);
                        poseStack.mulPose(Axis.YP.rotation(rotRad));
                        
                        RenderSystem.enableDepthTest();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.depthMask(true); // 使内部面被前面的瓦片等剔除，消除混乱的透视感
                        
                        List<StructureComponent> comps = getHologramComponents(tag);
                        
                        Tesselator tesselator = Tesselator.getInstance();
                        BufferBuilder builder = tesselator.getBuilder();
                        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getId());
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                        
                        for (StructureComponent comp : comps) {
                            poseStack.pushPose();
                            poseStack.translate(comp.localPos.x, comp.localPos.y, comp.localPos.z);
                            poseStack.mulPose(comp.rotation);
                            poseStack.scale(comp.size.x, comp.size.y, comp.size.z);
                            
                            String texPath = com.example.cloudtectonics.math.StructureComponent.getTexturePath(comp);
                             net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = 
                                Minecraft.getInstance()
                                    .getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                                    .apply(new net.minecraft.resources.ResourceLocation("minecraft", texPath));
                            
                            float u0 = sprite.getU0(), v0 = sprite.getV0();
                            float u1 = sprite.getU1(), v1 = sprite.getV1();
                            
                            int color = comp.color;
                            float r = ((color >> 16) & 0xFF) / 255.0f;
                            float g = ((color >> 8) & 0xFF) / 255.0f;
                            float b = (color & 0xFF) / 255.0f;
                            float a = 0.5f; // 半透明虚影效果
                            
                            drawCube(poseStack, builder, r, g, b, a, 0, u0, v0, u1, v1, comp.size, rotRad);
                            drawCube(poseStack, builder, r, g, b, a, 1, u0, v0, u1, v1, comp.size, rotRad);
                            drawCube(poseStack, builder, r, g, b, a, 2, u0, v0, u1, v1, comp.size, rotRad);
                            drawCube(poseStack, builder, r, g, b, a, 3, u0, v0, u1, v1, comp.size, rotRad);
                            drawCube(poseStack, builder, r, g, b, a, 4, u0, v0, u1, v1, comp.size, rotRad);
                            drawCube(poseStack, builder, r, g, b, a, 5, u0, v0, u1, v1, comp.size, rotRad);
                            
                            poseStack.popPose();
                        }
                        
                        tesselator.end();
                        poseStack.popPose();
                        
                        RenderSystem.disableBlend();
                    }
                }
            }

            // 2. 调试物理碰撞箱渲染 (当开启 Debug 录制/日志模式时显示周围代理方块的绿色碰撞箱)
            renderProxyCollisionBoxes(event, mc);
        }
    }

    private static void renderProxyCollisionBoxes(RenderLevelStageEvent event, Minecraft mc) {
        if (!ClientAccess.debugEnabled || !BuildingDebugLogger.isLogging()) return;
        if (mc.level == null || mc.player == null) return;

        net.minecraft.world.entity.player.Player player = mc.player;
        BlockPos playerPos = player.blockPosition();
        int radius = 24; // 扩大扫描半径，确保更远距离的建筑也能看到
        int radY = 16;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        
        RenderSystem.disableDepthTest(); // 关闭深度测试，开启 X-Ray 透视效果，使得藏在建筑内部的碰撞箱清晰可见
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radY; y <= radY; y++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(pos);
                    
                    // 1. 发现控制锚点方块，画青色 1x1x1 边框
                    if (state.is(ModBlocks.BUILDING_ANCHOR.get())) {
                        poseStack.pushPose();
                        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                        net.minecraft.world.phys.AABB blockBox = new net.minecraft.world.phys.AABB(0, 0, 0, 1, 1, 1);
                        drawWireframeAABB(poseStack, builder, blockBox, 0.0f, 0.8f, 1.0f, 0.9f); // 青色高亮
                        poseStack.popPose();
                    }
                    
                    // 2. 发现物理代理方块，画黄色 1x1x1 整体边界框与绿色实际碰撞子形状
                    if (state.is(ModBlocks.BUILDING_PROXY.get())) {
                        poseStack.pushPose();
                        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                        
                        // 黄色半透明框，代表代理方块占据的 1x1x1 空格
                        net.minecraft.world.phys.AABB blockBox = new net.minecraft.world.phys.AABB(0, 0, 0, 1, 1, 1);
                        drawWireframeAABB(poseStack, builder, blockBox, 1.0f, 0.8f, 0.0f, 0.4f); // 亮黄色
                        
                        // 绿色框，代表该代理方块内部实际的 1/16 精度碰撞子区域
                        net.minecraft.world.phys.shapes.VoxelShape shape = state.getCollisionShape(mc.level, pos);
                        if (!shape.isEmpty()) {
                            for (net.minecraft.world.phys.AABB aabb : shape.toAabbs()) {
                                drawWireframeAABB(poseStack, builder, aabb, 0.0f, 1.0f, 0.2f, 0.9f); // 亮绿色
                            }
                        }
                        
                        poseStack.popPose();
                    }
                }
            }
        }
        
        tesselator.end();
        RenderSystem.enableDepthTest(); // 恢复深度测试
        RenderSystem.disableBlend();
    }

    private static void drawWireframeAABB(PoseStack poseStack, VertexConsumer consumer, net.minecraft.world.phys.AABB aabb, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        // Bottom face
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();

        // Top face
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();

        // Vertical edges
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    private static void drawCube(PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a, int faceIndex, float u0, float v0, float u1, float v1, Vector3f size, float rotationRad) {
        Matrix4f matrix = poseStack.last().pose();

        float nx = 0, ny = 0, nz = 0;
        switch (faceIndex) {
            case 0: nz = 1; break;
            case 1: nz = -1; break;
            case 2: nx = -1; break;
            case 3: nx = 1; break;
            case 4: ny = 1; break;
            case 5: ny = -1; break;
        }

        // 旋转法线方向，得到在局部/世界坐标中的朝向来做漫反射计算
        Vector3f normal = new Vector3f(nx, ny, nz);
        normal.rotateY(rotationRad);

        float diffuse = 0.6f;
        if (normal.y() > 0.05f) {
            diffuse = 0.8f + 0.2f * normal.y();
        } else if (normal.y() < -0.05f) {
            diffuse = 0.5f + 0.1f * normal.y();
        } else {
            diffuse = 0.6f + 0.2f * Math.abs(normal.z());
        }

        int finalR = Math.max(0, Math.min(255, (int)(r * diffuse * 255)));
        int finalG = Math.max(0, Math.min(255, (int)(g * diffuse * 255)));
        int finalB = Math.max(0, Math.min(255, (int)(b * diffuse * 255)));
        int finalA = (int)(a * 255);

        float W_val = 1.0f;
        float H_val = 1.0f;
        switch (faceIndex) {
            case 0: // Front
            case 1: // Back
                W_val = size.x;
                H_val = size.y;
                break;
            case 2: // Left
            case 3: // Right
                W_val = size.z;
                H_val = size.y;
                break;
            case 4: // Top
            case 5: // Bottom
                W_val = size.x;
                H_val = size.z;
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

                    switch (faceIndex) {
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

                    consumer.vertex(matrix, vx, vy, vz).color(finalR, finalG, finalB, finalA).uv(u, v_uv).endVertex();
                }
            }
        }
    }

    // ================== 左击取消 & 滚轮旋转 ==================

    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem)) {
            stack = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        }
        
        if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
            if (event.isAttack()) { // 左击攻击
                if (stack.hasTag() && stack.getTag().contains("ActivePreset")) {
                    // 取消预设，退出建造模式
                    stack.getTag().remove("ActivePreset");
                    ModMessages.sendToServer(new ServerboundUpdateWandNBTPacket(new CompoundTag()));
                    mc.player.displayClientMessage(Component.literal("§c已退出建造模式"), true);
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
            if (stack.hasTag() && stack.getTag().contains("ActivePreset")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem)) {
            stack = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        }

        if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
            if (stack.hasTag() && stack.getTag().contains("ActivePreset")) {
                CompoundTag tag = stack.getTag().getCompound("ActivePreset");
                int currentRot = tag.getInt("Rotation");
                double delta = event.getScrollDelta();
                
                if (delta != 0) {
                    if (delta > 0) {
                        currentRot = (currentRot + 15) % 360;
                    } else {
                        currentRot = (currentRot - 15 + 360) % 360;
                    }
                    tag.putInt("Rotation", currentRot);
                    
                    // 同步到服务端
                    ModMessages.sendToServer(new ServerboundUpdateWandNBTPacket(tag));
                    mc.player.displayClientMessage(Component.literal("§e旋转朝向: " + currentRot + "°"), true);
                    event.setCanceled(true); // 拦截滚轮切快捷栏槽位
                }
            }
        }
    }
}
