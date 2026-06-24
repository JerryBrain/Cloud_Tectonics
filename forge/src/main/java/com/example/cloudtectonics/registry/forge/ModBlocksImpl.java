package com.example.cloudtectonics.registry.forge;

import com.example.cloudtectonics.block.ForgeBuildingProxyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocksImpl {
    public static Block createProxyBlock(BlockBehaviour.Properties properties) {
        return new ForgeBuildingProxyBlock(properties);
    }
}
