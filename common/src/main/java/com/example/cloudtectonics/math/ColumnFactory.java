package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
27:  * 立柱与柱础石几何构件工厂
28:  */
public class ColumnFactory {

    public static List<StructureComponent> createColumn(float x, float y, float z, float height, int color, float caiSize) {
        List<StructureComponent> list = new ArrayList<>();
        float colRadius = caiSize * 0.9f;
        // 立柱组件
        list.add(new StructureComponent(
            new Vector3f(x, y + height / 2.0f, z),
            new Vector3f(colRadius * 2.0f, height, colRadius * 2.0f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            color
        ));
        return list;
    }

    public static List<StructureComponent> createPlinth(float x, float y, float z, int color, float caiSize) {
        List<StructureComponent> list = new ArrayList<>();
        float plinthW = caiSize * 3.5f;
        float plinthH = caiSize * 0.6f;
        // 柱础石
        list.add(new StructureComponent(
            new Vector3f(x, y + plinthH / 2.0f, z),
            new Vector3f(plinthW, plinthH, plinthW),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            color
        ));
        return list;
    }
}
