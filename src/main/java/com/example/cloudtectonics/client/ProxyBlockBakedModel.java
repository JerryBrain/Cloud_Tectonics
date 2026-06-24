package com.example.cloudtectonics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 代理方块的自定义烘焙模型。
 * 它的渲染 Quads 列表为空（因为统一由主控制方块渲染，代理方块不需要视觉外观）。
 * 但它覆写了 getParticleIcon(ModelData)，从 ModelData 中动态提取方块的材质，使得粒子引擎能够生成正确的脚步与跌落粒子。
 */
public class ProxyBlockBakedModel implements IDynamicBakedModel {

    private final BakedModel originalModel;

    public ProxyBlockBakedModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        // 返回空，这样代理方块在世界中渲染时完全是空气/隐形的
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        // 默认备用贴图（比如在无 ModelData 传入的上下文中）
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("minecraft", "block/white_concrete"));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        String texturePath = data.get(com.example.cloudtectonics.blockentity.BuildingProxyBlockEntity.TEXTURE_PROPERTY);
        if (texturePath == null) {
            texturePath = "block/white_concrete";
        }
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("minecraft", texturePath));
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
