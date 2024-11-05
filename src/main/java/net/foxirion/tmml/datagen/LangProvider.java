package net.foxirion.tmml.datagen;

import net.foxirion.tmml.init.TMMLCreativeModeTabs;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;


import static net.foxirion.tmml.init.TMML.TMMLID;

public class LangProvider extends LanguageProvider {
    public LangProvider(PackOutput output) {
        super(output, TMMLID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // Void Bottle translations
        // item name
        addItem(TMMLItems.VOID_BOTTLE, "Void Bottle");
        // description
        add("item.void_bottle.warning", "§c§lWARNING: DO NOT DRINK!");
        add("item.void_bottle.description", "§4Consuming this will result in certain death.");
        // death message
        add("death.void_bottle", "%1$s has been erased from reality by drinking void");

        //Transport Modules
        addItem(TMMLItems.BLOCK_TRANSPORT_MODULE, "Block Transport Module");
        addItem(TMMLItems.LIQUID_TRANSPORT_MODULE, "Liquid Transport Module");
        addItem(TMMLItems.ENTITY_TRANSPORT_MODULE, "Entity Transport Module");

        // Creative Tab
        add(TMMLCreativeModeTabs.TMML_TABS, "Transport Modules: Modular Logistics");
    }

}