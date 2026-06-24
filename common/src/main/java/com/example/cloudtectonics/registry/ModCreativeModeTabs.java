package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.CloudTectonicsMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(CloudTectonicsMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> CLOUD_TECTONICS_TAB = CREATIVE_MODE_TABS.register("cloudtectonics_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ModItems.BUILDING_ANCHOR.get()))
                    .title(Component.translatable("creativetab.cloudtectonics"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BUILDING_ANCHOR.get());
                    })
                    .build());

    public static void init() {
        CREATIVE_MODE_TABS.register();
    }
}
