package com.example.cloudtectonics.fabric.client;

import com.example.cloudtectonics.client.ClientAccess;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

@Environment(EnvType.CLIENT)
public class FabricClientEvents {

    public static final KeyMapping DEBUG_KEY = new KeyMapping(
            "key.cloudtectonics.debug_toggle",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_F10,
            "key.categories.cloudtectonics"
    );

    private static CompoundTag cachedTag = null;
    private static List<StructureComponent> cachedComponents = null;

    public static void init() {
        // Register keybind
        KeyBindingHelper.registerKeyBinding(DEBUG_KEY);

        // Register client ticks for F10 debug keypress
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (DEBUG_KEY.consumeClick()) {
                ClientAccess.toggleDebugLogging();
            }
        });

        // Register HUD renderer (for debug REC / STANDBY status)
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            if (!ClientAccess.debugEnabled) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return;

            int centerX = 15;
            int centerY = 15;
            float radius = 5.0f;

            boolean logging = BuildingDebugLogger.isLogging();
            int dotColor = logging ? 0xFFFF3333 : 0xFF33FF33;
            String text = logging ? "REC (F10)" : "STANDBY (F10)";
            int textColor = logging ? 0xFFFF5555 : 0xFF888888;

            // Draw outline
            drawFilledCircle(guiGraphics, centerX, centerY, radius + 1.5f, 0xFF000000);
            // Draw center dot
            drawFilledCircle(guiGraphics, centerX, centerY, radius, dotColor);
            // Draw text
            guiGraphics.drawString(mc.font, text, centerX + 10, centerY - 4, textColor, true);
        });

        // Register World rendering (translucent hologram preview and debug lines)
        WorldRenderEvents.AFTER_TRANSLUCENT.register(FabricClientEvents::onRenderLevelStage);
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

    private static void onRenderLevelStage(WorldRenderContext context) {
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

                    Vec3 camPos = context.camera().getPosition();
                    PoseStack poseStack = context.matrixStack();

                    poseStack.pushPose();
                    poseStack.translate(placePos.getX() - camPos.x, placePos.getY() - camPos.y, placePos.getZ() - camPos.z);

                    int rotationYaw = tag.getInt("Rotation");
                    float rotRad = (float) Math.toRadians(rotationYaw);
                    poseStack.mulPose(Axis.YP.rotation(rotRad));

                    RenderSystem.enableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.depthMask(true);

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
                        float a = 0.5f;

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
            case 0:
            case 1:
                W_val = size.x;
                H_val = size.y;
                break;
            case 2:
            case 3:
                W_val = size.z;
                H_val = size.y;
                break;
            case 4:
            case 5:
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
                        case 0:
                            tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                            ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                            vx = tx_val;
                            vy = ty_val;
                            vz = 0.5f;
                            break;
                        case 1:
                            tx_val = (v == 0 || v == 3) ? tx1 : tx0;
                            ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                            vx = tx_val;
                            vy = ty_val;
                            vz = -0.5f;
                            break;
                        case 2:
                            tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                            ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                            vx = -0.5f;
                            vy = ty_val;
                            vz = tx_val;
                            break;
                        case 3:
                            tx_val = (v == 0 || v == 3) ? tx1 : tx0;
                            ty_val = (v == 2 || v == 3) ? ty1 : ty0;
                            vx = 0.5f;
                            vy = ty_val;
                            vz = tx_val;
                            break;
                        case 4:
                            tx_val = (v == 1 || v == 2) ? tx1 : tx0;
                            ty_val = (v == 0 || v == 1) ? ty1 : ty0;
                            vx = tx_val;
                            vy = 0.5f;
                            vz = ty_val;
                            break;
                        case 5:
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
}
