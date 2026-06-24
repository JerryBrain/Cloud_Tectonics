package com.example.cloudtectonics.block;

import com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ForgeBuildingProxyBlock extends BuildingProxyBlock {

    public ForgeBuildingProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BuildingProxyBlockEntity proxy) {
            String tex = proxy.getTexturePath();
            if (tex != null) {
                if (tex.contains("wood") || tex.contains("log") || tex.contains("planks")) {
                    return SoundType.WOOD;
                } else if (tex.contains("stone") || tex.contains("brick") || tex.contains("deepslate") || tex.contains("terracotta")) {
                    return SoundType.STONE;
                } else if (tex.contains("gold") || tex.contains("metal") || tex.contains("iron")) {
                    return SoundType.METAL;
                }
            }
        }
        return super.getSoundType(state, level, pos, entity);
    }
}
