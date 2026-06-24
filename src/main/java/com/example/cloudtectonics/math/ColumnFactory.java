package com.example.cloudtectonics.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
27:  * 立柱与柱础石几何构件工厂
28:  */
public class ColumnFactory {

    private static final float COL_RADIUS = 0.18f;

    public static List<StructureComponent> createColumn(float x, float y, float z, float height, int color) {
        List<StructureComponent> list = new ArrayList<>();
        // 立柱组件
        list.add(new StructureComponent(
            new Vector3f(x, y + height / 2.0f, z),
            new Vector3f(COL_RADIUS * 2.0f, height, COL_RADIUS * 2.0f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            color
        ));
        return list;
    }

    public static List<StructureComponent> createPlinth(float x, float y, float z, int color) {
        List<StructureComponent> list = new ArrayList<>();
        // 柱础石
        list.add(new StructureComponent(
            new Vector3f(x, y + 0.06f, z),
            new Vector3f(0.7f, 0.12f, 0.7f),
            new Quaternionf(),
            "",
            0, 0, 16, 16,
            color
        ));
        return list;
    }
}
