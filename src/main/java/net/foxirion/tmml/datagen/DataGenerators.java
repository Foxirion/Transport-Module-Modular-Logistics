package net.foxirion.tmml.datagen;

import net.foxirion.tmml.datagen.tags.TMMLBlockTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

import static net.foxirion.tmml.init.TMML.TMMLID;

@Mod.EventBusSubscriber(modid = TMMLID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new LangProvider(packOutput));

        generator.addProvider(true, new net.foxirion.tmml.datagen.recipe.TMMLRecipeProvider(packOutput));

        generator.addProvider(true, new TMMLItemModelProvider(packOutput, existingFileHelper));

        TMMLBlockTagsProvider blockTagGenerator = generator.addProvider(event.includeServer(), new TMMLBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
    }
}