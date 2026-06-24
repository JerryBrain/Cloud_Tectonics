package com.example.cloudtectonics.math;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 调试日志记录器，支持在 GUI 中一键开启、停止并导出运行诊断数据。
 */
public class BuildingDebugLogger {
    private static boolean logging = false;
    private static StringBuilder builder = null;

    public static boolean isLogging() {
        return logging;
    }

    public static void startLogging() {
        logging = true;
        builder = new StringBuilder();
        log("=== 云构调试日志开始 ===");
        log("时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        log("系统: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        log("Java 版本: " + System.getProperty("java.version"));
    }

    public static void log(String message) {
        if (logging && builder != null) {
            String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            builder.append("[").append(timestamp).append("] ").append(message).append("\n");
        }
    }

    public static String stopLoggingAndExport() {
        if (!logging) return null;
        logging = false;
        log("=== 云构调试日志结束 ===");
        String content = builder.toString();
        builder = null;

        try {
            java.io.File file = new java.io.File("building_debug_log.txt");
            try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
                out.print(content);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
