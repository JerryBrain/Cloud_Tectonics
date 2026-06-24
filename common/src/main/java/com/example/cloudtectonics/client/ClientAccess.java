package com.example.cloudtectonics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import com.example.cloudtectonics.math.BuildingDebugLogger;
import net.minecraft.network.chat.Component;

/**
 * 客户端专属访问桥接类，防止服务端加载客户端类崩溃
 */
public class ClientAccess {
    public static boolean debugEnabled = true;

    public static void openBuildingScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new BuildingConfigurationScreen(pos));
    }

    public static void toggleDebugLogging() {
        Minecraft mc = Minecraft.getInstance();
        if (BuildingDebugLogger.isLogging()) {
            String path = BuildingDebugLogger.stopLoggingAndExport();
            if (path != null) {
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("调试日志已导出至: " + path));
                }
            } else {
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("调试日志导出失败。"));
                }
            }
        } else {
            BuildingDebugLogger.startLogging();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("开始录制调试日志..."));
            }
        }
    }
}
