package net.foxirion.tmml.item;

import net.foxirion.tmml.TMML;
import net.foxirion.tmml.item.custom.BlockTransportModule;
import net.foxirion.tmml.item.custom.FluidTransportModule;
import net.foxirion.tmml.item.custom.VoidBottleItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TMMLItems {
    public static Item VOID_BOTTLE = registerItem("void_bottle",
            new VoidBottleItem(new Item.Settings()
                    .maxCount(1)
                    .fireproof()
                    .food(TMMLFoodComponents.VOID_BOTTLE)
            ));

    public static Item BLOCK_TRANSPORT_MODULE = registerItem("block_transport_module", new BlockTransportModule());

    public static Item FLUID_TRANSPORT_MODULE = registerItem("fluid_transport_module", new FluidTransportModule());

    public static Item ENTITY_TRANSPORT_MODULE = registerItem("entity_transport_module",
            new Item(new Item.Settings()
                    .maxCount(1)
            ));

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(TMML.TMMLID, name), item);
    }

    public static void registerTMMLItems() {
        TMML.LOGGER.info("Registering Mod Items for" + TMML.TMMLID);
    }
}
