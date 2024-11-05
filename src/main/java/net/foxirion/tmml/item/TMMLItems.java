package net.foxirion.tmml.item;

import net.foxirion.tmml.item.custom.BlockTransportModule;
import net.foxirion.tmml.item.custom.VoidBottleItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TMMLItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "tmml");

    public static final RegistryObject<Item> VOID_BOTTLE = ITEMS.register("void_bottle",
            () -> new VoidBottleItem(new Item.Properties()
                    .food(TMMLFoods.VOID_BOTTLE)
                    .fireResistant()
                    .stacksTo(1)));

    public static final  RegistryObject<Item> BLOCK_TRANSPORT_MODULE = ITEMS.register("block_transport_module",
            () -> new BlockTransportModule(new Item.Properties()
                    .stacksTo(1)));

    public static final  RegistryObject<Item> LIQUID_TRANSPORT_MODULE = ITEMS.register("liquid_transport_module",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)));

    public static final  RegistryObject<Item> ENTITY_TRANSPORT_MODULE = ITEMS.register("entity_transport_module",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)));
}
