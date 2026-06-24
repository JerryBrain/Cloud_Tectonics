package com.example.cloudtectonics.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PresetManager {
    public static class Preset {
        public String name;
        public int bays = 3;
        public int depths = 2;
        public float widthMid = 5.5f;
        public float widthSide = 4.5f;
        public float depthStep = 4.5f;
        public float colHeight = 4.0f;
        public float roofPitch = 0.65f;
        public float eavesLen = 1.5f;
        public float gableSetback = 1.0f;
        public float cornerLift = 0.6f;
        public int dougongLv = 1;
        public boolean showRoof = true;
        public boolean showRafters = true;
        public boolean showPurlins = true;
        public boolean showBeams = true;
        public boolean showDougong = true;
        public boolean showColumns = true;
        public boolean showBase = true;

        public Preset() {}

        public Preset(String name, int bays, int depths, float widthMid, float widthSide, float depthStep,
                      float colHeight, float roofPitch, float eavesLen, float gableSetback, float cornerLift, int dougongLv) {
            this.name = name;
            this.bays = bays;
            this.depths = depths;
            this.widthMid = widthMid;
            this.widthSide = widthSide;
            this.depthStep = depthStep;
            this.colHeight = colHeight;
            this.roofPitch = roofPitch;
            this.eavesLen = eavesLen;
            this.gableSetback = gableSetback;
            this.cornerLift = cornerLift;
            this.dougongLv = dougongLv;
        }

        public Preset copy() {
            Preset c = new Preset();
            c.name = this.name;
            c.bays = this.bays;
            c.depths = this.depths;
            c.widthMid = this.widthMid;
            c.widthSide = this.widthSide;
            c.depthStep = this.depthStep;
            c.colHeight = this.colHeight;
            c.roofPitch = this.roofPitch;
            c.eavesLen = this.eavesLen;
            c.gableSetback = this.gableSetback;
            c.cornerLift = this.cornerLift;
            c.dougongLv = this.dougongLv;
            c.showRoof = this.showRoof;
            c.showRafters = this.showRafters;
            c.showPurlins = this.showPurlins;
            c.showBeams = this.showBeams;
            c.showDougong = this.showDougong;
            c.showColumns = this.showColumns;
            c.showBase = this.showBase;
            return c;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Preset> PRESETS = new ArrayList<>();
    private static File configFile;

    static {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "cloudtectonics_presets.json");
        loadPresets();
    }

    public static List<Preset> getPresets() {
        if (PRESETS.isEmpty()) {
            loadPresets();
        }
        return PRESETS;
    }

    public static void loadPresets() {
        PRESETS.clear();
        addDefaultPresets();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type listType = new TypeToken<ArrayList<Preset>>() {}.getType();
                List<Preset> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) {
                    for (Preset loadedPreset : loaded) {
                        // Avoid duplicates with default presets by name
                        PRESETS.removeIf(p -> p.name.equals(loadedPreset.name));
                        PRESETS.add(loadedPreset);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePresets() {
        // Only save custom presets (those that are not default or save all including default)
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(PRESETS, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPreset(Preset preset) {
        PRESETS.removeIf(p -> p.name.equals(preset.name));
        PRESETS.add(preset);
        savePresets();
    }

    public static void deletePreset(String name) {
        PRESETS.removeIf(p -> p.name.equals(name));
        savePresets();
    }

    private static void addDefaultPresets() {
        PRESETS.add(new Preset("园林歇山亭", 3, 2, 4.5f, 4.0f, 4.0f, 3.5f, 0.70f, 1.6f, 0.8f, 0.8f, 1));
        PRESETS.add(new Preset("三开间小殿", 3, 3, 5.5f, 4.5f, 4.5f, 4.0f, 0.65f, 1.5f, 1.0f, 0.6f, 1));
        PRESETS.add(new Preset("五开间大殿", 5, 4, 6.5f, 5.0f, 5.0f, 4.5f, 0.60f, 1.8f, 1.3f, 0.7f, 2));
        PRESETS.add(new Preset("高耸楼阁层", 5, 3, 6.0f, 4.5f, 4.5f, 5.5f, 0.75f, 1.4f, 1.4f, 0.4f, 2));
    }
}
