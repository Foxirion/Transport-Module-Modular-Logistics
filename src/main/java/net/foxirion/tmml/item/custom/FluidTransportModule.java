package net.foxirion.tmml.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.List;

public class FluidTransportModule extends Item {
    // UNCHANGED: Maximum fluid capacity
    public static final int MAX_FLUID_CAPACITY = 10000;

    // CHANGED: NBT key for fluid storage instead of data components
    public static final String FLUID_NBT_KEY = "StoredFluid";

    // UNCHANGED: Constructor
    public FluidTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    // UNCHANGED: Use method calling handleFluidInteraction
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos blockpos = blockhitresult.getBlockPos();
        Direction direction = blockhitresult.getDirection();

        return handleFluidInteraction(level, blockpos, direction, itemstack, player);
    }

    // MOSTLY UNCHANGED: Fluid interaction logic
    public InteractionResultHolder handleFluidInteraction(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player) {
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();

        // CHANGED: Use new getStoredFluid method
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

                    // CHANGED: Use new saveFluidToNBT method instead of data components
                    saveFluidToNBT(stack, updatedFluid);

                    // Remove fluid from world
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("Picked up additional " + updatedFluid.getDisplayName().getString()), true);
                    }

                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
                }

                // If can't pick up, inform player
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Cannot pick up fluid"), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            // Normal fluid placing logic
            if (existingFluid.getAmount() < FluidType.BUCKET_VOLUME) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Not enough fluid to place (Need " + FluidType.BUCKET_VOLUME + " mb)"), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            // Check for waterloggable block first
            if (isWaterloggable && existingFluid.getFluid() == Fluids.WATER) {
                // Waterlog the block
                BlockState updatedState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                level.setBlock(pos, updatedState, 11);

                // Reduce fluid amount
                FluidStack updatedFluid = existingFluid.copy();
                updatedFluid.shrink(FluidType.BUCKET_VOLUME);

                // CHANGED: Use saveFluidToNBT method
                saveFluidToNBT(stack, updatedFluid);

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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

                    // CHANGED: Use saveFluidToNBT method
                    saveFluidToNBT(stack, updatedFluid);

                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("Placed " + existingFluid.getDisplayName().getString()), true);
                    }

                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
                }
            }

            return InteractionResultHolder.fail(stack);
        }

        // Picking up fluid
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            // Check for waterlogged block
            if (isWaterloggable && fluidState.getType() == Fluids.WATER) {
                // Create fluid stack
                FluidStack fluidToStore = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);

                // CHANGED: Use saveFluidToNBT method
                saveFluidToNBT(stack, fluidToStore);

                // Remove water from waterlogged state, but keep the block
                BlockState updatedState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
                level.setBlock(pos, updatedState, 11);

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            // Original fluid pickup logic
            if (existingFluid.isEmpty()) {
                FluidStack fluidToStore = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);

                // CHANGED: Use saveFluidToNBT method
                saveFluidToNBT(stack, fluidToStore);

                // Remove the fluid from the world
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Picked up " + fluidToStore.getDisplayName().getString()), true);
                }

                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Cannot pick up fluid. Container is not empty."), true);
                }
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    // UNCHANGED: Fluid placement check
    public boolean canPlaceFluid(Level level, BlockPos pos, BlockState blockState, Fluid fluid) {
        if (blockState.canBeReplaced(fluid)) {
            if (blockState.getBlock() instanceof LiquidBlockContainer) {
                LiquidBlockContainer container = (LiquidBlockContainer) blockState.getBlock();
                return container.canPlaceLiquid(level, pos, blockState, fluid);
            }
            return true;
        }
        return false;
    }

    // CHANGED: Fluid storage retrieval using NBT
    public FluidStack getStoredFluid(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(FLUID_NBT_KEY)) {
            return FluidStack.loadFluidStackFromNBT(nbt.getCompound(FLUID_NBT_KEY));
        }
        return FluidStack.EMPTY;
    }

    // UNCHANGED: Tooltip method
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        FluidStack fluidStack = getStoredFluid(stack);
        if (!fluidStack.isEmpty()) {
            tooltipComponents.add(fluidStack.getDisplayName());
            tooltipComponents.add(Component.literal("Capacity: " + fluidStack.getAmount() + "/" + MAX_FLUID_CAPACITY + " mb").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltipComponents.add(Component.literal("[Empty]"));
    }

    // NEW: Method to save fluid to NBT
    public void saveFluidToNBT(ItemStack stack, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            stack.removeTagKey(FLUID_NBT_KEY);
        } else {
            CompoundTag fluidNBT = fluidStack.writeToNBT(new CompoundTag());
            stack.getOrCreateTag().put(FLUID_NBT_KEY, fluidNBT);
        }
    }
}