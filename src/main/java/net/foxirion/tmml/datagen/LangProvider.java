package net.foxirion.tmml.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.foxirion.tmml.item.TMMLItemGroups;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LangProvider extends FabricLanguageProvider {
    protected LangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {

        // Void Bottle
        translationBuilder.add(TMMLItems.VOID_BOTTLE, "Void Bottle");
        translationBuilder.add("item.void_bottle.warning", "§c§lWARNING: DO NOT DRINK!");
        translationBuilder.add("item.void_bottle.description", "§4Consuming this will result in certain death.");
        // Death Message
        translationBuilder.add("death.void_bottle", "%1$s has been erased from reality by drinking void");

        //Transport Modules
        translationBuilder.add(TMMLItems.BLOCK_TRANSPORT_MODULE, "Block Transport Module");
        translationBuilder.add(TMMLItems.FLUID_TRANSPORT_MODULE, "Fluid Transport Module");
        translationBuilder.add(TMMLItems.ENTITY_TRANSPORT_MODULE, "Entity Transport Module");

        // Creative Tab
        translationBuilder.add(TMMLItemGroups.TMML_TABS, "Transport Module: Modular Logistics");
    }
}
