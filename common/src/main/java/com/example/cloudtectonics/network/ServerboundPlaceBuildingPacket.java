package com.example.cloudtectonics.network;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.registry.ModBlocks;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.function.Supplier;

public class ServerboundPlaceBuildingPacket {
    private final BlockPos pos;
    private final int rotationYaw;
    private final int bays;
    private final int depths;
    private final float widthMid;
    private final float widthSide;
    private final float depthStep;
    private final float colHeight;
    private final float roofPitch;
    private final float eavesLen;
    private final float gableSetback;
    private final float cornerLift;
    private final int dougongLv;

    private final boolean showRoof;
    private final boolean showRafters;
    private final boolean showPurlins;
    private final boolean showBeams;
    private final boolean showDougong;
    private final boolean showColumns;
    private final boolean showBase;

    public ServerboundPlaceBuildingPacket(BlockPos pos, int rotationYaw, int bays, int depths, float widthMid, float widthSide,
                                          float depthStep, float colHeight, float roofPitch, float eavesLen,
                                          float gableSetback, float cornerLift, int dougongLv,
                                          boolean showRoof, boolean showRafters, boolean showPurlins,
                                          boolean showBeams, boolean showDougong, boolean showColumns, boolean showBase) {
        this.pos = pos;
        this.rotationYaw = rotationYaw;
        this.bays = bays;
        this.depths = depths;
        this.widthMid = widthMid;
        this.widthSide = widthSide;
        this.depthStep = depthStep;
        this.colHeight = colHeight;
        this.roofPitch = roofPitch;
        this.eavesLen = eavesLen;
        this.gableSetback = gableSetback;
        this.cornerLift = cornerLift;
        this.dougongLv = dougongLv;
        this.showRoof = showRoof;
        this.showRafters = showRafters;
        this.showPurlins = showPurlins;
        this.showBeams = showBeams;
        this.showDougong = showDougong;
        this.showColumns = showColumns;
        this.showBase = showBase;
    }

    public ServerboundPlaceBuildingPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.rotationYaw = buf.readInt();
        this.bays = buf.readInt();
        this.depths = buf.readInt();
        this.widthMid = buf.readFloat();
        this.widthSide = buf.readFloat();
        this.depthStep = buf.readFloat();
        this.colHeight = buf.readFloat();
        this.roofPitch = buf.readFloat();
        this.eavesLen = buf.readFloat();
        this.gableSetback = buf.readFloat();
        this.cornerLift = buf.readFloat();
        this.dougongLv = buf.readInt();
        this.showRoof = buf.readBoolean();
        this.showRafters = buf.readBoolean();
        this.showPurlins = buf.readBoolean();
        this.showBeams = buf.readBoolean();
        this.showDougong = buf.readBoolean();
        this.showColumns = buf.readBoolean();
        this.showBase = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(rotationYaw);
        buf.writeInt(bays);
        buf.writeInt(depths);
        buf.writeFloat(widthMid);
        buf.writeFloat(widthSide);
        buf.writeFloat(depthStep);
        buf.writeFloat(colHeight);
        buf.writeFloat(roofPitch);
        buf.writeFloat(eavesLen);
        buf.writeFloat(gableSetback);
        buf.writeFloat(cornerLift);
        buf.writeInt(dougongLv);
        buf.writeBoolean(showRoof);
        buf.writeBoolean(showRafters);
        buf.writeBoolean(showPurlins);
        buf.writeBoolean(showBeams);
        buf.writeBoolean(showDougong);
        buf.writeBoolean(showColumns);
        buf.writeBoolean(showBase);
    }

    public static void handle(ServerboundPlaceBuildingPacket message, Supplier<NetworkManager.PacketContext> contextSupplier) {
        NetworkManager.PacketContext context = contextSupplier.get();
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            if (level.isLoaded(message.pos)) {
                BlockState current = level.getBlockState(message.pos);
                if (current.canBeReplaced() || current.isAir()) {
                    level.setBlock(message.pos, ModBlocks.BUILDING_ANCHOR.get().defaultBlockState(), 3);
                    BlockEntity be = level.getBlockEntity(message.pos);
                    if (be instanceof BuildingAnchorBlockEntity anchor) {
                        float rad = (float) Math.toRadians(message.rotationYaw);
                        anchor.setRotation(new Quaternionf().rotationY(rad));
                        anchor.updateParameters(
                                message.bays, message.depths, message.widthMid, message.widthSide, message.depthStep,
                                message.colHeight, message.roofPitch, message.eavesLen, message.gableSetback,
                                message.cornerLift, message.dougongLv, false,
                                message.showRoof, message.showRafters, message.showPurlins, message.showBeams,
                                message.showDougong, message.showColumns, message.showBase
                        );
                    }
                }
            }
        });
    }
}
