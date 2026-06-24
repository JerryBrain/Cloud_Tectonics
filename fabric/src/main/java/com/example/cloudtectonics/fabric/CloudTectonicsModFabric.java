package com.example.cloudtectonics.fabric;

import com.example.cloudtectonics.CloudTectonicsMod;
import net.fabricmc.api.ModInitializer;

public class CloudTectonicsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CloudTectonicsMod.init();
    }
}
