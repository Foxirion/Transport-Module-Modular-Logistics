package net.foxirion.tmml.datacomponents;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "tmml");

    public static final Supplier<DataComponentType<ItemStack>> BLOCK_TRANSPORT_MODULE =
            COMPONENTS.registerComponentType(
                    "transported_block",
                    builder -> builder
                            .persistent(ItemStack.CODEC)
                            .networkSynchronized(ItemStack.STREAM_CODEC)
            );
}