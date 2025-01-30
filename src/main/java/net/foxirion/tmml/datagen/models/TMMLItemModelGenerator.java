package net.foxirion.tmml.datagen.models;

import net.foxirion.tmml.init.TMML;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
public class TMMLItemModelGenerator extends ItemModelGenerators {

    public TMMLItemModelGenerator(ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        super(itemModelOutput, modelOutput);
    }

    @Override
    public void run() {

        //Void Bottle
        generateFlatItem(TMMLItems.VOID_BOTTLE.get(), ModelTemplates.FLAT_ITEM);

        //Modules
        generateFlatItem(TMMLItems.BLOCK_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);
        generateFlatItem(TMMLItems.FLUID_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);
        generateFlatItem(TMMLItems.ENTITY_TRANSPORT_MODULE.get(), TMMLModelTemplates.TRANSPORT_MODULE_ITEM);

    }
}
