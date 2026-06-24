package com.example.cloudtectonics.block;

import com.example.cloudtectonics.blockentity.BuildingAnchorBlockEntity;
import com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity;
import com.example.cloudtectonics.math.GomedricTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import org.jetbrains.annotations.Nullable;

/**
 * 隐形代理方块。
 * 占据建筑的空间，提供1/16精度的碰撞箱，并将交互事件转发给主方块。
 */
public class BuildingProxyBlock extends BaseEntityBlock {

    public BuildingProxyBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL; // 改变渲染形状为 MODEL，以触发渲染相关的粒子/声音逻辑，但烘焙模型本身不生成面，因此仍然不可见
    }



    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return com.example.cloudtectonics.registry.ModBlockEntities.createProxyBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof BuildingProxyBlockEntity proxy) {
            VoxelShape cached = proxy.getCachedShape();
            if (cached != null) {
                return cached;
            }
            BlockPos anchorPos = proxy.getAnchorPos();
            if (anchorPos != null) {
                BlockEntity anchorBe = pLevel.getBlockEntity(anchorPos);
                if (anchorBe instanceof BuildingAnchorBlockEntity anchor) {
                    if (anchor.isPreview()) {
                        return Shapes.empty();
                    }
                    
                    VoxelShape shape = GomedricTransformer.calculateProxyShape(anchorPos, pPos, anchor);
                    proxy.setCachedShape(shape);
                    return shape;
                }
            }
        }
        return Shapes.empty();
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof BuildingProxyBlockEntity proxy) {
                BlockPos anchorPos = proxy.getAnchorPos();
                if (anchorPos != null) {
                    // 如果代理方块被破坏，且主方块还在，则破坏主方块（连锁反应）
                    BlockState anchorState = pLevel.getBlockState(anchorPos);
                    if (anchorState.getBlock() instanceof BuildingAnchorBlock) {
                        pLevel.destroyBlock(anchorPos, true);
                    }
                }
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof BuildingProxyBlockEntity proxy) {
            BlockPos anchorPos = proxy.getAnchorPos();
            if (anchorPos != null) {
                BlockState anchorState = pLevel.getBlockState(anchorPos);
                // 转发交互事件
                return anchorState.use(pLevel, pPlayer, pHand, 
                        new BlockHitResult(pHit.getLocation(), pHit.getDirection(), anchorPos, pHit.isInside()));
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof BuildingProxyBlockEntity proxy) {
            BlockPos anchorPos = proxy.getAnchorPos();
            if (anchorPos != null) {
                // 转发攻击(左键)事件
                pLevel.getBlockState(anchorPos).attack(pLevel, anchorPos, pPlayer);
            }
        }
        super.attack(pState, pLevel, pPos, pPlayer);
    }
}
