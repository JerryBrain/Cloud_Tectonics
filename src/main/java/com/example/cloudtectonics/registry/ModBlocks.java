package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.block.BuildingAnchorBlock;
import com.example.cloudtectonics.block.BuildingProxyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组方块注册类
 */
public class ModBlocks {
    public static final String MODID = "cloudtectonics";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> BUILDING_ANCHOR = BLOCKS.register("building_anchor",
            () -> new BuildingAnchorBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> BUILDING_PROXY = BLOCKS.register("building_proxy",
            () -> new BuildingProxyBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noLootTable()));
}
