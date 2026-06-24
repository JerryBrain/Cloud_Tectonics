package com.example.cloudtectonics.forge;

import com.example.cloudtectonics.CloudTectonicsMod;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CloudTectonicsMod.MOD_ID)
public class CloudTectonicsModForge {
    public CloudTectonicsModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(CloudTectonicsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CloudTectonicsMod.init();
    }
}
