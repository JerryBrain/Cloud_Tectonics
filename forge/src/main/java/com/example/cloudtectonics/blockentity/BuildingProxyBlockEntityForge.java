package com.example.cloudtectonics.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class BuildingProxyBlockEntityForge extends BuildingProxyBlockEntity {

    public static final ModelProperty<String> TEXTURE_PROPERTY = new ModelProperty<>();

    public BuildingProxyBlockEntityForge(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    @Override
    protected void requestModelDataUpdatePlatform() {
        requestModelDataUpdate();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
            .with(TEXTURE_PROPERTY, texturePath != null ? texturePath : "block/white_concrete")
            .build();
    }
}
