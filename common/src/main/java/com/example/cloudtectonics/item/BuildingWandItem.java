package com.example.cloudtectonics.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class BuildingWandItem extends Item {

    public BuildingWandItem(Properties pProperties) {
        super(pProperties.stacksTo(1)); // 建筑棒堆叠数量为1
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        
        if (pLevel.isClientSide()) {
            // 客户端逻辑
            boolean hasPreset = stack.hasTag() && stack.getTag().contains("ActivePreset");
            boolean sneak = pPlayer.isShiftKeyDown();
            
            if (sneak || !hasPreset) {
                // 打开预设选择界面
                net.minecraft.client.Minecraft.getInstance().setScreen(
                        new com.example.cloudtectonics.client.PresetSelectionScreen(stack)
                );
                return InteractionResultHolder.success(stack);
            } else {
                // 在放置模式下，右击地面进行建造
                net.minecraft.world.phys.BlockHitResult hit = customRaycast(pPlayer, 32.0);
                if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    net.minecraft.core.BlockPos placePos = hit.getBlockPos().relative(hit.getDirection());
                    // 发送建造数据包给服务端
                    int rotation = stack.getTag().getCompound("ActivePreset").getInt("Rotation");
                    com.google.gson.JsonObject presetJson = new com.google.gson.JsonObject();
                    net.minecraft.nbt.CompoundTag tag = stack.getTag().getCompound("ActivePreset");
                    
                    com.example.cloudtectonics.network.ModMessages.sendToServer(
                            new com.example.cloudtectonics.network.ServerboundPlaceBuildingPacket(
                                    placePos, rotation,
                                    tag.getInt("bays"), tag.getInt("depths"),
                                    tag.getFloat("widthMid"), tag.getFloat("widthSide"), tag.getFloat("depthStep"),
                                    tag.getFloat("colHeight"), tag.getFloat("roofPitch"), tag.getFloat("eavesLen"),
                                    tag.getFloat("gableSetback"), tag.getFloat("cornerLift"), tag.getInt("dougongLv"),
                                    tag.getBoolean("showRoof"), tag.getBoolean("showRafters"), tag.getBoolean("showPurlins"),
                                    tag.getBoolean("showBeams"), tag.getBoolean("showDougong"), tag.getBoolean("showColumns"),
                                    tag.getBoolean("showBase")
                            )
                    );
                    pPlayer.displayClientMessage(Component.literal("开始建造结构..."), true);
                }
                return InteractionResultHolder.success(stack);
            }
        }
        
        return InteractionResultHolder.pass(stack);
    }

    public static net.minecraft.world.phys.BlockHitResult customRaycast(Player player, double distance) {
        net.minecraft.world.phys.Vec3 eyePosition = player.getEyePosition(1.0f);
        net.minecraft.world.phys.Vec3 lookVector = player.getViewVector(1.0f);
        net.minecraft.world.phys.Vec3 endPosition = eyePosition.add(lookVector.x * distance, lookVector.y * distance, lookVector.z * distance);
        return player.level().clip(new net.minecraft.world.level.ClipContext(
                eyePosition,
                endPosition,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (pStack.hasTag() && pStack.getTag().contains("ActivePreset")) {
            String name = pStack.getTag().getCompound("ActivePreset").getString("Name");
            int rotation = pStack.getTag().getCompound("ActivePreset").getInt("Rotation");
            pTooltipComponents.add(Component.literal("§a当前预设: " + name));
            pTooltipComponents.add(Component.literal("§e旋转角度: " + rotation + "°"));
            pTooltipComponents.add(Component.literal("§7[右击] 放置建筑"));
            pTooltipComponents.add(Component.literal("§7[滚轮] 旋转朝向"));
            pTooltipComponents.add(Component.literal("§7[左击] 退出建造模式 (不破坏方块)"));
            pTooltipComponents.add(Component.literal("§7[Shift+右击] 重新进入编辑界面"));
        } else {
            pTooltipComponents.add(Component.literal("§c建造模式已关闭 (当前无预设)"));
            pTooltipComponents.add(Component.literal("§7[右击任意位置/空气] 进入编辑界面"));
        }
    }
}
