package com.example.cloudtectonics.registry.fabric;

import com.example.cloudtectonics.block.BuildingProxyBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocksImpl {
    public static Block createProxyBlock(BlockBehaviour.Properties properties) {
        return new BuildingProxyBlock(properties);
    }
}
