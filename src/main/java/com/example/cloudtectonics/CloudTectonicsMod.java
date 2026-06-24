package com.example.cloudtectonics;

import com.example.cloudtectonics.registry.ModBlockEntities;
import com.example.cloudtectonics.registry.ModBlocks;
import com.example.cloudtectonics.registry.ModItems;
import com.example.cloudtectonics.registry.ModCreativeModeTabs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModBlocks.MODID)
public class CloudTectonicsMod {

    public CloudTectonicsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册方块与物品
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // 注册网络通信管道
        com.example.cloudtectonics.network.ModMessages.register();

        // 监听创造模式物品栏填充事件
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // 同步放入原版的建筑方块和工具类物品栏中，方便寻找
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.BUILDING_ANCHOR.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BUILDING_ANCHOR.get());
        }
    }
}
