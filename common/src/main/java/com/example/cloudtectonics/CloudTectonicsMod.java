package com.example.cloudtectonics;

import com.example.cloudtectonics.registry.ModBlockEntities;
import com.example.cloudtectonics.registry.ModBlocks;
import com.example.cloudtectonics.registry.ModItems;
import com.example.cloudtectonics.registry.ModCreativeModeTabs;
import com.example.cloudtectonics.network.ModMessages;

public class CloudTectonicsMod {
    public static final String MOD_ID = "cloudtectonics";

    public static void init() {
        // Initialize Registries
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModCreativeModeTabs.init();

        // Register Networking
        ModMessages.register();
    }
}
