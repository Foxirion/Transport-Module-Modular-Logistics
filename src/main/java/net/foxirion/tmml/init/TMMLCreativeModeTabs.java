package net.foxirion.tmml.init;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TMMLID);

    public static String TMML_TABS = TMMLID + ".creativetab";

    public static final Supplier<CreativeModeTab> TMML_TAB = CREATIVE_MODE_TABS.register("tmml_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(TMMLItems.VOID_BOTTLE.get()))
                    .title(Component.translatable(TMML_TABS))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(TMMLItems.VOID_BOTTLE.get());
                        output.accept(TMMLItems.BLOCK_TRANSPORT_MODULE.get());
//                output.accept(TMMLItems.LIQUID_TRANSPORT_MODULE.get());
//                output.accept(TMMLItems.ENTITY_TRANSPORT_MODULE.get());
                    }).build()
    );
}