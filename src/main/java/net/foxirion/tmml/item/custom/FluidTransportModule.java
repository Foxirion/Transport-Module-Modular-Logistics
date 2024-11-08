package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class FluidTransportModule extends Item {
    private static final int MAX_FLUID_CAPACITY = 10;

    public FluidTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        Direction clickedFace = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (player.isShiftKeyDown()) {
            // If module is not empty, prevent picking up another fluid
            if (itemContents != ItemContainerContents.EMPTY && itemContents.getStackInSlot(0).getCount() >= MAX_FLUID_CAPACITY) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Module is full, please empty it before storing another fluid."), true);
                }
                return InteractionResult.FAIL;
            }
            return handleFluidStore(level, pos, stack, player);
        }
        return handleFluidPlace(level, pos, clickedFace, stack, player, context);
    }

    public InteractionResult handleFluidStore(Level level, BlockPos pos, ItemStack fluidModule, Player player) {
        BlockState blockState = level.getBlockState(pos);
        Fluid fluid = blockState.getFluidState().getType();

        // Send a message to the player if they try to pick up an unpickable fluid
        if (fluid == Fluids.EMPTY || !level.mayInteract(player, pos) || fluid.is(TMMLTags.FluidTags.FLUID_TRANSPORT_UNPICKABLE)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.literal("This fluid cannot be picked up."), true);
            }
            return InteractionResult.FAIL;
        }

        // Create ItemStack from the fluid
        ItemStack fluidStack = new ItemStack(fluid.getBucket().asItem());

        // Store the fluid in the module
        NonNullList<ItemStack> itemList = NonNullList.withSize(1, fluidStack);
        ItemContainerContents itemContents = ItemContainerContents.fromItems(itemList);
        fluidModule.set(DataComponents.CONTAINER, itemContents);

        // Remove the fluid from the world and prevent items from dropping if container
        if (blockState.getBlock() instanceof Container container) {
            container.clearContent();
        }
        level.removeBlock(pos, false);

        return InteractionResult.sidedSuccess(level.isClientSide);
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
                itemContents.setStackInSlot(0, storedFluid);
                stack.set(DataComponents.CONTAINER, itemContents);
            }

            return result;
        } catch (UnsupportedOperationException e) {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (itemContents != ItemContainerContents.EMPTY) {
            try {
                ItemStack storedStack = itemContents.getStackInSlot(0);
                if (!storedStack.isEmpty()) {
                    tooltipComponents.add(storedStack.getDisplayName());
                    tooltipComponents.add(Component.literal("Capacity: " + itemContents.getStackInSlot(0).getCount() + "/" + MAX_FLUID_CAPACITY).withStyle(ChatFormatting.GRAY));
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Handle the case where slot 0 doesn't exist
            }
        }
        tooltipComponents.add(Component.literal("[Empty]"));
    }
}