package com.example.cloudtectonics.client;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.client.PresetManager.Preset;
import com.example.cloudtectonics.math.ParametricBuildingGenerator;
import com.example.cloudtectonics.math.StructureComponent;
import com.example.cloudtectonics.network.ModMessages;
import com.example.cloudtectonics.network.ServerboundUpdateBuildingPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BuildingConfigurationScreen extends Screen {

    // 运行模式
    private final boolean isBlockMode;
    private BlockPos blockPos;
    private ItemStack wandStack;
    private BuildingAnchorBlockEntity anchorBE;

    // 结构参数
    private int bays = 3;
    private int depths = 2;
    private float widthMid = 5.5f;
    private float widthSide = 4.5f;
    private float depthStep = 4.5f;
    private float colHeight = 4.0f;
    private float roofPitch = 0.65f;
    private float eavesLen = 1.5f;
    private float gableSetback = 1.0f;
    private float cornerLift = 0.6f;
    private int dougongLv = 1;

    // 显示图层
    private boolean showRoof = true;
    private boolean showRafters = true;
    private boolean showPurlins = true;
    private boolean showBeams = true;
    private boolean showDougong = true;
    private boolean showColumns = true;
    private boolean showBase = true;

    private float yaw = 45.0f;
    private float pitch = -30.0f;
    private float zoom = 10.0f;

    private final List<StructureComponent> previewComponents = new ArrayList<>();

    private int scrollOffset = 0;
    private static class ScrollEntry {
        final net.minecraft.client.gui.components.AbstractWidget widget;
        final int originalY;
        ScrollEntry(net.minecraft.client.gui.components.AbstractWidget widget, int originalY) {
            this.widget = widget;
            this.originalY = originalY;
        }
    }
    private final List<ScrollEntry> scrollEntries = new ArrayList<>();

    private int activeTab = 0;
    private Button tabParamsButton;
    private Button tabLayersButton;

    private Button baysButton;
    private Button depthsButton;
    private Button dougongButton;
    private ParameterSlider midWidthSlider;
    private ParameterSlider sideWidthSlider;
    private ParameterSlider depthStepSlider;
    private ParameterSlider colHeightSlider;
    private ParameterSlider roofPitchSlider;
    private ParameterSlider eavesLenSlider;
    private ParameterSlider gableSetbackSlider;
    private ParameterSlider cornerLiftSlider;

    private final List<Button> layerButtons = new ArrayList<>();

    private EditBox presetNameInput;
    private Button confirmButton;
    private Button closeButton;
    private Button confirmButtonTab1;
    private Button closeButtonTab1;

    public BuildingConfigurationScreen(BlockPos pos) {
        super(Component.literal("歇山营造编辑器 (Block Mode)"));
        this.isBlockMode = true;
        this.blockPos = pos;

        if (Minecraft.getInstance().level != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof BuildingAnchorBlockEntity) {
                this.anchorBE = (BuildingAnchorBlockEntity) be;
                this.bays = anchorBE.getBays();
                this.depths = anchorBE.getDepths();
                this.widthMid = anchorBE.getWidthMid();
                this.widthSide = anchorBE.getWidthSide();
                this.depthStep = anchorBE.getDepthStep();
                this.colHeight = anchorBE.getColHeight();
                this.roofPitch = anchorBE.getRoofPitch();
                this.eavesLen = anchorBE.getEavesLen();
                this.gableSetback = anchorBE.getGableSetback();
                this.cornerLift = anchorBE.getCornerLift();
                this.dougongLv = anchorBE.getDougongLv();
                this.showRoof = anchorBE.isShowRoof();
                this.showRafters = anchorBE.isShowRafters();
                this.showPurlins = anchorBE.isShowPurlins();
                this.showBeams = anchorBE.isShowBeams();
                this.showDougong = anchorBE.isShowDougong();
                this.showColumns = anchorBE.isShowColumns();
                this.showBase = anchorBE.isShowBase();
            }
        }
        rebuildPreview();
    }

    public BuildingConfigurationScreen(ItemStack wandStack) {
        super(Component.literal("营造预设编辑器 (Preset Mode)"));
        this.isBlockMode = false;
        this.wandStack = wandStack;

        if (wandStack.hasTag() && wandStack.getTag().contains("ActivePreset")) {
            CompoundTag tag = wandStack.getTag().getCompound("ActivePreset");
            this.bays = tag.getInt("bays");
            this.depths = tag.getInt("depths");
            this.widthMid = tag.getFloat("widthMid");
            this.widthSide = tag.getFloat("widthSide");
            this.depthStep = tag.getFloat("depthStep");
            this.colHeight = tag.getFloat("colHeight");
            this.roofPitch = tag.getFloat("roofPitch");
            this.eavesLen = tag.getFloat("eavesLen");
            this.gableSetback = tag.getFloat("gableSetback");
            this.cornerLift = tag.getFloat("cornerLift");
            this.dougongLv = tag.getInt("dougongLv");
            this.showRoof = tag.getBoolean("showRoof");
            this.showRafters = tag.getBoolean("showRafters");
            this.showPurlins = tag.getBoolean("showPurlins");
            this.showBeams = tag.getBoolean("showBeams");
            this.showDougong = tag.getBoolean("showDougong");
            this.showColumns = tag.getBoolean("showColumns");
            this.showBase = tag.getBoolean("showBase");
        }
        rebuildPreview();
    }

    private void rebuildPreview() {
        this.previewComponents.clear();
        this.previewComponents.addAll(ParametricBuildingGenerator.generate(
                bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv,
                showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase
        ));
    }

    @Override
    protected void init() {
        super.init();

        this.scrollEntries.clear();
        this.layerButtons.clear();

        int leftX = 10;
        int colW = 140;
        int tabY = 10;
        int spacing = 22;

        this.tabParamsButton = Button.builder(Component.literal("营造参数"), btn -> {
            this.activeTab = 0;
            this.scrollOffset = 0;
            updateVisibility();
        }).bounds(leftX, tabY, 68, 20).build();
        this.addRenderableWidget(this.tabParamsButton);

        this.tabLayersButton = Button.builder(Component.literal("图层控制"), btn -> {
            this.activeTab = 1;
            this.scrollOffset = 0;
            updateVisibility();
        }).bounds(leftX + 72, tabY, 68, 20).build();
        this.addRenderableWidget(this.tabLayersButton);

        int y = 35;

        this.baysButton = Button.builder(Component.literal("开间: " + this.bays), btn -> {
            this.bays = (this.bays == 3) ? 5 : 3;
            btn.setMessage(Component.literal("开间: " + this.bays));
            if (this.sideWidthSlider != null) {
                this.sideWidthSlider.active = (this.bays == 5);
            }
            onParameterChange();
        }).bounds(leftX, y, colW, 20).build();
        this.addRenderableWidget(this.baysButton);
        scrollEntries.add(new ScrollEntry(this.baysButton, y));
        y += spacing;

        this.depthsButton = Button.builder(Component.literal("进深: " + this.depths), btn -> {
            this.depths = this.depths + 1;
            if (this.depths > 4) this.depths = 2;
            btn.setMessage(Component.literal("进深: " + this.depths));
            onParameterChange();
        }).bounds(leftX, y, colW, 20).build();
        this.addRenderableWidget(this.depthsButton);
        scrollEntries.add(new ScrollEntry(this.depthsButton, y));
        y += spacing;

        this.dougongButton = Button.builder(Component.literal("斗拱: " + getDougongLabel(this.dougongLv)), btn -> {
            this.dougongLv = (this.dougongLv + 1) % 3;
            btn.setMessage(Component.literal("斗拱: " + getDougongLabel(this.dougongLv)));
            onParameterChange();
        }).bounds(leftX, y, colW, 20).build();
        this.addRenderableWidget(this.dougongButton);
        scrollEntries.add(new ScrollEntry(this.dougongButton, y));
        y += spacing;

        this.midWidthSlider = new ParameterSlider(leftX, y, colW, 20, "明间宽", this.widthMid, 4.0f, 8.0f, val -> this.widthMid = val);
        this.addRenderableWidget(this.midWidthSlider);
        scrollEntries.add(new ScrollEntry(this.midWidthSlider, y));
        y += spacing;

        this.sideWidthSlider = new ParameterSlider(leftX, y, colW, 20, "次间宽", this.widthSide, 3.0f, 7.0f, val -> this.widthSide = val);
        this.sideWidthSlider.active = (this.bays == 5);
        this.addRenderableWidget(this.sideWidthSlider);
        scrollEntries.add(new ScrollEntry(this.sideWidthSlider, y));
        y += spacing;

        this.depthStepSlider = new ParameterSlider(leftX, y, colW, 20, "步架深", this.depthStep, 3.0f, 6.0f, val -> this.depthStep = val);
        this.addRenderableWidget(this.depthStepSlider);
        scrollEntries.add(new ScrollEntry(this.depthStepSlider, y));
        y += spacing;

        this.colHeightSlider = new ParameterSlider(leftX, y, colW, 20, "柱高", this.colHeight, 3.0f, 6.5f, val -> this.colHeight = val);
        this.addRenderableWidget(this.colHeightSlider);
        scrollEntries.add(new ScrollEntry(this.colHeightSlider, y));
        y += spacing;

        this.roofPitchSlider = new ParameterSlider(leftX, y, colW, 20, "屋面坡度", this.roofPitch, 0.45f, 0.85f, val -> this.roofPitch = val);
        this.addRenderableWidget(this.roofPitchSlider);
        scrollEntries.add(new ScrollEntry(this.roofPitchSlider, y));
        y += spacing;

        this.eavesLenSlider = new ParameterSlider(leftX, y, colW, 20, "檐出长", this.eavesLen, 1.0f, 2.5f, val -> this.eavesLen = val);
        this.addRenderableWidget(this.eavesLenSlider);
        scrollEntries.add(new ScrollEntry(this.eavesLenSlider, y));
        y += spacing;

        this.gableSetbackSlider = new ParameterSlider(leftX, y, colW, 20, "山花进深", this.gableSetback, 0.5f, 2.0f, val -> this.gableSetback = val);
        this.addRenderableWidget(this.gableSetbackSlider);
        scrollEntries.add(new ScrollEntry(this.gableSetbackSlider, y));
        y += spacing;

        this.cornerLiftSlider = new ParameterSlider(leftX, y, colW, 20, "角起翘", this.cornerLift, 0.0f, 1.5f, val -> this.cornerLift = val);
        this.addRenderableWidget(this.cornerLiftSlider);
        scrollEntries.add(new ScrollEntry(this.cornerLiftSlider, y));
        y += spacing;

        if (isBlockMode) {
            y += 10;
            this.confirmButton = Button.builder(Component.literal("应用修改"), btn -> {
                syncToBlock(false);
                this.onClose();
            }).bounds(leftX, y, colW, 20).build();
            this.addRenderableWidget(this.confirmButton);
            scrollEntries.add(new ScrollEntry(this.confirmButton, y));
            y += spacing;

            this.closeButton = Button.builder(Component.literal("退出"), btn -> {
                this.onClose();
            }).bounds(leftX, y, colW, 20).build();
            this.addRenderableWidget(this.closeButton);
            scrollEntries.add(new ScrollEntry(this.closeButton, y));
        } else {
            y += 14;
            
            this.presetNameInput = new EditBox(this.font, leftX, y, colW, 20, Component.literal("预设名称"));
            this.presetNameInput.setValue("新预设");
            this.addRenderableWidget(this.presetNameInput);
            scrollEntries.add(new ScrollEntry(this.presetNameInput, y));
            y += spacing;

            this.confirmButton = Button.builder(Component.literal("保存预设"), btn -> {
                saveAsPreset();
            }).bounds(leftX, y, colW, 20).build();
            this.addRenderableWidget(this.confirmButton);
            scrollEntries.add(new ScrollEntry(this.confirmButton, y));
            y += spacing;

            this.closeButton = Button.builder(Component.literal("取消"), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new PresetSelectionScreen(this.wandStack));
                }
            }).bounds(leftX, y, colW, 20).build();
            this.addRenderableWidget(this.closeButton);
            scrollEntries.add(new ScrollEntry(this.closeButton, y));
        }

        int ly = 35;

        Button toggleBase = Button.builder(Component.literal("台基: " + getToggleLabel(showBase)), btn -> { showBase = !showBase; btn.setMessage(Component.literal("台基: " + getToggleLabel(showBase))); onParameterChange(); }).bounds(leftX, ly, colW, 20).build();
        Button toggleColumns = Button.builder(Component.literal("立柱: " + getToggleLabel(showColumns)), btn -> { showColumns = !showColumns; btn.setMessage(Component.literal("立柱: " + getToggleLabel(showColumns))); onParameterChange(); }).bounds(leftX, ly + spacing, colW, 20).build();
        Button toggleDougong = Button.builder(Component.literal("斗拱: " + getToggleLabel(showDougong)), btn -> { showDougong = !showDougong; btn.setMessage(Component.literal("斗拱: " + getToggleLabel(showDougong))); onParameterChange(); }).bounds(leftX, ly + spacing * 2, colW, 20).build();
        Button toggleBeams = Button.builder(Component.literal("梁架: " + getToggleLabel(showBeams)), btn -> { showBeams = !showBeams; btn.setMessage(Component.literal("梁架: " + getToggleLabel(showBeams))); onParameterChange(); }).bounds(leftX, ly + spacing * 3, colW, 20).build();
        Button togglePurlins = Button.builder(Component.literal("檩条: " + getToggleLabel(showPurlins)), btn -> { showPurlins = !showPurlins; btn.setMessage(Component.literal("檩条: " + getToggleLabel(showPurlins))); onParameterChange(); }).bounds(leftX, ly + spacing * 4, colW, 20).build();
        Button toggleRafters = Button.builder(Component.literal("椽木: " + getToggleLabel(showRafters)), btn -> { showRafters = !showRafters; btn.setMessage(Component.literal("椽木: " + getToggleLabel(showRafters))); onParameterChange(); }).bounds(leftX, ly + spacing * 5, colW, 20).build();
        Button toggleRoof = Button.builder(Component.literal("屋面瓦: " + getToggleLabel(showRoof)), btn -> { showRoof = !showRoof; btn.setMessage(Component.literal("屋面瓦: " + getToggleLabel(showRoof))); onParameterChange(); }).bounds(leftX, ly + spacing * 6, colW, 20).build();
        Button toggleDebug = Button.builder(Component.literal("调试功能: " + getToggleLabel(ClientAccess.debugEnabled)), btn -> {
            ClientAccess.debugEnabled = !ClientAccess.debugEnabled;
            btn.setMessage(Component.literal("调试功能: " + getToggleLabel(ClientAccess.debugEnabled)));
            if (!ClientAccess.debugEnabled && com.example.cloudtectonics.math.BuildingDebugLogger.isLogging()) {
                com.example.cloudtectonics.math.BuildingDebugLogger.stopLoggingAndExport();
            }
        }).bounds(leftX, ly + spacing * 7, colW, 20).build();

        this.layerButtons.add(toggleBase);
        this.layerButtons.add(toggleColumns);
        this.layerButtons.add(toggleDougong);
        this.layerButtons.add(toggleBeams);
        this.layerButtons.add(togglePurlins);
        this.layerButtons.add(toggleRafters);
        this.layerButtons.add(toggleRoof);
        this.layerButtons.add(toggleDebug);

        for (int i = 0; i < this.layerButtons.size(); i++) {
            Button btn = this.layerButtons.get(i);
            this.addRenderableWidget(btn);
            scrollEntries.add(new ScrollEntry(btn, ly + spacing * i));
        }

        ly += spacing * this.layerButtons.size();

        if (isBlockMode) {
            ly += 10;
            this.confirmButtonTab1 = Button.builder(Component.literal("应用修改"), btn -> {
                syncToBlock(false);
                this.onClose();
            }).bounds(leftX, ly, colW, 20).build();
            this.addRenderableWidget(this.confirmButtonTab1);
            scrollEntries.add(new ScrollEntry(this.confirmButtonTab1, ly));
            ly += spacing;

            this.closeButtonTab1 = Button.builder(Component.literal("退出"), btn -> {
                this.onClose();
            }).bounds(leftX, ly, colW, 20).build();
            this.addRenderableWidget(this.closeButtonTab1);
            scrollEntries.add(new ScrollEntry(this.closeButtonTab1, ly));
        } else {
            ly += 10;
            this.confirmButtonTab1 = Button.builder(Component.literal("保存预设"), btn -> {
                saveAsPreset();
            }).bounds(leftX, ly, colW, 20).build();
            this.addRenderableWidget(this.confirmButtonTab1);
            scrollEntries.add(new ScrollEntry(this.confirmButtonTab1, ly));
            ly += spacing;

            this.closeButtonTab1 = Button.builder(Component.literal("取消"), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new PresetSelectionScreen(this.wandStack));
                }
            }).bounds(leftX, ly, colW, 20).build();
            this.addRenderableWidget(this.closeButtonTab1);
            scrollEntries.add(new ScrollEntry(this.closeButtonTab1, ly));
        }

        updateVisibility();
    }

    private void updateVisibility() {
        boolean showTab0 = (activeTab == 0);
        boolean showTab1 = (activeTab == 1);

        if (this.tabParamsButton != null) this.tabParamsButton.active = showTab1;
        if (this.tabLayersButton != null) this.tabLayersButton.active = showTab0;

        int startViewportY = 35;
        int endViewportY = this.height - 10;

        for (ScrollEntry entry : scrollEntries) {
            int newY = entry.originalY - scrollOffset;
            entry.widget.setY(newY);

            boolean isTabMatch = isWidgetForActiveTab(entry.widget);
            if (isTabMatch && newY >= startViewportY && newY + entry.widget.getHeight() <= endViewportY) {
                entry.widget.visible = true;
                entry.widget.active = true;
            } else {
                entry.widget.visible = false;
                entry.widget.active = false;
            }
        }
    }

    private boolean isWidgetForActiveTab(net.minecraft.client.gui.components.AbstractWidget widget) {
        if (widget == tabParamsButton || widget == tabLayersButton) {
            return true;
        }
        if (activeTab == 0) {
            return widget == baysButton || widget == depthsButton || widget == dougongButton ||
                   widget == midWidthSlider || widget == sideWidthSlider || widget == depthStepSlider ||
                   widget == colHeightSlider || widget == roofPitchSlider || widget == eavesLenSlider ||
                   widget == gableSetbackSlider || widget == cornerLiftSlider || widget == presetNameInput ||
                   widget == confirmButton || widget == closeButton;
        } else {
            return layerButtons.contains(widget) || widget == confirmButtonTab1 || widget == closeButtonTab1;
        }
    }

    private String getDougongLabel(int lv) {
        if (lv == 0) return "无";
        if (lv == 1) return "一斗三升";
        return "五踩斗拱";
    }

    private String getToggleLabel(boolean active) {
        return active ? "§a显示" : "§c隐藏";
    }

    private void onParameterChange() {
        rebuildPreview();
        if (isBlockMode && anchorBE != null) {
            this.anchorBE.updateParameters(
                    bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv, true,
                    showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase
            );
            BuildingAnchorRenderer.markDirty(this.anchorBE);
            syncToBlock(true);
        }
    }

    private void syncToBlock(boolean preview) {
        if (blockPos != null) {
            ModMessages.sendToServer(new ServerboundUpdateBuildingPacket(
                    blockPos, bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv, preview,
                    showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase
            ));
        }
    }

    private void saveAsPreset() {
        String name = this.presetNameInput.getValue().trim();
        if (name.isEmpty()) {
            name = "未命名预设";
        }
        
        Preset preset = new Preset(name, bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv);
        preset.showRoof = showRoof;
        preset.showRafters = showRafters;
        preset.showPurlins = showPurlins;
        preset.showBeams = showBeams;
        preset.showDougong = showDougong;
        preset.showColumns = showColumns;
        preset.showBase = showBase;

        PresetManager.addPreset(preset);

        if (this.minecraft != null) {
            this.minecraft.setScreen(new PresetSelectionScreen(this.wandStack));
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pMouseX > 165) {
            this.yaw += pDragX * 0.8f;
            this.pitch += pDragY * 0.8f;
            this.pitch = Math.max(-89.0f, Math.min(89.0f, this.pitch));
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pMouseX > 165) {
            this.zoom += pDelta * 0.8f;
            this.zoom = Math.max(2.0f, Math.min(50.0f, this.zoom));
            return true;
        } else {
            int totalHeight = (activeTab == 0) ? (isBlockMode ? 325 : 360) : 235;
            int maxScroll = Math.max(0, totalHeight - (this.height - 45));
            this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int)(pDelta * 12)));
            updateVisibility();
            return true;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        updateVisibility();

        this.renderBackground(pGuiGraphics);

        pGuiGraphics.fill(0, 0, 160, this.height, 0xDD1E1E26);
        pGuiGraphics.vLine(160, -1, this.height + 1, 0xFF444454);

        int pLeft = 165;
        int pTop = 10;
        int pRight = this.width - 10;
        int pBottom = this.height - 10;
        
        pGuiGraphics.fillGradient(pLeft, pTop, pRight, pBottom, 0xEE111524, 0xEE1A2238);
        pGuiGraphics.renderOutline(pLeft, pTop, pRight - pLeft, pBottom - pTop, 0xFF4C5878);

        pGuiGraphics.drawString(this.font, this.title, pLeft + 10, pTop + 10, 0xFFFFFF, false);

        int startViewportY = 35;
        int endViewportY = this.height - 10;
        if (activeTab == 0 && !isBlockMode) {
            int textY = 277 - scrollOffset;
            if (textY >= startViewportY && textY + 8 <= endViewportY) {
                pGuiGraphics.drawString(this.font, "输入预设名保存:", 10, textY, 0xAAAAAA, false);
            }
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        // 渲染 3D 预览模型
        render3DModel(pLeft, pTop, pRight - pLeft, pBottom - pTop);

        // 绘制 2D 水平比例尺图例
        draw2DScaleLegend(pGuiGraphics, pRight - 80, pBottom - 20);
    }

    private int leftTabTitleX() {
        return 10;
    }

    private String getTexturePath(StructureComponent comp) {
        if ("tile".equals(comp.texture) || comp.color == 0xFF707A80) { // Slate grey
            return "block/deepslate_tiles";
        }
        if (comp.color == 0xFF9A2A22) { // Vermilion
            return "block/stripped_mangrove_log";
        }
        if (comp.color == 0xFFB58450 || comp.color == 0xFFD4A76A) { // Natural wood, corner beams
            return "block/stripped_spruce_log";
        }
        if (comp.color == 0xFFCDA234 || comp.color == 0xFFD4AF37) { // Gold accents
            return "block/gold_block";
        }
        if (comp.color == 0xFF7B828A) { // Stone base
            return "block/stone_bricks";
        }
        if (comp.color == 0xFFBF5D38) { // Gable wall
            return "block/red_terracotta";
        }
        return "block/white_concrete";
    }

    private void render3DModel(int x, int y, int width, int height) {
        PoseStack poseStack = new PoseStack();
        
        // 设定裁剪区域，防止 3D 模型越出视口
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor((int)(x * scale), (int)((this.height - y - height) * scale), (int)(width * scale), (int)(height * scale));

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        poseStack.pushPose();
        // 移动到视口正中心
        poseStack.translate(x + width / 2.0f, y + height / 2.0f, 200.0f);
        // 放大倍数与3D世界Y轴倒转（GUI Y轴朝下，3D Y轴朝上）
        poseStack.scale(zoom, -zoom, zoom);

        // 应用拖拽的旋转角
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        // 1. 渲染参考三维坐标网格 (Grid)
        renderGrid(poseStack);

        // 2. 渲染 3D 比例尺 (Red Bar of 5 meters)
        render3DScaleBar(poseStack);

        // 3. 渲染所有古建结构构件
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getId());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (StructureComponent comp : this.previewComponents) {
            poseStack.pushPose();
            poseStack.translate(comp.localPos.x, comp.localPos.y, comp.localPos.z);
            poseStack.mulPose(comp.rotation);
            float inflate = 0.005f;
            poseStack.scale(comp.size.x + inflate, comp.size.y + inflate, comp.size.z + inflate);

            String texPath = getTexturePath(comp);
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
            float a = ((color >> 24) & 0xFF) / 255.0f;

            // 六个面，对齐纹理平铺 UV
            drawCube(poseStack, builder, r, g, b, a, 0, u0, v0, u1, v1, comp.size);   // Front
            drawCube(poseStack, builder, r, g, b, a, 1, u0, v0, u1, v1, comp.size);   // Back
            drawCube(poseStack, builder, r, g, b, a, 2, u0, v0, u1, v1, comp.size);   // Left
            drawCube(poseStack, builder, r, g, b, a, 3, u0, v0, u1, v1, comp.size);   // Right
            drawCube(poseStack, builder, r, g, b, a, 4, u0, v0, u1, v1, comp.size);   // Top
            drawCube(poseStack, builder, r, g, b, a, 5, u0, v0, u1, v1, comp.size);   // Bottom

            poseStack.popPose();
        }

        tesselator.end();
        poseStack.popPose();

        RenderSystem.disableScissor();
        RenderSystem.disableDepthTest();
    }

    private void renderGrid(PoseStack poseStack) {
        // 根据建筑包围盒动态计算网格大小
        float totalW = widthMid + (bays - 1) * widthSide;
        float totalD = depths * depthStep;
        float halfW = (totalW / 2.0f) + 2.0f;
        float halfD = (totalD / 2.0f) + 2.0f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        // 渲染辅助网格线
        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = poseStack.last().pose();

        int gridColorR = 100, gridColorG = 130, gridColorB = 180, gridColorA = 120;

        for (float x = -halfW; x <= halfW; x += 1.0f) {
            builder.vertex(mat, x, 0.0f, -halfD).color(gridColorR, gridColorG, gridColorB, gridColorA).endVertex();
            builder.vertex(mat, x, 0.0f, halfD).color(gridColorR, gridColorG, gridColorB, gridColorA).endVertex();
        }
        for (float z = -halfD; z <= halfD; z += 1.0f) {
            builder.vertex(mat, -halfW, 0.0f, z).color(gridColorR, gridColorG, gridColorB, gridColorA).endVertex();
            builder.vertex(mat, halfW, 0.0f, z).color(gridColorR, gridColorG, gridColorB, gridColorA).endVertex();
        }

        tesselator.end();
    }

    private void render3DScaleBar(PoseStack poseStack) {
        // 在网格角落绘制一个长 5.0f 宽 0.15f 高 0.15f 的红色 3D 标尺棒，表示 5 米
        float totalW = widthMid + (bays - 1) * widthSide;
        float totalD = depths * depthStep;
        float halfW = (totalW / 2.0f) + 1.0f;
        float halfD = (totalD / 2.0f) + 1.0f;

        poseStack.pushPose();
        // 放置在右前角
        poseStack.translate(-halfW, 0.075f, halfD);
        poseStack.scale(5.0f, 0.15f, 0.15f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getId());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        net.minecraft.client.renderer.texture.TextureAtlasSprite whiteSprite = 
            Minecraft.getInstance()
                .getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
                .apply(new net.minecraft.resources.ResourceLocation("minecraft", "block/white_concrete"));
        
        float u0 = whiteSprite.getU0(), v0 = whiteSprite.getV0();
        float u1 = whiteSprite.getU1(), v1 = whiteSprite.getV1();

        // 红色比例尺棒
        float r = 1.0f, g = 0.2f, b = 0.2f, a = 1.0f;
        Vector3f size = new Vector3f(1.0f, 1.0f, 1.0f);
        drawCube(poseStack, builder, r, g, b, a, 0, u0, v0, u1, v1, size);
        drawCube(poseStack, builder, r, g, b, a, 1, u0, v0, u1, v1, size);
        drawCube(poseStack, builder, r, g, b, a, 2, u0, v0, u1, v1, size);
        drawCube(poseStack, builder, r, g, b, a, 3, u0, v0, u1, v1, size);
        drawCube(poseStack, builder, r, g, b, a, 4, u0, v0, u1, v1, size);
        drawCube(poseStack, builder, r, g, b, a, 5, u0, v0, u1, v1, size);

        tesselator.end();
        poseStack.popPose();
    }

    private void draw2DScaleLegend(GuiGraphics graphics, int rx, int ry) {
        // 在视口右下角绘制静态的比例尺图例说明
        graphics.fill(rx - 4, ry - 14, rx + 74, ry + 10, 0x77000000);
        graphics.renderOutline(rx - 4, ry - 14, 78, 24, 0xAA6688AA);
        graphics.drawString(this.font, "红色标尺: 5米", rx, ry - 10, 0xFFFFFF, false);
        graphics.drawString(this.font, "Ruler: 5m", rx, ry, 0xAAAAAA, false);
    }

    private void drawCube(PoseStack poseStack, VertexConsumer consumer, float r, float g, float b, float a, int faceIndex, float u0, float v0, float u1, float v1, Vector3f size) {
        Matrix4f matrix = poseStack.last().pose();

        // 计算世界空间的法线以做漫反射明暗计算
        float nx = 0, ny = 0, nz = 0;
        switch (faceIndex) {
            case 0: nz = 1; break;
            case 1: nz = -1; break;
            case 2: nx = -1; break;
            case 3: nx = 1; break;
            case 4: ny = 1; break;
            case 5: ny = -1; break;
        }

        Vector3f rotNorm = new Vector3f(nx, ny, nz);
        rotNorm.rotateX((float) Math.toRadians(pitch));
        rotNorm.rotateY((float) Math.toRadians(yaw));

        float diffuse = 0.6f;
        if (rotNorm.y() > 0.05f) {
            diffuse = 0.85f + 0.15f * rotNorm.y(); // 顶部最亮
        } else if (rotNorm.y() < -0.05f) {
            diffuse = 0.45f + 0.05f * rotNorm.y(); // 底部最暗
        } else {
            diffuse = 0.6f + 0.25f * Math.abs(rotNorm.z()); // 侧面折中
        }

        int finalR = Math.max(0, Math.min(255, (int) (r * diffuse * 255)));
        int finalG = Math.max(0, Math.min(255, (int) (g * diffuse * 255)));
        int finalB = Math.max(0, Math.min(255, (int) (b * diffuse * 255)));
        int finalA = (int) (a * 255);

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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 自定义范围滑块控件
     */
    private class ParameterSlider extends AbstractSliderButton {
        private final float min;
        private final float max;
        private final String label;
        private final Consumer<Float> setter;

        public ParameterSlider(int x, int y, int width, int height, String label, float current, float min, float max, Consumer<Float> setter) {
            super(x, y, width, height, Component.literal(label + ": " + String.format("%.2f", current)), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
        }

        public void setValue(float val) {
            this.value = (val - min) / (max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            float val = getValue();
            this.setMessage(Component.literal(label + ": " + String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            setter.accept(getValue());
            onParameterChange();
        }

        public float getValue() {
            return min + (float) this.value * (max - min);
        }
    }
}
