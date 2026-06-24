package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.CloudTectonicsMod;
import com.example.cloudtectonics.block.BuildingAnchorBlock;
import com.example.cloudtectonics.block.BuildingProxyBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(CloudTectonicsMod.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> BUILDING_ANCHOR = BLOCKS.register("building_anchor",
            () -> new BuildingAnchorBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistrySupplier<Block> BUILDING_PROXY = BLOCKS.register("building_proxy",
            () -> createProxyBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noLootTable()));

    @dev.architectury.injectables.annotations.ExpectPlatform
    public static Block createProxyBlock(BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }

    public static void init() {
        BLOCKS.register();
    }
}
