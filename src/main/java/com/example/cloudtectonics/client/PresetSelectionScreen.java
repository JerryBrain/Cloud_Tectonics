package com.example.cloudtectonics.client;

import com.example.cloudtectonics.client.PresetManager.Preset;
import com.example.cloudtectonics.network.ModMessages;
import com.example.cloudtectonics.network.ServerboundUpdateWandNBTPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PresetSelectionScreen extends Screen {
    private final ItemStack wandStack;
    private int scrollOffset = 0;
    private Button designButton;
    private Button closeButton;

    public PresetSelectionScreen(ItemStack wandStack) {
        super(Component.literal("选择建筑预设 (Select Preset)"));
        this.wandStack = wandStack;
    }

    @Override
    protected void init() {
        super.init();
        PresetManager.loadPresets(); // 刷新预设列表

        int panelW = 280;
        int startX = (this.width - panelW) / 2;
        int startY = 40;

        List<Preset> presets = PresetManager.getPresets();
        int y = startY;

        for (int i = 0; i < presets.size(); i++) {
            final Preset preset = presets.get(i);
            // 预设选择按钮
            Component btnText = Component.literal(preset.name + " (" + preset.bays + "开间, " + preset.depths + "进深)");
            
            Button selectBtn = Button.builder(btnText, btn -> {
                selectAndClose(preset);
            }).bounds(startX + 10, y, 200, 20).build();
            this.addRenderableWidget(selectBtn);

            // 删除按钮（仅允许删除非默认预设，或者只要不是前四个默认名称就可以删）
            boolean isDefault = isDefaultPreset(preset.name);
            if (!isDefault) {
                Button delBtn = Button.builder(Component.literal("§c删除"), btn -> {
                    PresetManager.deletePreset(preset.name);
                    this.rebuildWidgets(); // 重建UI以刷新列表
                }).bounds(startX + 215, y, 45, 20).build();
                this.addRenderableWidget(delBtn);
            }

            y += 24;
        }

        // 底部设计与关闭按钮
        int bottomY = this.height - 35;
        this.designButton = Button.builder(Component.literal("§a设计新预设 (Design New)"), btn -> {
            if (this.minecraft != null) {
                // 打开营造编辑器，传入 null 表示 Preset 模式，并且传入 wandStack 以便保存
                this.minecraft.setScreen(new BuildingConfigurationScreen(wandStack));
            }
        }).bounds(this.width / 2 - 130, bottomY, 120, 20).build();
        this.addRenderableWidget(this.designButton);

        this.closeButton = Button.builder(Component.literal("返回 (Cancel)"), btn -> {
            this.onClose();
        }).bounds(this.width / 2 + 10, bottomY, 120, 20).build();
        this.addRenderableWidget(this.closeButton);
    }

    private boolean isDefaultPreset(String name) {
        return "园林歇山亭".equals(name) || "三开间小殿".equals(name) || "五开间大殿".equals(name) || "高耸楼阁层".equals(name);
    }

    private void selectAndClose(Preset preset) {
        CompoundTag activePresetTag = new CompoundTag();
        activePresetTag.putString("Name", preset.name);
        activePresetTag.putInt("bays", preset.bays);
        activePresetTag.putInt("depths", preset.depths);
        activePresetTag.putFloat("widthMid", preset.widthMid);
        activePresetTag.putFloat("widthSide", preset.widthSide);
        activePresetTag.putFloat("depthStep", preset.depthStep);
        activePresetTag.putFloat("colHeight", preset.colHeight);
        activePresetTag.putFloat("roofPitch", preset.roofPitch);
        activePresetTag.putFloat("eavesLen", preset.eavesLen);
        activePresetTag.putFloat("gableSetback", preset.gableSetback);
        activePresetTag.putFloat("cornerLift", preset.cornerLift);
        activePresetTag.putInt("dougongLv", preset.dougongLv);

        activePresetTag.putBoolean("showRoof", preset.showRoof);
        activePresetTag.putBoolean("showRafters", preset.showRafters);
        activePresetTag.putBoolean("showPurlins", preset.showPurlins);
        activePresetTag.putBoolean("showBeams", preset.showBeams);
        activePresetTag.putBoolean("showDougong", preset.showDougong);
        activePresetTag.putBoolean("showColumns", preset.showColumns);
        activePresetTag.putBoolean("showBase", preset.showBase);
        
        // 默认旋转角度为 0
        activePresetTag.putInt("Rotation", 0);

        // 更新客户端的 NBT
        this.wandStack.getOrCreateTag().put("ActivePreset", activePresetTag);
        
        // 发送包同步至服务端
        ModMessages.sendToServer(new ServerboundUpdateWandNBTPacket(activePresetTag));

        this.onClose();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        // 绘制提示文字
        pGuiGraphics.drawCenteredString(this.font, "§7选择一个预设进行放置，握持建筑棒并右击地面建造。Shift+右击可再次唤出此菜单。", this.width / 2, 28, 0xAAAAAA);
        
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
