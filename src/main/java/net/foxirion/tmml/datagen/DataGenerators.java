package net.foxirion.tmml.datagen;

import net.foxirion.tmml.datagen.models.TMMLItemModelGenerator;
import net.foxirion.tmml.datagen.models.TMMLModelProvider;
import net.foxirion.tmml.datagen.recipe.TMMLRecipeProvider;
import net.foxirion.tmml.datagen.tags.TMMLBlockTagsProvider;
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
    public static void gatherData(GatherDataEvent.Client event) {
        try {
            DataGenerator generator = event.getGenerator();
            PackOutput output = generator.getPackOutput();
            ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            event.addProvider(new LangProvider(output));

            event.addProvider(new TMMLBlockTagsProvider(output, lookupProvider, existingFileHelper));

            event.addProvider(new TMMLRecipeProvider.Runner(output, lookupProvider));

            event.addProvider(new TMMLItemModelProvider(output, existingFileHelper));
            event.addProvider(new TMMLModelProvider(output));

        } catch (RuntimeException e) {
            TMML.logger.error("Transport Module: Modular Logistics failed to gather data", e);
        }
    }
}