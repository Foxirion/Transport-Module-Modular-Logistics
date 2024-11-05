package net.foxirion.tmml.datagen.tags;

import net.foxirion.tmml.init.TMMLTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.foxirion.tmml.init.TMML.TMMLID;

public class TMMLBlockTagsProvider extends BlockTagsProvider {
    public TMMLBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TMMLID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)
                .addTags(BlockTags.FIRE, BlockTags.PORTALS)
                .add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.BARRIER, Blocks.BEDROCK, Blocks.REINFORCED_DEEPSLATE, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_VOID,
                        Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.MOVING_PISTON, Blocks.LIGHT, Blocks.RESPAWN_ANCHOR);
    }
}
