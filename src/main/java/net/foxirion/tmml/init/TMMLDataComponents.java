package net.foxirion.tmml.init;

import net.foxirion.tmml.TMML;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class TMMLDataComponents {
/*    public static final ComponentType<SimpleFluidContent> FLUID_CONTENT = register("fluid_content", (builder) -> {
        return builder.codec(SimpleFluidContent.CODEC).packetCodec(SimpleFluidContent.STREAM_CODEC).cache();
    });*/

    public static final ComponentType<Identifier> FLUID_CONTENT = register("fluid_content",
            (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC).cache()
    );

    public static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return (ComponentType) Registry.register(Registries.DATA_COMPONENT_TYPE, TMML.rl(id), ((ComponentType.Builder) builderOperator.apply(ComponentType.builder())).build());
    }
}
