package net.foxirion.tmml.datagen;

import net.foxirion.tmml.datagen.recipe.TMMLRecipeProvider;
import net.foxirion.tmml.datagen.tags.TMMLBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.foxirion.tmml.init.TMML.TMMLID;

@Mod.EventBusSubscriber(modid = TMMLID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new TMMLItemModelProvider(generator, existingFileHelper));  // Item model provider
        generator.addProvider(event.includeServer(), new TMMLRecipeProvider(generator));  // Registering recipes
        generator.addProvider(event.includeServer(), new LangProvider(generator));  // Registering language files
        generator.addProvider(event.includeServer(), new TMMLBlockTagsProvider(generator, existingFileHelper));  // Block tags
    }
}
