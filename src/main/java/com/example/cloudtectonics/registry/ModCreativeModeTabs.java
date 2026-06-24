package com.example.cloudtectonics.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组创造模式物品栏选项卡注册类
 */
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModBlocks.MODID);

    public static final RegistryObject<CreativeModeTab> CLOUD_TECTONICS_TAB = CREATIVE_MODE_TABS.register("cloudtectonics_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BUILDING_ANCHOR.get()))
                    .title(Component.translatable("creativetab.cloudtectonics"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BUILDING_ANCHOR.get());
                    })
                    .build());
}
