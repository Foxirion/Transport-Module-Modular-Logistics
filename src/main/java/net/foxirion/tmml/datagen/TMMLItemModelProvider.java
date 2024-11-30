package net.foxirion.tmml.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class TMMLItemModelProvider extends FabricModelProvider {
    public TMMLItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(TMMLItems.VOID_BOTTLE, Models.GENERATED);
        itemModelGenerator.register(TMMLItems.BLOCK_TRANSPORT_MODULE, TMMLModels.TRANSPORT_MODULE);
        itemModelGenerator.register(TMMLItems.ENTITY_TRANSPORT_MODULE, TMMLModels.TRANSPORT_MODULE);
        itemModelGenerator.register(TMMLItems.FLUID_TRANSPORT_MODULE, TMMLModels.TRANSPORT_MODULE);
    }
}
