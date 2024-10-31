package net.foxirion.tmml.item;

import net.foxirion.tmml.item.custom.VoidBottleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TMMLItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "tmml");

    public static final DeferredHolder<Item, Item> VOID_BOTTLE = ITEMS.register("void_bottle",
            () -> new VoidBottleItem(new Item.Properties()
                    .food(TMMLFoods.VOID_BOTTLE)
                    .fireResistant()
                    .stacksTo(1)));
}
