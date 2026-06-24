package com.example.cloudtectonics.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundUpdateWandNBTPacket {
    private final CompoundTag tag;

    public ServerboundUpdateWandNBTPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public ServerboundUpdateWandNBTPacket(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
                    stack.getOrCreateTag().put("ActivePreset", tag);
                } else {
                    stack = player.getItemInHand(InteractionHand.OFF_HAND);
                    if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
                        stack.getOrCreateTag().put("ActivePreset", tag);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
