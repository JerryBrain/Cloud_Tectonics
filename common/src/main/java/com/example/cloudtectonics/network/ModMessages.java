package com.example.cloudtectonics.network;

import com.example.cloudtectonics.CloudTectonicsMod;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(new ResourceLocation(CloudTectonicsMod.MOD_ID, "messages"));

    public static void register() {
        CHANNEL.register(
                ServerboundUpdateBuildingPacket.class,
                ServerboundUpdateBuildingPacket::encode,
                ServerboundUpdateBuildingPacket::new,
                ServerboundUpdateBuildingPacket::handle
        );

        CHANNEL.register(
                ServerboundPlaceBuildingPacket.class,
                ServerboundPlaceBuildingPacket::encode,
                ServerboundPlaceBuildingPacket::new,
                ServerboundPlaceBuildingPacket::handle
        );

        CHANNEL.register(
                ServerboundUpdateWandNBTPacket.class,
                ServerboundUpdateWandNBTPacket::encode,
                ServerboundUpdateWandNBTPacket::new,
                ServerboundUpdateWandNBTPacket::handle
        );
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
}
