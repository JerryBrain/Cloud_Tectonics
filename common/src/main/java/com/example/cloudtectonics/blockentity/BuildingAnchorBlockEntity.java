package com.example.cloudtectonics.blockentity;

import com.example.cloudtectonics.block.BuildingAnchorBlock;
import com.example.cloudtectonics.math.StructureComponent;
import com.example.cloudtectonics.math.ParametricBuildingGenerator;
import com.example.cloudtectonics.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑主控制节点实体。
 * 负责存储所有建筑参数、组件信息，NBT持久化以及客户端同步。
 */
public class BuildingAnchorBlockEntity extends BlockEntity {

    private final List<StructureComponent> components = new ArrayList<>();
    private List<AABB> cachedAABBs = null;
    // 建筑占用的包围盒大小，以方块为单位
    private int boundX = 1;
    private int boundY = 1;
    private int boundZ = 1;
    // 整体建筑的旋转
    private Quaternionf rotation = new Quaternionf(); 

    // 参数化建造参数
    private int bays = 3;
    private int depths = 2;
    private float widthMid = 5.5f;
    private float widthSide = 4.5f;
    private float depthStep = 4.5f;
    private float colHeight = 4.0f;
    private float roofPitch = 0.65f;
    private float eavesLen = 1.5f;
    private float gableSetback = 1.0f;
    private float cornerLift = 0.6f;
    private int dougongLv = 1;

    // 图层显示隐藏开关
    private boolean showRoof = true;
    private boolean showRafters = true;
    private boolean showPurlins = true;
    private boolean showBeams = true;
    private boolean showDougong = true;
    private boolean showColumns = true;
    private boolean showBase = true;

    // 是否为虚影预览模式 (Preview/Hologram Mode)
    private boolean preview = true;

    public BuildingAnchorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.BUILDING_ANCHOR_BE.get(), pPos, pBlockState);
    }

    public void generateBuilding() {
        this.components.clear();
        this.components.addAll(ParametricBuildingGenerator.generate(
                bays, depths, widthMid, widthSide, depthStep, colHeight, roofPitch, eavesLen, gableSetback, cornerLift, dougongLv,
                showRoof, showRafters, showPurlins, showBeams, showDougong, showColumns, showBase
        ));
        this.cachedAABBs = null;
        setChanged();
    }

    public void updateParameters(int bays, int depths, float widthMid, float widthSide, float depthStep,
                                 float colHeight, float roofPitch, float eavesLen, float gableSetback, float cornerLift,
                                 int dougongLv, boolean preview,
                                 boolean showRoof, boolean showRafters, boolean showPurlins,
                                 boolean showBeams, boolean showDougong, boolean showColumns, boolean showBase) {
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
        this.preview = preview;
        this.showRoof = showRoof;
        this.showRafters = showRafters;
        this.showPurlins = showPurlins;
        this.showBeams = showBeams;
        this.showDougong = showDougong;
        this.showColumns = showColumns;
        this.showBase = showBase;

        // 重新生成结构组件
        generateBuilding();
        
        // 如果退出预览状态（确定建造），则在世界上放置实际的物理代理方块
        if (!preview) {
            // 根据实际旋转后的组件包围盒大小，动态计算世界边界包围盒
            List<AABB> aabbs = getCachedAABBs();
            double minX = 0, minY = 0, minZ = 0;
            double maxX = 0, maxY = 0, maxZ = 0;
            for (AABB aabb : aabbs) {
                minX = Math.min(minX, aabb.minX);
                minY = Math.min(minY, aabb.minY);
                minZ = Math.min(minZ, aabb.minZ);
                maxX = Math.max(maxX, aabb.maxX);
                maxY = Math.max(maxY, aabb.maxY);
                maxZ = Math.max(maxZ, aabb.maxZ);
            }
            
            // 为保证关于 anchorPos 的对称性和留出安全余量，向外扩展并对称化
            int extX = (int) Math.ceil(Math.max(Math.abs(minX), Math.abs(maxX))) + 2;
            int extZ = (int) Math.ceil(Math.max(Math.abs(minZ), Math.abs(maxZ))) + 2;
            this.boundX = extX * 2 + 1;
            this.boundZ = extZ * 2 + 1;
            this.boundY = (int) Math.ceil(maxY) + 2;
            
            if (level != null && !level.isClientSide()) {
                BlockState state = getBlockState();
                if (state.getBlock() instanceof BuildingAnchorBlock anchorBlock) {
                    anchorBlock.removeProxyBlocks(level, worldPosition, this);
                    anchorBlock.placeProxyBlocks(level, worldPosition, this);
                }
            }
        }

        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
        setChanged();
    }

    // 参数 Getter 方法
    public int getBays() { return bays; }
    public int getDepths() { return depths; }
    public float getWidthMid() { return widthMid; }
    public float getWidthSide() { return widthSide; }
    public float getDepthStep() { return depthStep; }
    public float getColHeight() { return colHeight; }
    public float getRoofPitch() { return roofPitch; }
    public float getEavesLen() { return eavesLen; }
    public float getGableSetback() { return gableSetback; }
    public float getCornerLift() { return cornerLift; }
    public int getDougongLv() { return dougongLv; }

    public boolean isShowRoof() { return showRoof; }
    public boolean isShowRafters() { return showRafters; }
    public boolean isShowPurlins() { return showPurlins; }
    public boolean isShowBeams() { return showBeams; }
    public boolean isShowDougong() { return showDougong; }
    public boolean isShowColumns() { return showColumns; }
    public boolean isShowBase() { return showBase; }

    public void setBounds(int x, int y, int z) {
        this.boundX = x;
        this.boundY = y;
        this.boundZ = z;
        setChanged();
    }
    
    public int[] getBounds() {
        return new int[]{boundX, boundY, boundZ};
    }

    public void addComponent(StructureComponent component) {
        this.components.add(component);
        this.cachedAABBs = null;
        setChanged();
    }

    public List<StructureComponent> getComponents() {
        return components;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
        this.cachedAABBs = null;
        setChanged();
    }

    public List<AABB> getCachedAABBs() {
        if (this.cachedAABBs == null) {
            long startTime = System.nanoTime();
            this.cachedAABBs = new ArrayList<>();
            Quaternionf anchorRot = getRotation();
            for (StructureComponent comp : this.components) {
                org.joml.Matrix4f transform = new org.joml.Matrix4f()
                        .rotate(anchorRot)
                        .translate(comp.localPos)
                        .rotate(comp.rotation)
                        .scale(comp.size);
                AABB aabb = com.example.cloudtectonics.math.GomedricTransformer.transformUnitCube(transform);
                comp.transformedAABB = aabb;
                this.cachedAABBs.add(aabb);
            }
            long endTime = System.nanoTime();
            com.example.cloudtectonics.math.BuildingDebugLogger.log("预计算 AABB 完成，组件数量: " + this.components.size() + ", 耗时: " + (endTime - startTime) / 1000000.0f + "ms");
        }
        return this.cachedAABBs;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("boundX", boundX);
        pTag.putInt("boundY", boundY);
        pTag.putInt("boundZ", boundZ);

        pTag.putFloat("rotX", rotation.x());
        pTag.putFloat("rotY", rotation.y());
        pTag.putFloat("rotZ", rotation.z());
        pTag.putFloat("rotW", rotation.w());

        // 参数写入 NBT
        pTag.putInt("bays", bays);
        pTag.putInt("depths", depths);
        pTag.putFloat("widthMid", widthMid);
        pTag.putFloat("widthSide", widthSide);
        pTag.putFloat("depthStep", depthStep);
        pTag.putFloat("colHeight", colHeight);
        pTag.putFloat("roofPitch", roofPitch);
        pTag.putFloat("eavesLen", eavesLen);
        pTag.putFloat("gableSetback", gableSetback);
        pTag.putFloat("cornerLift", cornerLift);
        pTag.putInt("dougongLv", dougongLv);

        pTag.putBoolean("showRoof", showRoof);
        pTag.putBoolean("showRafters", showRafters);
        pTag.putBoolean("showPurlins", showPurlins);
        pTag.putBoolean("showBeams", showBeams);
        pTag.putBoolean("showDougong", showDougong);
        pTag.putBoolean("showColumns", showColumns);
        pTag.putBoolean("showBase", showBase);

        pTag.putBoolean("preview", preview);

        ListTag compList = new ListTag();
        for (StructureComponent component : components) {
            compList.add(component.save(new CompoundTag()));
        }
        pTag.put("components", compList);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.boundX = pTag.getInt("boundX");
        this.boundY = pTag.getInt("boundY");
        this.boundZ = pTag.getInt("boundZ");

        this.rotation = new Quaternionf(
                pTag.getFloat("rotX"),
                pTag.getFloat("rotY"),
                pTag.getFloat("rotZ"),
                pTag.getFloat("rotW")
        );

        // NBT 读取参数
        if (pTag.contains("bays")) this.bays = pTag.getInt("bays");
        if (pTag.contains("depths")) this.depths = pTag.getInt("depths");
        if (pTag.contains("widthMid")) this.widthMid = pTag.getFloat("widthMid");
        if (pTag.contains("widthSide")) this.widthSide = pTag.getFloat("widthSide");
        if (pTag.contains("depthStep")) this.depthStep = pTag.getFloat("depthStep");
        if (pTag.contains("colHeight")) this.colHeight = pTag.getFloat("colHeight");
        if (pTag.contains("roofPitch")) this.roofPitch = pTag.getFloat("roofPitch");
        if (pTag.contains("eavesLen")) this.eavesLen = pTag.getFloat("eavesLen");
        if (pTag.contains("gableSetback")) this.gableSetback = pTag.getFloat("gableSetback");
        if (pTag.contains("cornerLift")) this.cornerLift = pTag.getFloat("cornerLift");
        if (pTag.contains("dougongLv")) this.dougongLv = pTag.getInt("dougongLv");

        if (pTag.contains("showRoof")) this.showRoof = pTag.getBoolean("showRoof");
        if (pTag.contains("showRafters")) this.showRafters = pTag.getBoolean("showRafters");
        if (pTag.contains("showPurlins")) this.showPurlins = pTag.getBoolean("showPurlins");
        if (pTag.contains("showBeams")) this.showBeams = pTag.getBoolean("showBeams");
        if (pTag.contains("showDougong")) this.showDougong = pTag.getBoolean("showDougong");
        if (pTag.contains("showColumns")) this.showColumns = pTag.getBoolean("showColumns");
        if (pTag.contains("showBase")) this.showBase = pTag.getBoolean("showBase");

        if (pTag.contains("preview")) this.preview = pTag.getBoolean("preview");

        this.components.clear();
        if (pTag.contains("components", Tag.TAG_LIST)) {
            ListTag compList = pTag.getList("components", Tag.TAG_COMPOUND);
            for (int i = 0; i < compList.size(); i++) {
                this.components.add(StructureComponent.load(compList.getCompound(i)));
            }
        }
        this.cachedAABBs = null;
        
        // 若没有组件数据，则自动根据当前参数生成默认结构
        if (this.components.isEmpty()) {
            generateBuilding();
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
            // 通知客户端重新渲染和更新模型烘焙
            if (level != null && level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public AABB getRenderBoundingBox() {
        List<AABB> aabbs = getCachedAABBs();
        if (aabbs.isEmpty()) {
            return new AABB(worldPosition);
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (AABB aabb : aabbs) {
            minX = Math.min(minX, aabb.minX);
            minY = Math.min(minY, aabb.minY);
            minZ = Math.min(minZ, aabb.minZ);
            maxX = Math.max(maxX, aabb.maxX);
            maxY = Math.max(maxY, aabb.maxY);
            maxZ = Math.max(maxZ, aabb.maxZ);
        }
        return new AABB(
            worldPosition.getX() + minX,
            worldPosition.getY() + minY,
            worldPosition.getZ() + minZ,
            worldPosition.getX() + maxX,
            worldPosition.getY() + maxY,
            worldPosition.getZ() + maxZ
        );
    }
}
