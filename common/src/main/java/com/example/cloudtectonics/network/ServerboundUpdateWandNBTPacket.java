package com.example.cloudtectonics.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

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

    public static void handle(ServerboundUpdateWandNBTPacket message, Supplier<NetworkManager.PacketContext> contextSupplier) {
        NetworkManager.PacketContext context = contextSupplier.get();
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player != null) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
                    stack.getOrCreateTag().put("ActivePreset", message.tag);
                } else {
                    stack = player.getItemInHand(InteractionHand.OFF_HAND);
                    if (stack.getItem() instanceof com.example.cloudtectonics.item.BuildingWandItem) {
                        stack.getOrCreateTag().put("ActivePreset", message.tag);
                    }
                }
            }
        });
    }
}
