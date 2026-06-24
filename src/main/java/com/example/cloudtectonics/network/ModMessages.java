package com.example.cloudtectonics.network;

import com.example.cloudtectonics.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int nextID() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ModBlocks.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = channel;

        channel.messageBuilder(ServerboundUpdateBuildingPacket.class, nextID(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUpdateBuildingPacket::encode)
                .decoder(ServerboundUpdateBuildingPacket::new)
                .consumerMainThread(ServerboundUpdateBuildingPacket::handle)
                .add();

        channel.messageBuilder(ServerboundPlaceBuildingPacket.class, nextID(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundPlaceBuildingPacket::encode)
                .decoder(ServerboundPlaceBuildingPacket::new)
                .consumerMainThread(ServerboundPlaceBuildingPacket::handle)
                .add();

        channel.messageBuilder(ServerboundUpdateWandNBTPacket.class, nextID(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundUpdateWandNBTPacket::encode)
                .decoder(ServerboundUpdateWandNBTPacket::new)
                .consumerMainThread(ServerboundUpdateWandNBTPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        if (INSTANCE != null) {
            INSTANCE.sendToServer(message);
        }
    }
}
