package net.foxirion.tmml.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockTransportModule extends Item {
    public BlockTransportModule(Properties properties) {
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

        if (player.isShiftKeyDown()) {
            return handleBlockStore(level, pos, stack, player);
        } else {
            return handleBlockPlace(level, pos, clickedFace, stack, player, context);
        }
    }

    public InteractionResult handleBlockStore(Level level, BlockPos pos, ItemStack transportModule, Player player) {
        BlockState blockState = level.getBlockState(pos);

        if (blockState.isAir() || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }

        ItemStack blockStack = new ItemStack(blockState.getBlock().asItem());

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            CustomData blockEntityTag = blockEntity.components().get(DataComponents.BLOCK_ENTITY_DATA);
            blockStack.set(DataComponents.BLOCK_ENTITY_DATA, blockEntityTag);
        }

        NonNullList<ItemStack> itemList = NonNullList.withSize(1, ItemStack.EMPTY);
        itemList.set(0, blockStack);

        transportModule.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(itemList));

        // Remove the block from the world
        level.removeBlock(pos, false);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public InteractionResult handleBlockPlace(Level level, BlockPos pos, Direction clickedFace, ItemStack stack, Player player, UseOnContext context) {
        BlockPos placePos = pos.relative(clickedFace);

        if (!level.isEmptyBlock(placePos) || !level.mayInteract(player, placePos)) {
            return InteractionResult.PASS;
        }

        // Get the stored block
        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (itemContents == ItemContainerContents.EMPTY) {
            return InteractionResult.PASS;
        }

        try {
            ItemStack storedBlock = itemContents.getStackInSlot(0);
            if (storedBlock.isEmpty() || !(storedBlock.getItem() instanceof BlockItem)) {
                return InteractionResult.PASS;
            }

            // Place the block
            BlockItem blockItem = (BlockItem) storedBlock.getItem();
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
            InteractionResult result = blockItem.place(blockPlaceContext);

            if (result.consumesAction()) {
                stack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
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
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Handle the case where slot 0 doesn't exist
            }
        }
        tooltipComponents.add(Component.literal("[Empty]"));
    }
}