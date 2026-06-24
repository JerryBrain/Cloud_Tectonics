package com.example.cloudtectonics.registry.fabric;

import com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModBlockEntitiesImpl {
    public static BlockEntityType.BlockEntitySupplier<BuildingProxyBlockEntity> getProxyBlockEntitySupplier() {
        return BuildingProxyBlockEntity::new;
    }

    public static BlockEntity createProxyBlockEntity(BlockPos pos, BlockState state) {
        return new BuildingProxyBlockEntity(pos, state);
    }
}
