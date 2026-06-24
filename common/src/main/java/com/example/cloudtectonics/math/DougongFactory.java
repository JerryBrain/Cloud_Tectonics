package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * 斗拱构件生成工厂 (符合宋代/清代法式大木作的拼装结构)
 */
public class DougongFactory {

    public static List<StructureComponent> createDougong(float x, float y, float z, int level, float rotY, int vermilionColor, int jadeColor, int indigoColor, int goldColor, float caiSize) {
        List<StructureComponent> list = new ArrayList<>();
        Quaternionf rot = new Quaternionf().rotationY(rotY);
        float ratio = caiSize / 0.2f; // Proportional scaling ratio relative to standard 0.20m Cai

        // ==========================================
        // LAYER 1: 栌斗 (Lu Dou) - 底部大斗，呈台阶状收缩以模拟斜面
        // ==========================================
        // 底部基座 (稍窄)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.02f * ratio, 0.0f), new Vector3f(0.28f * ratio, 0.04f * ratio, 0.28f * ratio), vermilionColor);
        // 上部斗耳 (稍宽)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.08f * ratio, 0.0f), new Vector3f(0.38f * ratio, 0.08f * ratio, 0.38f * ratio), vermilionColor);

        // ==========================================
        // LAYER 2: 第一层 栱 (Gong) & 斗 (Dou)
        // ==========================================
        // 1. 泥道栱 (横向，平行于墙面，沿着 X 轴)
        // 主栱身
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.16f * ratio, 0.0f), new Vector3f(0.68f * ratio, 0.08f * ratio, 0.14f * ratio), vermilionColor);
        // 下部拱形两端垫木 (模拟曲线拱)
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.26f * ratio, 0.10f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.04f * ratio, 0.14f * ratio), vermilionColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.26f * ratio, 0.10f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.04f * ratio, 0.14f * ratio), vermilionColor);
        // 彩画装饰 (在栱端面)
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.28f * ratio, 0.16f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.09f * ratio, 0.15f * ratio), jadeColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.28f * ratio, 0.16f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.09f * ratio, 0.15f * ratio), indigoColor);
        
        // 泥道栱两端的散斗 (San Dou) - 承托上层
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.28f * ratio, 0.22f * ratio, 0.0f), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.28f * ratio, 0.22f * ratio, 0.0f), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);

        // 2. 华栱 (纵向，垂直于墙面，沿着 Z 轴挑出)
        // 主栱身
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.20f * ratio, 0.0f), new Vector3f(0.14f * ratio, 0.08f * ratio, 0.68f * ratio), vermilionColor);
        // 下部拱形两端垫木 (模拟曲线拱)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.14f * ratio, -0.26f * ratio), new Vector3f(0.14f * ratio, 0.04f * ratio, 0.12f * ratio), vermilionColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.14f * ratio, 0.26f * ratio), new Vector3f(0.14f * ratio, 0.04f * ratio, 0.12f * ratio), vermilionColor);
        // 华栱两端的交互斗 (Jiaohu Dou)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.26f * ratio, -0.28f * ratio), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.26f * ratio, 0.28f * ratio), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);

        // ==========================================
        // LAYER 3: 昂 (Ang) - 斜下挑出 (Level >= 1)
        // ==========================================
        if (level >= 1) {
            // 昂身 (斜向挑出) - 使用 WEDGE 几何类型，呈现斜切角效果
            Quaternionf tiltedRot = new Quaternionf(rot).rotateLocalX(0.15f);
            addLocalComponent(list, x, y, z, tiltedRot, new Vector3f(0.0f, 0.28f * ratio, 0.12f * ratio), new Vector3f(0.12f * ratio, 0.08f * ratio, 0.90f * ratio), vermilionColor, StructureComponent.ShapeType.WEDGE, 0);
            
            // 昂尖装饰 (昂嘴，向下倾斜伸出) - 同样使用 WEDGE 实现斜切昂嘴
            addLocalComponent(list, x, y, z, tiltedRot, new Vector3f(0.0f, 0.28f * ratio, (0.12f + 0.45f) * ratio), new Vector3f(0.13f * ratio, 0.09f * ratio, 0.15f * ratio), goldColor, StructureComponent.ShapeType.WEDGE, 0);
            
            // 昂头所承托的交互斗
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.24f * ratio, 0.54f * ratio), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);
        }

        // ==========================================
        // LAYER 4: 耍头 (Shuatou) & 慢栱 (Man Gong) (Level == 2)
        // ==========================================
        if (level == 2) {
            // 1. 慢栱 (横向较长的栱，平行于 X 轴，安装在泥道栱之上)
            // 主栱身
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.32f * ratio, 0.0f), new Vector3f(0.86f * ratio, 0.08f * ratio, 0.14f * ratio), jadeColor);
            // 慢栱两端垫木
            addLocalComponent(list, x, y, z, rot, new Vector3f(-0.35f * ratio, 0.26f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.04f * ratio, 0.14f * ratio), jadeColor);
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.35f * ratio, 0.26f * ratio, 0.0f), new Vector3f(0.12f * ratio, 0.04f * ratio, 0.14f * ratio), jadeColor);
            // 慢栱两端的散斗
            addLocalComponent(list, x, y, z, rot, new Vector3f(-0.37f * ratio, 0.38f * ratio, 0.0f), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.37f * ratio, 0.38f * ratio, 0.0f), new Vector3f(0.16f * ratio, 0.05f * ratio, 0.16f * ratio), goldColor);

            // 2. 耍头 (纵向，位于最上方的装饰木雕，平行于 Z 轴)
            // 主耍头身
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.36f * ratio, 0.05f * ratio), new Vector3f(0.14f * ratio, 0.08f * ratio, 0.55f * ratio), vermilionColor);
            // 耍头前突的雕刻尖端 (模拟耍头桃尖/蚂蚱头)
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.345f * ratio, 0.35f * ratio), new Vector3f(0.14f * ratio, 0.05f * ratio, 0.12f * ratio), vermilionColor);
        }

        // ==========================================
        // LAYER 5: 齐心斗 (Qixin Dou) - 顶部承接托梁
        // ==========================================
        float topY = level == 2 ? 0.44f : 0.32f;
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, topY * ratio, 0.0f), new Vector3f(0.22f * ratio, 0.06f * ratio, 0.22f * ratio), goldColor);

        return list;
    }

    private static void addLocalComponent(List<StructureComponent> list, float anchorX, float anchorY, float anchorZ, Quaternionf parentRot, Vector3f localPos, Vector3f size, int color) {
        addLocalComponent(list, anchorX, anchorY, anchorZ, parentRot, localPos, size, color, StructureComponent.ShapeType.CUBE, 0);
    }

    private static void addLocalComponent(List<StructureComponent> list, float anchorX, float anchorY, float anchorZ, Quaternionf parentRot, Vector3f localPos, Vector3f size, int color, StructureComponent.ShapeType shape, int wedgeDirection) {
        Vector3f rotatedPos = new Vector3f(localPos).rotate(parentRot);
        Vector3f absPos = new Vector3f(anchorX, anchorY, anchorZ).add(rotatedPos);
        list.add(new StructureComponent(absPos, size, parentRot, "", 0, 0, 16, 16, color, shape, wedgeDirection));
    }
}
