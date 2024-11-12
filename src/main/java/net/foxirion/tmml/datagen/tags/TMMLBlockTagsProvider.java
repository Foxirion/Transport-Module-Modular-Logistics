package net.foxirion.tmml.datagen.tags;

import net.foxirion.tmml.init.TMMLTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;  // Fixed extra semicolon here
import net.minecraftforge.common.data.ExistingFileHelper;

public class TMMLBlockTagsProvider extends BlockTagsProvider {
    public TMMLBlockTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, "tmml", existingFileHelper); // "tmml" is your mod ID
    }

    @Override
    protected void addTags() {
        // Adding your custom tag and blocks
        tag(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)
                .addTags(BlockTags.FIRE, BlockTags.PORTALS)
                .add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.BARRIER, Blocks.BEDROCK,
                        Blocks.REINFORCED_DEEPSLATE, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
                        Blocks.STRUCTURE_VOID, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK,
                        Blocks.JIGSAW, Blocks.MOVING_PISTON, Blocks.LIGHT, Blocks.RESPAWN_ANCHOR);
    }
}