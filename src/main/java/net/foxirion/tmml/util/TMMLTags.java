package net.foxirion.tmml.util;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;
import net.foxirion.tmml.TMML;
import net.minecraft.util.Identifier;

public class TMMLTags {
    public static class BlockTags {
        public static final TagKey<Block> BLOCK_TRANSPORT_UNPICKABLE = createBlockTag("block_transport_unpickable");
    }

    // Register Tags
    public static TagKey<Block> createBlockTag(String name) {
        return TagKey.of(RegistryKeys.BLOCK, TMML.rl(name));
    }
}
