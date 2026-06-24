package com.example.cloudtectonics.blockentity;

import com.example.cloudtectonics.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * 代理方块实体。
 * 主要作用是存储其对应的主控制方块（Anchor Block）的坐标。
 */
public class BuildingProxyBlockEntity extends BlockEntity {

    public static final ModelProperty<String> TEXTURE_PROPERTY = new ModelProperty<>();

    private BlockPos anchorPos;
    private VoxelShape cachedShape = null;
    private String texturePath = null;

    public BuildingProxyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.BUILDING_PROXY_BE.get(), pPos, pBlockState);
    }

    public VoxelShape getCachedShape() {
        return cachedShape;
    }

    public void setCachedShape(VoxelShape cachedShape) {
        this.cachedShape = cachedShape;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
        setChanged();
        if (level != null) {
            if (level.isClientSide()) {
                requestModelDataUpdate();
            } else {
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
    }

    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
            .with(TEXTURE_PROPERTY, texturePath != null ? texturePath : "block/white_concrete")
            .build();
    }

    public void setAnchorPos(BlockPos pos) {
        this.anchorPos = pos;
        this.cachedShape = null; // 重置缓存
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (anchorPos != null) {
            pTag.putInt("anchorX", anchorPos.getX());
            pTag.putInt("anchorY", anchorPos.getY());
            pTag.putInt("anchorZ", anchorPos.getZ());
        }
        if (texturePath != null) {
            pTag.putString("texturePath", texturePath);
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("anchorX")) {
            this.anchorPos = new BlockPos(
                pTag.getInt("anchorX"),
                pTag.getInt("anchorY"),
                pTag.getInt("anchorZ")
            );
        }
        if (pTag.contains("texturePath")) {
            this.texturePath = pTag.getString("texturePath");
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (pkt.getTag() != null) {
            this.load(pkt.getTag());
        }
    }
}
