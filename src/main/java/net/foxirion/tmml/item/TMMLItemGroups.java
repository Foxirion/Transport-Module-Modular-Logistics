package net.foxirion.tmml.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.foxirion.tmml.TMML;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TMMLItemGroups {
    public static String TMML_TABS = "itemgroup." + TMML.TMMLID + ".tmml_tab";

    public static final ItemGroup TMML_TAB = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(TMML.TMMLID, "tmml_tab"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(TMMLItems.VOID_BOTTLE))
                    .displayName(Text.translatable(TMML_TABS))
                    .entries((displayContext, entries) -> {
                        entries.add(TMMLItems.VOID_BOTTLE);
                        entries.add(TMMLItems.BLOCK_TRANSPORT_MODULE);
                        entries.add(TMMLItems.FLUID_TRANSPORT_MODULE);
                    }).build());

    public static void registerTMMLItemGroups() {
        TMML.LOGGER.info("Registering Mod Item Groups for" + TMML.TMMLID);
    }
}
