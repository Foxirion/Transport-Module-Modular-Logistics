package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.SimpleFluidContent;
import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
        ItemStack itemstack = player.getMainHandStack();
        BlockHitResult blockHitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        BlockPos blockpos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();

        return handleFluidInteraction(world, blockpos, direction, itemstack, player);
    }

    public TypedActionResult<ItemStack> handleFluidInteraction(World level, BlockPos pos, Direction direction, ItemStack stack, PlayerEntity player) {
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();
        SimpleFluidContent.FluidStack existingFluid = getStoredFluid(stack);
        boolean isShiftKeyDown = player.isSneaking();
        boolean isWaterloggable = blockState.get(Properties.WATERLOGGED);

        // Placing fluid
        if (!existingFluid.isEmpty()) {
            // Shift-click partial fluid pickup logic (unchanged)
            if (isShiftKeyDown && existingFluid.getAmount() < MAX_FLUID_CAPACITY) {
                // Check if block clicked is a fluid source and matches current fluid
                if (!fluidState.isEmpty() && !fluidState.isStill() &&
                        (existingFluid.isEmpty() || existingFluid.getFluid() == fluidState.getFluid())) {

                    // Calculate how much fluid can be added
                    int spaceRemaining = MAX_FLUID_CAPACITY - existingFluid.getAmount();
                    int fluidToAdd = Math.min(spaceRemaining, BUCKET_VOLUME);

                    SimpleFluidContent.FluidStack updatedFluid = existingFluid.copy();
                    //updatedFluid.grow(fluidToAdd);

                    // Update fluid in item
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.copyOf(updatedFluid));

                    // Remove fluid from world
                    level.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

                    if (!level.isClient) {
                        player.sendMessage(Text.literal("Picked up additional " + updatedFluid.getHoverName().getString()), true);
                    }

                    return TypedActionResult.success(stack);
                }

                // If can't pick up, inform player
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Cannot pick up fluid"), true);
                }
                return TypedActionResult.fail(stack);
            }

            // Normal fluid placing logic
            if (existingFluid.getAmount() < BUCKET_VOLUME) {
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Not enough fluid to place (Need " + BUCKET_VOLUME + " mb)"), true);
                }
                return TypedActionResult.fail(stack);
            }

            // Check for waterloggable block first
            if (isWaterloggable && existingFluid.getFluid() == Fluids.WATER) {
                // Waterlog the block
                //BlockState updatedState = blockState.setValue(Properties.WATERLOGGED, true);
                //level.setBlockState(pos, updatedState, 11);

                // Reduce fluid amount
                SimpleFluidContent.FluidStack updatedFluid = existingFluid.copy();
                //updatedFluid.shrink(BUCKET_VOLUME);

                // Update fluid content
                if (updatedFluid.isEmpty()) {
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.EMPTY);
                } else {
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.copyOf(updatedFluid));
                }

                return TypedActionResult.success(stack);
            }

            // Original fluid placement logic
            BlockPos placePos = blockState.canBucketPlace(existingFluid.getFluid()) ? pos : pos.offset(direction);
            BlockState targetBlockState = level.getBlockState(placePos);

            // Check if the target block can be replaced
            if (targetBlockState.canBucketPlace(existingFluid.getFluid())) {
                // Try to place fluid without destroying existing blocks
                if (canPlaceFluid(level, placePos, targetBlockState, existingFluid.getFluid())) {
                    level.setBlockState(placePos, existingFluid.getFluid().getDefaultState().getBlockState(), 11);

                    // Reduce fluid amount
                    SimpleFluidContent.FluidStack updatedFluid = existingFluid.copy();
                    //updatedFluid.shrink(BUCKET_VOLUME);

                    // Update fluid content
                    if (updatedFluid.isEmpty()) {
                        stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.EMPTY);
                    } else {
                        stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.copyOf(updatedFluid));
                    }

                    if (!level.isClient) {
                        player.sendMessage(Text.literal("Placed " + existingFluid.getHoverName().getString()), true);
                    }

                    return TypedActionResult.success(stack);
                }
            }

            return TypedActionResult.fail(stack);
        }

        // Picking up fluid
        if (!fluidState.isEmpty() && !fluidState.isStill()) {
            // Check for waterlogged block
            if (isWaterloggable && fluidState.getFluid() == Fluids.WATER) {
                // Create fluid stack
                SimpleFluidContent.FluidStack fluidToStore = new SimpleFluidContent.FluidStack(fluidState.getFluid(), BUCKET_VOLUME);
                SimpleFluidContent fluidContent = SimpleFluidContent.copyOf(fluidToStore);
                stack.set(TMMLDataComponents.FLUID_CONTENT, fluidContent);

                // Remove water from waterlogged state, but keep the block
/*                BlockState updatedState = blockState.setValue(Properties.WATERLOGGED, false);
                level.setBlockState(pos, updatedState, 11);*/

                return TypedActionResult.success(stack);
            }

            // Original fluid pickup logic
            if (existingFluid.isEmpty()) {
                SimpleFluidContent.FluidStack fluidToStore = new SimpleFluidContent.FluidStack(fluidState.getFluid(), BUCKET_VOLUME);
                SimpleFluidContent fluidContent = SimpleFluidContent.copyOf(fluidToStore);
                stack.set(TMMLDataComponents.FLUID_CONTENT, fluidContent);

                // Remove the fluid from the world
                level.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

                if (!level.isClient) {
                    player.sendMessage(Text.literal("Picked up " + fluidToStore), true);
                }

                return TypedActionResult.success(stack);
            } else {
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Cannot pick up fluid. Container is not empty."), true);
                }
                return TypedActionResult.fail(stack);
            }
        }

        return TypedActionResult.fail(stack);
    }

    public boolean canPlaceFluid(World level, BlockPos pos, BlockState blockState, Fluid fluid) {
        // Check if the block can be replaced
        if (blockState.canBucketPlace(fluid)) {
/*            if (blockState.getBlock() instanceof FluidBlock) {
                FluidBlock container = (FluidBlock) blockState.getBlock();
                return container.(null, level, pos, blockState, fluid);
            }*/
            return true;
        }
        return false;
    }

    public SimpleFluidContent.FluidStack getStoredFluid(ItemStack stack) {
        SimpleFluidContent fluidContent = stack.get(TMMLDataComponents.FLUID_CONTENT);
        if (fluidContent != null && !fluidContent.isEmpty()) {
            return fluidContent.copy();
        }
        return SimpleFluidContent.FluidStack.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        SimpleFluidContent.FluidStack fluidStack = getStoredFluid(stack);
        if (!fluidStack.isEmpty()) {
            //tooltip.add(fluidStack);
            tooltip.add(Text.literal("Capacity: " + fluidStack.getAmount() + "/" + MAX_FLUID_CAPACITY + " mb").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Text.literal("[Empty]"));
    }
}
