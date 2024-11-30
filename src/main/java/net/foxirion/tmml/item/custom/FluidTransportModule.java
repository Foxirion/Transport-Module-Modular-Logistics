package net.foxirion.tmml.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
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

import java.util.List;

public class FluidTransportModule extends Item {
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
        Fluid existingFluid = getStoredFluid(stack);
        boolean isShiftKeyDown = player.isSneaking();
        boolean isWaterloggable = blockState.hasProperty(Properties.WATERLOGGED);

        // Placing fluid
        if (!existingFluid.isEmpty()) {
            // Shift-click partial fluid pickup logic (unchanged)
            if (isShiftKeyDown && existingFluid.getAmount() < MAX_FLUID_CAPACITY) {
                // Check if block clicked is a fluid source and matches current fluid
                if (!fluidState.isEmpty() && fluidState.isSource() &&
                        (existingFluid.isEmpty() || existingFluid.getFluid() == fluidState.getType())) {

                    // Calculate how much fluid can be added
                    int spaceRemaining = MAX_FLUID_CAPACITY - existingFluid.getAmount();
                    int fluidToAdd = Math.min(spaceRemaining, FluidType.BUCKET_VOLUME);

                    FluidStack updatedFluid = existingFluid.copy();
                    updatedFluid.grow(fluidToAdd);

                    // Update fluid in item
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.copyOf(updatedFluid));

                    // Remove fluid from world
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

                    if (!level.isClient) {
                        player.sendMessage(Text.literal("Picked up additional " + updatedFluid.getHoverName().getString()), true);
                    }

                    return InteractionResult.SUCCESS;
                }

                // If can't pick up, inform player
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Cannot pick up fluid"), true);
                }
                return InteractionResult.FAIL;
            }

            // Normal fluid placing logic
            if (existingFluid.getAmount() < FluidType.BUCKET_VOLUME) {
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Not enough fluid to place (Need " + FluidType.BUCKET_VOLUME + " mb)"), true);
                }
                return InteractionResult.FAIL;
            }

            // Check for waterloggable block first
            if (isWaterloggable && existingFluid.getFluid() == Fluids.WATER) {
                // Waterlog the block
                BlockState updatedState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                level.setBlock(pos, updatedState, 11);

                // Reduce fluid amount
                FluidStack updatedFluid = existingFluid.copy();
                updatedFluid.shrink(FluidType.BUCKET_VOLUME);

                // Update fluid content
                if (updatedFluid.isEmpty()) {
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.EMPTY);
                } else {
                    stack.set(TMMLDataComponents.FLUID_CONTENT, SimpleFluidContent.copyOf(updatedFluid));
                }

                return InteractionResult.SUCCESS;
            }

            // Original fluid placement logic
            BlockPos placePos = blockState.canBeReplaced(existingFluid.getFluid()) ? pos : pos.relative(direction);
            BlockState targetBlockState = level.getBlockState(placePos);

            // Check if the target block can be replaced
            if (targetBlockState.canBeReplaced(existingFluid.getFluid())) {
                // Try to place fluid without destroying existing blocks
                if (canPlaceFluid(level, placePos, targetBlockState, existingFluid.getFluid())) {
                    level.setBlock(placePos, existingFluid.getFluid().defaultFluidState().createLegacyBlock(), 11);

                    // Reduce fluid amount
                    FluidStack updatedFluid = existingFluid.copy();
                    updatedFluid.shrink(FluidType.BUCKET_VOLUME);

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
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            // Check for waterlogged block
            if (isWaterloggable && fluidState.getType() == Fluids.WATER) {
                // Create fluid stack
                FluidStack fluidToStore = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                SimpleFluidContent fluidContent = SimpleFluidContent.copyOf(fluidToStore);
                stack.set(TMMLDataComponents.FLUID_CONTENT, fluidContent);

                // Remove water from waterlogged state, but keep the block
                BlockState updatedState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
                level.setBlock(pos, updatedState, 11);

                return TypedActionResult.success(stack);
            }

            // Original fluid pickup logic
            if (existingFluid.isEmpty()) {
                FluidStack fluidToStore = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                SimpleFluidContent fluidContent = SimpleFluidContent.copyOf(fluidToStore);
                stack.set(TMMLDataComponents.FLUID_CONTENT, fluidContent);

                // Remove the fluid from the world
                level.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

                if (!level.isClient) {
                    player.sendMessage(Text.literal("Picked up " + fluidToStore.getHoverName().getString()), true);
                }

                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClient) {
                    player.sendMessage(Text.literal("Cannot pick up fluid. Container is not empty."), true);
                }
                return InteractionResult.FAIL;
            }
        }

        return ActionResult.FAIL;
    }

    public boolean canPlaceFluid(World level, BlockPos pos, BlockState blockState, Fluid fluid) {
        // Check if the block can be replaced
        if (blockState.canBeReplaced(fluid)) {
            if (blockState.getBlock() instanceof LiquidBlockContainer) {
                LiquidBlockContainer container = (LiquidBlockContainer) blockState.getBlock();
                return container.canPlaceLiquid(null, level, pos, blockState, fluid);
            }
            return true;
        }
        return false;
    }

    public Fluids getStoredFluid(ItemStack stack) {
        SimpleFluidContent fluidContent = stack.get(TMMLDataComponents.FLUID_CONTENT);
        if (fluidContent != null && !fluidContent.isEmpty()) {
            return fluidContent.copy();
        }
        return Fluids.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Fluids fluidStack = getStoredFluid(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(fluidStack.getHoverName());
            tooltip.add(Text.literal("Capacity: " + fluidStack.getAmount() + "/" + MAX_FLUID_CAPACITY + " mb").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Text.literal("[Empty]"));
    }
}
