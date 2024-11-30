package net.foxirion.tmml.item.custom;

import com.mojang.serialization.Codec;
import net.foxirion.tmml.init.SimpleFluidContent;
import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.block.*;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidTransportModule extends Item {
    public static final int BUCKET_VOLUME = 1000;
    public static final int MAX_FLUID_CAPACITY = 10000;

    public FluidTransportModule() {
        super(new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();

        return handleFluidInteraction(world, blockPos, direction, itemStack, player);
    }

    private TypedActionResult<ItemStack> handleFluidInteraction(World world, BlockPos pos, Direction direction, ItemStack stack, PlayerEntity player) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();
        Fluid storedFluid = getStoredFluid(stack);

        // Fluid Placement Logic
        if (storedFluid != Fluids.EMPTY) {
            BlockPos placePos = blockState.canBucketPlace(storedFluid) ? pos : pos.offset(direction);

            if (canPlaceFluid(world, placePos, storedFluid)) {
                // Place fluid block
                world.setBlockState(placePos, storedFluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL);

                // Clear stored fluid
                stack = stack.get(TMMLDataComponents.FLUID_CONTENT);

                if (!world.isClient) {
                    player.sendMessage(Text.literal("Placed " + storedFluid.toString()), true);
                }

                return TypedActionResult.success(stack);
            }
        }

        // Fluid Pickup Logic
        if (!fluidState.isEmpty() && fluidState.isStill()) {
            Fluid fluidToStore = fluidState.getFluid();

            // Check if fluid can be stored
            if (storedFluid == Fluids.EMPTY) {
                // Store fluid identifier as a string
                stack = stack.set(TMMLDataComponents.FLUID_CONTENT, Registries.FLUID.getId(fluidToStore).toString());

                // Remove fluid from world
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                if (!world.isClient) {
                    player.sendMessage(Text.literal("Picked up " + fluidToStore.toString()), true);
                }

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.fail(stack);
    }

    private Fluid getStoredFluid(ItemStack stack) {
        String fluidId = stack.get(TMMLDataComponents.FLUID_CONTENT);
        if (fluidId != null) {
            Identifier identifier = Identifier.tryParse(fluidId);
            return identifier != null ? Registries.FLUID.get(identifier) : Fluids.EMPTY;
        }
        return Fluids.EMPTY;
    }

    private boolean canPlaceFluid(World world, BlockPos pos, Fluid fluid) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.canBucketPlace(fluid) &&
                (blockState.isAir() || blockState.getBlock() instanceof FluidFillable);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Fluid storedFluid = getStoredFluid(stack);
        if (storedFluid != Fluids.EMPTY) {
            tooltip.add(Text.literal("Fluid: " + storedFluid.toString()).formatted(Formatting.BLUE));
        } else {
            tooltip.add(Text.literal("[Empty]").formatted(Formatting.GRAY));
        }
    }
}