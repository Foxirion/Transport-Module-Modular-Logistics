package net.foxirion.tmml.init;

import com.mojang.serialization.Codec;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Predicate;

public class SimpleFluidContent {
    public static final SimpleFluidContent EMPTY;
    public static final Codec<SimpleFluidContent> CODEC;
    public static final PacketCodec<RegistryByteBuf, SimpleFluidContent> STREAM_CODEC;
    private final FluidStack fluidStack;

    private SimpleFluidContent(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public static SimpleFluidContent copyOf(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
    }

    public FluidStack copy() {
        return this.fluidStack.copy();
    }

    public boolean isEmpty() {
        return this.fluidStack.isEmpty();
    }

    public Fluid getFluid() {
        return this.fluidStack.getFluid();
    }

    public int getAmount() {
        return this.fluidStack.getAmount();
    }

    public boolean matches(FluidStack other) {
        return FluidStack.matches(this.fluidStack, other);
    }

    public DataComponentMap getComponents() {
        return this.fluidStack.getComponents();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof SimpleFluidContent) {
            SimpleFluidContent o = (SimpleFluidContent)obj;
            return FluidStack.matches(this.fluidStack, o.fluidStack);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.fluidStack.getAmount() * 31 + FluidStack.hashFluidAndComponents(this.fluidStack);
    }

    static {
        EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
        CODEC = FluidStack.OPTIONAL_CODEC.xmap(SimpleFluidContent::new, (content) -> content.fluidStack);
        STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(SimpleFluidContent::new, (content) -> content.fluidStack);
    }

    public static class FluidStack {
        public static final FluidStack EMPTY = new FluidStack(null, 0);

        private Fluid fluid;
        private int amount;

        public FluidStack(Fluid fluid, int amount) {
            this.fluid = fluid;
            this.amount = amount;
        }

        public Fluid getFluid() {
            return fluid;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isEmpty() {
            return fluid == null || amount <= 0;
        }

        public FluidStack copy() {
            return new FluidStack(fluid, amount);
        }

        public static boolean matches(FluidStack a, FluidStack b) {
            return a.fluid == b.fluid && a.amount == b.amount;
        }
    }
}

