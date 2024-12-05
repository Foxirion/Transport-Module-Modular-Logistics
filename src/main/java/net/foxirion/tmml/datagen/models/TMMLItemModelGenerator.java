package net.foxirion.tmml.datagen.models;

import net.foxirion.tmml.init.TMML;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
public class TMMLItemModelGenerator {
    public final ItemModelOutput output;
    public final BiConsumer<ResourceLocation, ModelInstance> modelOutput;
    public TMMLItemModelGenerator(ItemModelOutput output, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        this.output = output;
        this.modelOutput = modelOutput;
    }
    public void run() {

        //Void Bottle
        generateFlatItem(TMMLItems.VOID_BOTTLE.get(), ModelTemplates.FLAT_ITEM);

        //Modules
        generateFlatItem(TMMLItems.BLOCK_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);
        generateFlatItem(TMMLItems.FLUID_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);
        generateFlatItem(TMMLItems.ENTITY_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);

    }
    // Methods
    public ResourceLocation createFlatItemModel(Item item, ModelTemplate template) {
        return template.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), this.modelOutput);
    }
    public void generateFlatItem(Item item, ModelTemplate template) {
        this.output.accept(item, ItemModelUtils.plainModel(this.createFlatItemModel(item, template)));
    }
}
