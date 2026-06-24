package com.example.cloudtectonics.client;

import com.example.cloudtectonics.registry.ModBlockEntities;
import com.example.cloudtectonics.registry.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModBlocks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册主控制方块的自定义渲染器
        event.registerBlockEntityRenderer(ModBlockEntities.BUILDING_ANCHOR_BE.get(), BuildingAnchorRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientForgeEvents.DEBUG_KEY);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event) {
        java.util.Map<net.minecraft.resources.ResourceLocation, net.minecraft.client.resources.model.BakedModel> models = event.getModels();
        for (net.minecraft.resources.ResourceLocation loc : new java.util.ArrayList<>(models.keySet())) {
            if (loc.getNamespace().equals(ModBlocks.MODID) && (loc.getPath().equals("building_proxy") || loc.getPath().equals("block/building_proxy"))) {
                net.minecraft.client.resources.model.BakedModel original = models.get(loc);
                if (original != null && !(original instanceof ProxyBlockBakedModel)) {
                    models.put(loc, new ProxyBlockBakedModel(original));
                }
            }
        }
    }
}
