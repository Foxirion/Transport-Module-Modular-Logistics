package net.foxirion.tmml.datagen;

import net.foxirion.tmml.item.TMMLCreativeModeTabs;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Supplier;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class LangProvider extends LanguageProvider {
    public LangProvider(PackOutput output) {
        super(output, TMMLID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // Items
        addItem(TMMLItems.VOID_BOTTLE, "Void Bottle");

        // Void Bottle Translations
        add("item.void_bottle.warning", "§c§lWARNING: DO NOT DRINK!");
        add("item.void_bottle.description", "§4Consuming this will result in certain death.");

        // Creative Tab
        add(TMMLCreativeModeTabs.TMML_TABS, "Transport Module: Modular Logistics");
    }

    public void addObsidianPotion(Supplier<? extends Item> key, Holder<Potion> potionName, String name) {
        add(key.get().getDescriptionId() + ".effect." + potionName.getKey().location().getPath(), name);
    }
}