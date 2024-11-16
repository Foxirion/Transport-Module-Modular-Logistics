package net.foxirion.tmml.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.foxirion.tmml.util.TMMLTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class TMMLBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public TMMLBlockTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)
                .add(Blocks.AIR).add(Blocks.CAVE_AIR).add(Blocks.VOID_AIR) //Air Blocks
                .add(Blocks.FIRE).add(Blocks.SOUL_FIRE) //Fire Blocks
                .add(Blocks.END_PORTAL).add(Blocks.NETHER_PORTAL) //Portal Blocks
                .add(Blocks.BARRIER).add(Blocks.LIGHT)
                .add(Blocks.BEDROCK).add(Blocks.REINFORCED_DEEPSLATE)
                .add(Blocks.COMMAND_BLOCK).add(Blocks.CHAIN_COMMAND_BLOCK).add(Blocks.REPEATING_COMMAND_BLOCK)
                .add(Blocks.STRUCTURE_BLOCK).add(Blocks.STRUCTURE_VOID).add(Blocks.JIGSAW)
                .add(Blocks.TRIAL_SPAWNER)
                .add(Blocks.RESPAWN_ANCHOR);
    }
}
