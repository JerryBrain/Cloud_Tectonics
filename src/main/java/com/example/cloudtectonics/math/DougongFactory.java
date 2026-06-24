package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * 斗拱构件生成工厂 (符合宋代/清代法式大木作的拼装结构)
 */
public class DougongFactory {

    public static List<StructureComponent> createDougong(float x, float y, float z, int level, float rotY, int vermilionColor, int jadeColor, int indigoColor, int goldColor) {
        List<StructureComponent> list = new ArrayList<>();
        Quaternionf rot = new Quaternionf().rotationY(rotY);

        // ==========================================
        // LAYER 1: 栌斗 (Lu Dou) - 底部大斗，呈台阶状收缩以模拟斜面
        // ==========================================
        // 底部基座 (稍窄)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.02f, 0.0f), new Vector3f(0.28f, 0.04f, 0.28f), vermilionColor);
        // 上部斗耳 (稍宽)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.08f, 0.0f), new Vector3f(0.38f, 0.08f, 0.38f), vermilionColor);

        // ==========================================
        // LAYER 2: 第一层 栱 (Gong) & 斗 (Dou)
        // ==========================================
        // 1. 泥道栱 (横向，平行于墙面，沿着 X 轴)
        // 主栱身
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.16f, 0.0f), new Vector3f(0.68f, 0.08f, 0.14f), vermilionColor);
        // 下部拱形两端垫木 (模拟曲线拱)
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.26f, 0.10f, 0.0f), new Vector3f(0.12f, 0.04f, 0.14f), vermilionColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.26f, 0.10f, 0.0f), new Vector3f(0.12f, 0.04f, 0.14f), vermilionColor);
        // 彩画装饰 (在栱端面)
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.28f, 0.16f, 0.0f), new Vector3f(0.12f, 0.09f, 0.15f), jadeColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.28f, 0.16f, 0.0f), new Vector3f(0.12f, 0.09f, 0.15f), indigoColor);
        
        // 泥道栱两端的散斗 (San Dou) - 承托上层
        addLocalComponent(list, x, y, z, rot, new Vector3f(-0.28f, 0.22f, 0.0f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.28f, 0.22f, 0.0f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);

        // 2. 华栱 (纵向，垂直于墙面，沿着 Z 轴挑出)
        // 主栱身
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.20f, 0.0f), new Vector3f(0.14f, 0.08f, 0.68f), vermilionColor);
        // 下部拱形两端垫木 (模拟曲线拱)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.14f, -0.26f), new Vector3f(0.14f, 0.04f, 0.12f), vermilionColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.14f, 0.26f), new Vector3f(0.14f, 0.04f, 0.12f), vermilionColor);
        // 华栱两端的交互斗 (Jiaohu Dou)
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.26f, -0.28f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.26f, 0.28f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);

        // ==========================================
        // LAYER 3: 昂 (Ang) - 斜下挑出 (Level >= 1)
        // ==========================================
        if (level >= 1) {
            // 昂身 (斜向挑出)
            Quaternionf tiltedRot = new Quaternionf(rot).rotateLocalX(0.15f);
            addLocalComponent(list, x, y, z, tiltedRot, new Vector3f(0.0f, 0.28f, 0.12f), new Vector3f(0.12f, 0.08f, 0.90f), vermilionColor);
            
            // 昂尖装饰 (昂嘴，向下倾斜伸出)
            addLocalComponent(list, x, y, z, tiltedRot, new Vector3f(0.0f, 0.28f, 0.12f + 0.45f), new Vector3f(0.13f, 0.09f, 0.15f), goldColor);
            
            // 昂头所承托的交互斗
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.24f, 0.54f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);
        }

        // ==========================================
        // LAYER 4: 耍头 (Shuatou) & 慢栱 (Man Gong) (Level == 2)
        // ==========================================
        if (level == 2) {
            // 1. 慢栱 (横向较长的栱，平行于 X 轴，安装在泥道栱之上)
            // 主栱身
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.32f, 0.0f), new Vector3f(0.86f, 0.08f, 0.14f), jadeColor);
            // 慢栱两端垫木
            addLocalComponent(list, x, y, z, rot, new Vector3f(-0.35f, 0.26f, 0.0f), new Vector3f(0.12f, 0.04f, 0.14f), jadeColor);
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.35f, 0.26f, 0.0f), new Vector3f(0.12f, 0.04f, 0.14f), jadeColor);
            // 慢栱两端的散斗
            addLocalComponent(list, x, y, z, rot, new Vector3f(-0.37f, 0.38f, 0.0f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.37f, 0.38f, 0.0f), new Vector3f(0.16f, 0.05f, 0.16f), goldColor);

            // 2. 耍头 (纵向，位于最上方的装饰木雕，平行于 Z 轴)
            // 主耍头身
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.36f, 0.05f), new Vector3f(0.14f, 0.08f, 0.55f), vermilionColor);
            // 耍头前突的雕刻尖端 (模拟耍头桃尖/蚂蚱头)
            addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, 0.345f, 0.35f), new Vector3f(0.14f, 0.05f, 0.12f), vermilionColor);
        }

        // ==========================================
        // LAYER 5: 齐心斗 (Qixin Dou) - 顶部承接托梁
        // ==========================================
        float topY = level == 2 ? 0.44f : 0.32f;
        addLocalComponent(list, x, y, z, rot, new Vector3f(0.0f, topY, 0.0f), new Vector3f(0.22f, 0.06f, 0.22f), goldColor);

        return list;
    }

    private static void addLocalComponent(List<StructureComponent> list, float anchorX, float anchorY, float anchorZ, Quaternionf parentRot, Vector3f localPos, Vector3f size, int color) {
        Vector3f rotatedPos = new Vector3f(localPos).rotate(parentRot);
        Vector3f absPos = new Vector3f(anchorX, anchorY, anchorZ).add(rotatedPos);
        list.add(new StructureComponent(absPos, size, parentRot, "", 0, 0, 16, 16, color));
    }
}
