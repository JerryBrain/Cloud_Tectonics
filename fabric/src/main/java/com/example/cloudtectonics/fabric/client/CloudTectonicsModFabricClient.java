package com.example.cloudtectonics.fabric.client;

import com.example.cloudtectonics.registry.ModBlockEntities;
import com.example.cloudtectonics.client.BuildingAnchorRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public class CloudTectonicsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register BlockEntity Renderer
        BlockEntityRenderers.register(ModBlockEntities.BUILDING_ANCHOR_BE.get(), BuildingAnchorRenderer::new);

        // Initialize Fabric-specific client events
        FabricClientEvents.init();
    }
}
