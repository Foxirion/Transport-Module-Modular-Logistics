package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

public class FluidTransportModule extends Item implements IFluidHandlerItem {
    public static final int MAX_FLUID_CAPACITY = 1000;
    public ItemStack container = ItemStack.EMPTY;

    public FluidTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos blockpos = blockhitresult.getBlockPos();

        return handleFluidStore(level, blockpos, itemstack, player);
    }

    public InteractionResultHolder handleFluidStore(Level level, BlockPos pos, ItemStack itemStack, Player player) {
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();

        if (fluidState.isEmpty()) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Send a message to the player if they try to pick up an unpickable fluid
        if (!fluidState.isSource()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Can only pick up source blocks."), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        // Store the fluid in the module
        FluidStack fluidToStore = new FluidStack(fluidState.getType(), MAX_FLUID_CAPACITY);


        // Actually fill the handler
        int filled = fill(fluidToStore, IFluidHandler.FluidAction.SIMULATE);
        if (filled > 0) {
            fill(fluidToStore, IFluidHandler.FluidAction.EXECUTE);

            // Remove the fluid from the world
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Picked up " + fluidToStore.getHoverName()), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    private FluidStack getStoredFluid(ItemStack stack) {
        SimpleFluidContent fluidContent = stack.get(TMMLDataComponents.FLUID_CONTENT);
        if (fluidContent != null && !fluidContent.isEmpty()) {
            return fluidContent.copy();
        }
        return FluidStack.EMPTY;
    }

    public InteractionResult handleFluidPlace(Level level, BlockPos pos, Direction clickedFace, ItemStack stack, Player player, UseOnContext context) {
        BlockPos placePos = pos.relative(clickedFace);

        if (!level.mayInteract(player, placePos)) {
            return InteractionResult.PASS;
        }

        // Get the stored fluid
        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (itemContents == ItemContainerContents.EMPTY || itemContents.getStackInSlot(0).getCount() < 1) {
            return InteractionResult.PASS;
        }

        try {
            ItemStack storedFluid = itemContents.getStackInSlot(0);
            if (storedFluid.isEmpty() || !(storedFluid.getItem() instanceof BucketItem)) {
                return InteractionResult.PASS;
            }

            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
            InteractionResult result = InteractionResult.sidedSuccess(((BucketItem) storedFluid.getItem()).emptyContents(player, level, placePos, null, storedFluid));

            // Decrease the stored fluid count
            storedFluid.setCount(storedFluid.getCount() - 1);
            if (storedFluid.getCount() == 0) {
                stack.remove(DataComponents.CONTAINER);
            } else {
                storedFluid = itemContents.getStackInSlot(0);
                stack.set(DataComponents.CONTAINER, itemContents);
            }

            return result;
        } catch (UnsupportedOperationException e) {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        //if (!getContainer().isEmpty()) {
        try {
            FluidStack fluidStack = new FluidStack(getStoredFluid(stack).getFluid(), MAX_FLUID_CAPACITY);
            if (!fluidStack.isEmpty()) {
                tooltipComponents.add(fluidStack.getHoverName());
                tooltipComponents.add(Component.literal("Capacity: " + fluidStack.getAmount() + "/" + MAX_FLUID_CAPACITY).withStyle(ChatFormatting.GRAY));
                return;
            }
        } catch (UnsupportedOperationException e) {
            // Handle the case where fluid doesn't exist
        }
        //}
        //tooltipComponents.add(Component.literal("[Empty]"));
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int i) {
        return getStoredFluid(getContainer());
    }

    @Override
    public int getTankCapacity(int i) {
        return MAX_FLUID_CAPACITY;
    }

    @Override
    public boolean isFluidValid(int i, FluidStack fluidStack) {
        return true;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        if (fluidStack.isEmpty() || !isFluidValid(0, fluidStack)) {
            return 0;
        }

        ItemStack container = getContainer();
        FluidStack storedFluid = getStoredFluid(container);

        if (fluidAction.simulate()) {
            if (storedFluid.isEmpty()) {
                return Math.min(MAX_FLUID_CAPACITY, fluidStack.getAmount());
            }
            if (!storedFluid.is(fluidStack.getFluidType())) {
                return 0;
            }
            return Math.min(MAX_FLUID_CAPACITY - storedFluid.getAmount(), fluidStack.getAmount());
        }

        if (storedFluid.isEmpty()) {
            storedFluid = new FluidStack(fluidStack.getFluid(), Math.min(MAX_FLUID_CAPACITY, fluidStack.getAmount()));
            return storedFluid.getAmount();
        }

        if (!storedFluid.is(fluidStack.getFluidType())) {
            return 0;
        }

        int filled = MAX_FLUID_CAPACITY - storedFluid.getAmount();
        if (fluidStack.getAmount() < filled) {
            storedFluid.grow(fluidStack.getAmount());
            filled = fluidStack.getAmount();
        } else {
            storedFluid.setAmount(MAX_FLUID_CAPACITY);
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        ItemStack container = getContainer();
        FluidStack storedFluid = getStoredFluid(container);

        if (fluidStack.isEmpty() || !storedFluid.is(fluidStack.getFluidType())) {
            return FluidStack.EMPTY;
        }
        return drain(fluidStack.getAmount(), fluidAction);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction fluidAction) {
        ItemStack container = getContainer();
        FluidStack storedFluid = getStoredFluid(container);

        if (storedFluid.getAmount() < maxDrain) {
            maxDrain = storedFluid.getAmount();
        }
        FluidStack stack = new FluidStack(storedFluid.getFluid(), maxDrain);
        if (fluidAction.execute() && maxDrain > 0) {
            storedFluid.shrink(maxDrain);
        }
        return stack;
    }
}