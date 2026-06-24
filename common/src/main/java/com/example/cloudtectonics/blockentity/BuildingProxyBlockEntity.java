package com.example.cloudtectonics.blockentity;

import com.example.cloudtectonics.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BuildingProxyBlockEntity extends BlockEntity {

    private BlockPos anchorPos;
    private VoxelShape cachedShape = null;
    protected String texturePath = null;

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
                requestModelDataUpdatePlatform();
            } else {
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
    }

    protected void requestModelDataUpdatePlatform() {
        // Platform specific implementation
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setAnchorPos(BlockPos pos) {
        this.anchorPos = pos;
        this.cachedShape = null;
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

    // Forge-specific networking hook (compiles in common by omitting @Override and super call)
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        net.minecraft.nbt.CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag);
        }
    }
}
