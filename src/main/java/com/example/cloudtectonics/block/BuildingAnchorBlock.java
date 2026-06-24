package com.example.cloudtectonics.block;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 建筑的主控制方块 (Master Block)。
 * 放置后可生成占据整个建筑空间的代理方块 (Proxy Blocks)。
 */
public class BuildingAnchorBlock extends BaseEntityBlock {

    public BuildingAnchorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        // 由于使用自定义 BlockEntityRenderer 渲染，这里返回 ENTITYBLOCK_ANIMATED 
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BuildingAnchorBlockEntity(pPos, pState);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (!pLevel.isClientSide()) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof BuildingAnchorBlockEntity anchor) {
                // 默认进入预览/虚影模式
                anchor.setPreview(true);
                anchor.generateBuilding();
            }
        } else {
            // 客户端：立即开启配置 GUI
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                com.example.cloudtectonics.client.ClientAccess.openBuildingScreen(pPos);
            });
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            // 当主方块被破坏时，移除所有的代理方块
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof BuildingAnchorBlockEntity anchor) {
                removeProxyBlocks(pLevel, pPos, anchor);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public void placeProxyBlocks(Level level, BlockPos anchorPos, BuildingAnchorBlockEntity anchor) {
        long startTime = System.nanoTime();
        int[] bounds = anchor.getBounds();
        int halfX = bounds[0] / 2;
        int halfZ = bounds[2] / 2;
        int height = bounds[1];

        com.example.cloudtectonics.math.BuildingDebugLogger.log("开始生成代理方块... 包围盒: " + bounds[0] + "x" + bounds[1] + "x" + bounds[2]);
        List<AABB> cachedAABBs = anchor.getCachedAABBs();
        List<com.example.cloudtectonics.math.StructureComponent> comps = anchor.getComponents();
        org.joml.Quaternionf anchorRot = anchor.getRotation();
        int placedCount = 0;

        for (int x = -halfX; x <= halfX; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -halfZ; z <= halfZ; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // 跳过主方块本身
                    
                    // 检查此位置是否与任何组件相交，相交了才需要放置代理方块
                    AABB cellBounds = new AABB(x, y, z, x + 1, y + 1, z + 1);
                    boolean intersects = false;
                    String selectedTexture = "block/white_concrete";
                    
                    for (int i = 0; i < comps.size(); i++) {
                        AABB compAABB = cachedAABBs.get(i);
                        if (cellBounds.intersects(compAABB)) {
                            // AABB 粗筛通过，进行精确 OBB 相交检查
                            com.example.cloudtectonics.math.StructureComponent comp = comps.get(i);
                            org.joml.Matrix4f transform = new org.joml.Matrix4f()
                                    .rotate(anchorRot)
                                    .translate(comp.localPos)
                                    .rotate(comp.rotation)
                                    .scale(comp.size);
                            if (com.example.cloudtectonics.math.GomedricTransformer.intersectsOBB(cellBounds, transform)) {
                                intersects = true;
                                selectedTexture = com.example.cloudtectonics.math.StructureComponent.getTexturePath(comp);
                                break;
                            }
                        }
                    }
                    if (!intersects) continue;

                    BlockPos proxyPos = anchorPos.offset(x, y, z);
                    // 只有当位置可替换时才放置代理方块
                    if (level.getBlockState(proxyPos).canBeReplaced()) {
                        level.setBlock(proxyPos, ModBlocks.BUILDING_PROXY.get().defaultBlockState(), 3);
                        BlockEntity proxyBe = level.getBlockEntity(proxyPos);
                        if (proxyBe instanceof com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity proxyEntity) {
                            proxyEntity.setAnchorPos(anchorPos);
                            proxyEntity.setTexturePath(selectedTexture);
                        }
                        placedCount++;
                    }
                }
            }
        }
        long endTime = System.nanoTime();
        com.example.cloudtectonics.math.BuildingDebugLogger.log("代理方块生成完成，放置数量: " + placedCount + ", 耗时: " + (endTime - startTime) / 1000000.0f + "ms");
    }

    public void removeProxyBlocks(Level level, BlockPos anchorPos, BuildingAnchorBlockEntity anchor) {
        long startTime = System.nanoTime();
        int[] bounds = anchor.getBounds();
        int halfX = bounds[0] / 2;
        int halfZ = bounds[2] / 2;
        int height = bounds[1];

        com.example.cloudtectonics.math.BuildingDebugLogger.log("开始移除原有代理方块...");
        int removedCount = 0;

        for (int x = -halfX; x <= halfX; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -halfZ; z <= halfZ; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos proxyPos = anchorPos.offset(x, y, z);
                    if (level.getBlockState(proxyPos).is(ModBlocks.BUILDING_PROXY.get())) {
                        BlockEntity proxyBe = level.getBlockEntity(proxyPos);
                        if (proxyBe instanceof com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity proxyEntity) {
                            // 只有当该代理方块确实属于当前主方块时才进行清理，避免误拆邻近建筑的代理方块
                            if (anchorPos.equals(proxyEntity.getAnchorPos())) {
                                // 移除前断开连接，防止触发 onRemove 中的主方块连锁破坏
                                proxyEntity.setAnchorPos(null);
                                level.removeBlock(proxyPos, false);
                                removedCount++;
                            }
                        }
                    }
                }
            }
        }
        long endTime = System.nanoTime();
        com.example.cloudtectonics.math.BuildingDebugLogger.log("代理方块清除完成，移除数量: " + removedCount + ", 耗时: " + (endTime - startTime) / 1000000.0f + "ms");
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) {
            // 右键点击时重新打开配置 GUI
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                com.example.cloudtectonics.client.ClientAccess.openBuildingScreen(pPos);
            });
        }
        return InteractionResult.SUCCESS;
    }
}
