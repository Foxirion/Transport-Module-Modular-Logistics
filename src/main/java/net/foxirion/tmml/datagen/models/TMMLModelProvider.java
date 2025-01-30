package net.foxirion.tmml.datagen.models;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static net.foxirion.tmml.init.TMML.TMMLID;

@OnlyIn(Dist.CLIENT)
public class TMMLModelProvider extends ModelProvider {
    public TMMLModelProvider(PackOutput output) {
        super(output, TMMLID);
    }
    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        new TMMLItemModelGenerator(itemModels.itemModelOutput, itemModels.modelOutput).run();
    }
}