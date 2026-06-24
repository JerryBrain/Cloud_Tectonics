package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * 梁架、额枋与瓜柱（支托）几何构件工厂
 */
public class BeamFactory {

    public static List<StructureComponent> createBeamSegment(float x, float y, float z1, float z2, float width, float height, int mainColor, int endColor) {
        List<StructureComponent> list = new ArrayList<>();
        float length = Math.abs(z2 - z1);
        float zCenter = (z1 + z2) / 2.0f;
        
        // 主梁
        list.add(new StructureComponent(
            new Vector3f(x, y + height / 2.0f, zCenter),
            new Vector3f(width, height, length),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            mainColor
        ));

        // 梁端两端包梁箍 (箍色)
        list.add(new StructureComponent(
            new Vector3f(x, y + height / 2.0f, z1),
            new Vector3f(width + 0.04f, 0.12f, 0.2f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            endColor
        ));
        list.add(new StructureComponent(
            new Vector3f(x, y + height / 2.0f, z2),
            new Vector3f(width + 0.04f, 0.12f, 0.2f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            endColor
        ));

        return list;
    }

    public static List<StructureComponent> createStrut(float x, float y, float z, float strutH, int baseColor, int shaftColor, int capColor, float caiSize) {
        List<StructureComponent> list = new ArrayList<>();
        float w = caiSize * 0.7f; // e.g., 0.14m for 0.20m Cai
        float fen = caiSize / 15.0f;
        
        float baseW = w + 4.0f * fen;
        float baseH = 4.0f * fen;
        // 栌斗底座 (金)
        list.add(new StructureComponent(
            new Vector3f(x, y + baseH / 2.0f, z),
            new Vector3f(baseW, baseH, baseW),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            baseColor
        ));
        
        // 瓜柱立身 (朱红)
        float postH = strutH - 8.0f * fen;
        if (postH > 0.02f) {
            list.add(new StructureComponent(
                new Vector3f(x, y + baseH + postH / 2.0f, z),
                new Vector3f(w, postH, w),
                new Quaternionf(),
                "",
                0, 0, 16, 16,
                shaftColor
            ));
        }
        
        // 顶端替木垫块 (原木)
        float capH = 4.0f * fen;
        list.add(new StructureComponent(
            new Vector3f(x, y + strutH - capH / 2.0f, z),
            new Vector3f(w + 3.0f * fen, capH, 22.0f * fen),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            capColor
        ));

        return list;
    }

    public static List<StructureComponent> createTie(float x, float y, float z, float length, float width, float height, int color) {
        List<StructureComponent> list = new ArrayList<>();
        list.add(new StructureComponent(
            new Vector3f(x, y, z),
            new Vector3f(length, width, height),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            color
        ));
        return list;
    }

    public static List<StructureComponent> createTieDecoration(float x, float y, float z, int jadeColor, int indigoColor) {
        List<StructureComponent> list = new ArrayList<>();
        // 绿彩画
        list.add(new StructureComponent(
            new Vector3f(x - 0.4f, y, z),
            new Vector3f(0.5f, 0.26f, 0.26f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            jadeColor
        ));
        // 蓝彩画
        list.add(new StructureComponent(
            new Vector3f(x + 0.4f, y, z),
            new Vector3f(0.5f, 0.26f, 0.26f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            indigoColor
        ));
        return list;
    }

    public static List<StructureComponent> createPurlinStrut(float x, float yBottom, float z, float height, int baseColor, int shaftColor, int capColor, float caiSize) {
        List<StructureComponent> list = new ArrayList<>();
        if (height < 0.08f) return list;
        float w = caiSize * 0.6f; // e.g. 0.12m
        float fen = caiSize / 15.0f;
        
        float baseW = w + 3.0f * fen;
        float baseH = 3.5f * fen;
        // Base
        list.add(new StructureComponent(
            new Vector3f(x, yBottom + baseH / 2.0f, z),
            new Vector3f(baseW, baseH, baseW),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            baseColor
        ));
        
        // Body
        float postH = height - 7.0f * fen;
        if (postH > 0.02f) {
            list.add(new StructureComponent(
                new Vector3f(x, yBottom + baseH + postH / 2.0f, z),
                new Vector3f(w, postH, w),
                new Quaternionf(),
                "",
                0, 0, 16, 16,
                shaftColor
            ));
        }
        
        // Top cushion
        float capH = 3.5f * fen;
        list.add(new StructureComponent(
            new Vector3f(x, yBottom + height - capH / 2.0f, z),
            new Vector3f(w + 4.5f * fen, capH, 20.0f * fen),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            capColor
        ));
        return list;
    }
}
