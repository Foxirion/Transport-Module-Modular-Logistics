package net.foxirion.tmml.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TMMLTags {
    public static class BlockTags {
        public static final TagKey<Block> BLOCK_TRANSPORT_UNPICKABLE = createBlockTag("block_transport_unpickable");
    }

    // Register Tags
    public static TagKey<Block> createBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, TMML.rl(name));
    }
}
