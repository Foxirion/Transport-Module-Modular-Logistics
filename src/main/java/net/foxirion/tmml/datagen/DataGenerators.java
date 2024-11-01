package net.foxirion.tmml.datagen;

import net.foxirion.tmml.datagen.recipe.TMMLRecipeProvider;
import net.foxirion.tmml.init.TMML;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static net.foxirion.tmml.init.TMML.TMMLID;

@EventBusSubscriber(modid = TMMLID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        try {
            DataGenerator generator = event.getGenerator();
            PackOutput output = generator.getPackOutput();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            generator.addProvider(true, new LangProvider(output));

            generator.addProvider(true, new TMMLRecipeProvider(output, lookupProvider));

            generator.addProvider(true, new TMMLItemModelProvider(output, existingFileHelper));

        } catch (RuntimeException e) {
            TMML.logger.error("Transport Module: Modular Logistics failed to gather data", e);
        }
    }
}