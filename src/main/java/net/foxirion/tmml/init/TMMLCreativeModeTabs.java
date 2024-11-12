package net.foxirion.tmml.init;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TMMLCreativeModeTabs {
    public static final CreativeModeTab TMML_TAB =
            new CreativeModeTab("tmmltab") {
                @Override
                public ItemStack makeIcon() {
                    return new ItemStack(TMMLItems.VOID_BOTTLE.get());
                }
            };
}