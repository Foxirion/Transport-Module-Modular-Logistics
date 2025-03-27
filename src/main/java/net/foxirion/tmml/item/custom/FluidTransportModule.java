package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;
import java.util.function.Consumer;

public class FluidTransportModule extends Item {
    public static final int MAX_FLUID_CAPACITY = 10000;

    public FluidTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos blockpos = blockhitresult.getBlockPos();
        Direction direction = blockhitresult.getDirection();

        return handleFluidInteraction(level, blockpos, direction, itemstack, player);
    }

    public InteractionResult handleFluidInteraction(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player) {
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();
        FluidStack existingFluid = getStoredFluid(stack);
        boolean isShiftKeyDown = player.isShiftKeyDown();
        boolean isWaterloggable = blockState.hasProperty(BlockStateProperties.WATERLOGGED);

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

                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("Picked up additional " + updatedFluid.getHoverName().getString()), true);
                    }

                    return InteractionResult.SUCCESS;
                }

                // If can't pick up, inform player
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Cannot pick up fluid"), true);
                }
                return InteractionResult.FAIL;
            }

            // Normal fluid placing logic
            if (existingFluid.getAmount() < FluidType.BUCKET_VOLUME) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Not enough fluid to place (Need " + FluidType.BUCKET_VOLUME + " mb)"), true);
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

                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("Placed " + existingFluid.getHoverName().getString()), true);
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.FAIL;
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

                return InteractionResult.SUCCESS;
            }

            // Original fluid pickup logic
            if (existingFluid.isEmpty()) {
                FluidStack fluidToStore = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
                SimpleFluidContent fluidContent = SimpleFluidContent.copyOf(fluidToStore);
                stack.set(TMMLDataComponents.FLUID_CONTENT, fluidContent);

                // Remove the fluid from the world
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Picked up " + fluidToStore.getHoverName().getString()), true);
                }

                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Cannot pick up fluid. Container is not empty."), true);
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.FAIL;
    }

    public boolean canPlaceFluid(Level level, BlockPos pos, BlockState blockState, Fluid fluid) {
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

    public FluidStack getStoredFluid(ItemStack stack) {
        SimpleFluidContent fluidContent = stack.get(TMMLDataComponents.FLUID_CONTENT);
        if (fluidContent != null && !fluidContent.isEmpty()) {
            return fluidContent.copy();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        FluidStack fluidStack = getStoredFluid(stack);
        if (!fluidStack.isEmpty()) {
            tooltipComponents.accept(fluidStack.getHoverName());
            tooltipComponents.accept(Component.literal("Capacity: " + fluidStack.getAmount() + "/" + MAX_FLUID_CAPACITY + " mb").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltipComponents.accept(Component.literal("[Empty]"));
    }
}