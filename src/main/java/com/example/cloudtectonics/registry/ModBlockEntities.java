package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组方块实体注册类
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModBlocks.MODID);

    public static final RegistryObject<BlockEntityType<BuildingAnchorBlockEntity>> BUILDING_ANCHOR_BE =
            BLOCK_ENTITIES.register("building_anchor_be", () ->
                    BlockEntityType.Builder.of(BuildingAnchorBlockEntity::new, ModBlocks.BUILDING_ANCHOR.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<BuildingProxyBlockEntity>> BUILDING_PROXY_BE =
            BLOCK_ENTITIES.register("building_proxy_be", () ->
                    BlockEntityType.Builder.of(BuildingProxyBlockEntity::new, ModBlocks.BUILDING_PROXY.get())
                            .build(null));
}
