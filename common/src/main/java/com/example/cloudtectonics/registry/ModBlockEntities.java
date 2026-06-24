package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.CloudTectonicsMod;
import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(CloudTectonicsMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<BuildingAnchorBlockEntity>> BUILDING_ANCHOR_BE =
            BLOCK_ENTITIES.register("building_anchor_be", () ->
                    BlockEntityType.Builder.of(BuildingAnchorBlockEntity::new, ModBlocks.BUILDING_ANCHOR.get())
                            .build(null));

    public static final RegistrySupplier<BlockEntityType<BuildingProxyBlockEntity>> BUILDING_PROXY_BE =
            BLOCK_ENTITIES.register("building_proxy_be", () ->
                    BlockEntityType.Builder.of(getProxyBlockEntitySupplier(), ModBlocks.BUILDING_PROXY.get())
                            .build(null));

    public static void init() {
        BLOCK_ENTITIES.register();
    }

    @ExpectPlatform
    public static BlockEntityType.BlockEntitySupplier<BuildingProxyBlockEntity> getProxyBlockEntitySupplier() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockEntity createProxyBlockEntity(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }
}
