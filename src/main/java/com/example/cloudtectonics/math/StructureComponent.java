package com.example.cloudtectonics.math;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 建筑基元组件 (1/16 精度)。
 * 存储局部坐标、尺寸、旋转四元数等。
 */
public class StructureComponent {
    public AABB transformedAABB = null;
    
    // 局部中心点坐标，以 1/16 (0.0625) 为单位的缩放，实际上这里可以用浮点数记录真实游戏单位
    public final Vector3f localPos;
    // 尺寸（长宽高），对应实际的 1/16 格网大小
    public final Vector3f size;
    // 局部三维旋转
    public final Quaternionf rotation;
    // 纹理名称或 ID，这里为了演示简化为一个标识符
    public final String texture;
    // 可以添加 UV 信息或使用纹理画板（TextureAtlasSprite）
    // 为了渲染，可以添加 UV 坐标偏移
    public final float u0, v0, u1, v1;
    // 纯色渲染的颜色 (ARGB)
    public final int color;

    public StructureComponent(Vector3f localPos, Vector3f size, Quaternionf rotation, String texture, float u0, float v0, float u1, float v1) {
        this(localPos, size, rotation, texture, u0, v0, u1, v1, 0xFFFFFFFF);
    }

    public StructureComponent(Vector3f localPos, Vector3f size, Quaternionf rotation, String texture, float u0, float v0, float u1, float v1, int color) {
        this.localPos = localPos;
        this.size = size;
        this.rotation = rotation;
        this.texture = texture;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.color = color;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putFloat("x", localPos.x());
        tag.putFloat("y", localPos.y());
        tag.putFloat("z", localPos.z());
        
        tag.putFloat("sx", size.x());
        tag.putFloat("sy", size.y());
        tag.putFloat("sz", size.z());

        tag.putFloat("qx", rotation.x());
        tag.putFloat("qy", rotation.y());
        tag.putFloat("qz", rotation.z());
        tag.putFloat("qw", rotation.w());

        tag.putString("texture", texture);
        tag.putFloat("u0", u0);
        tag.putFloat("v0", v0);
        tag.putFloat("u1", u1);
        tag.putFloat("v1", v1);
        tag.putInt("color", color);
        return tag;
    }

    public static StructureComponent load(CompoundTag tag) {
        Vector3f pos = new Vector3f(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z"));
        Vector3f size = new Vector3f(tag.getFloat("sx"), tag.getFloat("sy"), tag.getFloat("sz"));
        Quaternionf rot = new Quaternionf(tag.getFloat("qx"), tag.getFloat("qy"), tag.getFloat("qz"), tag.getFloat("qw"));
        int color = tag.contains("color") ? tag.getInt("color") : 0xFFFFFFFF;
        return new StructureComponent(
            pos, size, rot,
            tag.getString("texture"),
            tag.getFloat("u0"), tag.getFloat("v0"), tag.getFloat("u1"), tag.getFloat("v1"),
            color
        );
    }

    public static String getTexturePath(StructureComponent comp) {
        if ("tile".equals(comp.texture) || comp.color == 0xFF707A80) { // Slate grey
            return "block/deepslate_tiles";
        }
        if (comp.color == 0xFF9A2A22) { // Vermilion
            return "block/stripped_mangrove_log";
        }
        if (comp.color == 0xFFB58450 || comp.color == 0xFFD4A76A) { // Natural wood, corner beams
            return "block/stripped_spruce_log";
        }
        if (comp.color == 0xFFCDA234 || comp.color == 0xFFD4AF37) { // Gold accents
            return "block/gold_block";
        }
        if (comp.color == 0xFF7B828A) { // Stone base
            return "block/stone_bricks";
        }
        if (comp.color == 0xFFBF5D38) { // Gable wall
            return "block/red_terracotta";
        }
        return "block/white_concrete";
    }
}
