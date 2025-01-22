package net.foxirion.tmml.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TMMLDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TMML.TMMLID);

    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID_CONTENT = COMPONENTS.registerComponentType(
            "fluid_content",
            builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<CompoundTag>> ENTITY_CONTENT = COMPONENTS.registerComponentType(
            "entity_content",
            builder -> builder.persistent(CompoundTag.CODEC)
    );
}
