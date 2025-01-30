package net.foxirion.tmml.datagen;

import net.foxirion.tmml.datagen.lang.LangProvider;
import net.foxirion.tmml.datagen.models.TMMLModelProvider;
import net.foxirion.tmml.datagen.recipe.TMMLRecipeProvider;
import net.foxirion.tmml.datagen.tags.TMMLBlockTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static net.foxirion.tmml.init.TMML.TMMLID;

@EventBusSubscriber(modid = TMMLID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            event.addProvider(new LangProvider(output));

            event.addProvider(new TMMLModelProvider(output));

            TMMLBlockTagsProvider modBlockTagsProvider = new TMMLBlockTagsProvider(output, lookupProvider);
            event.addProvider(modBlockTagsProvider);

            event.addProvider(new TMMLRecipeProvider.Runner(output, lookupProvider));

    }
}