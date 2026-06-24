package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.item.BuildingWandItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组物品注册类
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModBlocks.MODID);

    public static final RegistryObject<Item> BUILDING_ANCHOR = ITEMS.register("building_anchor",
            () -> new BuildingWandItem(new Item.Properties()));
}
