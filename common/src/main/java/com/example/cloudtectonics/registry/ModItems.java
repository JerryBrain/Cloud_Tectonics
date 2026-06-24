package com.example.cloudtectonics.registry;

import com.example.cloudtectonics.CloudTectonicsMod;
import com.example.cloudtectonics.item.BuildingWandItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CloudTectonicsMod.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> BUILDING_ANCHOR = ITEMS.register("building_anchor",
            () -> new BuildingWandItem(new Item.Properties()));

    public static void init() {
        ITEMS.register();
    }
}
