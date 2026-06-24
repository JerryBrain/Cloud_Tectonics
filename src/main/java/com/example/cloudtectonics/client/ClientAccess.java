package com.example.cloudtectonics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * 客户端专属访问桥接类，防止服务端加载客户端类崩溃
 */
public class ClientAccess {
    public static void openBuildingScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new BuildingConfigurationScreen(pos));
    }
}
