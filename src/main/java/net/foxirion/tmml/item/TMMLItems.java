package net.foxirion.tmml.item;

import net.foxirion.tmml.init.TMML;
import net.foxirion.tmml.item.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TMMLID);

    public static final DeferredItem<Item> VOID_BOTTLE = registerItem("void_bottle", properties ->
             new VoidBottleItem(properties
                    .food(TMMLFoods.VOID_BOTTLE, Consumables.DEFAULT_DRINK)
                    .fireResistant()
                    .stacksTo(1)));

    public static final DeferredItem<Item> BLOCK_TRANSPORT_MODULE = registerItem("block_transport_module", properties ->
             new BlockTransportModule(properties
                    .stacksTo(1)));

    public static final DeferredItem<Item> FLUID_TRANSPORT_MODULE = registerItem("fluid_transport_module", properties ->
             new FluidTransportModule(properties
                    .stacksTo(1)));

    public static final DeferredItem<Item> ENTITY_TRANSPORT_MODULE = registerItem("entity_transport_module", properties ->
            new EntityTransportModule(properties
                    .stacksTo(1)));

    public static Item createBlockItemWithCustomItemName(Block block, Item.Properties properties) {
        return new BlockItem(block, properties.useItemDescriptionPrefix());
    }

    public static ResourceKey<Item> itemId(String name) {
        return ResourceKey.create(Registries.ITEM, TMML.rl(name));
    }

    public static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemCreator) {
        return ITEMS.register(name, () -> itemCreator.apply(new Item.Properties().setId(itemId(name))));
    }
}

