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
import net.minecraft.world.Container;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (player.isShiftKeyDown()) {
            // If module is not empty, prevent picking up another block
            if (itemContents != ItemContainerContents.EMPTY) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Module must be empty before storing another block"), true);
                }
                return InteractionResult.FAIL;
            }
            return handleBlockStore(level, pos, stack, player);
        }
        return handleBlockPlace(level, pos, clickedFace, stack, player, context);
    }

    public InteractionResult handleBlockStore(Level level, BlockPos pos, ItemStack transportModule, Player player) {
        BlockState blockState = level.getBlockState(pos);

        // Send a message to the player if they try to pick up an unpickable block
        if (blockState.isAir() || !level.mayInteract(player, pos) || blockState.is(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.literal("This block cannot be picked up."), true);
            }
            return InteractionResult.FAIL;
        }

        // Create ItemStack from the block
        ItemStack blockStack = new ItemStack(blockState.getBlock().asItem());

        // Handle BlockEntity (NBT) data
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            HolderLookup.Provider registries = level.registryAccess();
            CompoundTag nbt = blockEntity.saveWithFullMetadata(registries);
            CustomData blockEntityData = CustomData.of(nbt);
            // Store the BlockEntity data in the ItemStack
            blockStack.set(DataComponents.BLOCK_ENTITY_DATA, blockEntityData);
        }

        // Store the block in the module
        NonNullList<ItemStack> itemList = NonNullList.withSize(1, ItemStack.EMPTY);
        itemList.set(0, blockStack);
        transportModule.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(itemList));

        // Remove the block from the world and prevent items from dropping if container
        if (blockEntity instanceof Container container) {
            container.clearContent();
        }
        level.removeBlock(pos, false);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public InteractionResult handleBlockPlace(Level level, BlockPos pos, Direction clickedFace, ItemStack stack, Player player, UseOnContext context) {
        BlockPos placePos = pos.relative(clickedFace);

        if (!level.mayInteract(player, placePos)) {
            return InteractionResult.PASS;
        }

        // Get the stored block
        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (itemContents == ItemContainerContents.EMPTY) {
            return InteractionResult.PASS;
        }

        try {
            ItemStack storedBlock = itemContents.getStackInSlot(0);
            if (storedBlock.isEmpty() || !(storedBlock.getItem() instanceof BlockItem blockItem)) {
                return InteractionResult.PASS;
            }

            // Get the NBT data before placing
            CustomData blockEntityData = storedBlock.get(DataComponents.BLOCK_ENTITY_DATA);
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
            InteractionResult result = InteractionResult.SUCCESS;
            if (level.getBlockState(placePos).canBeReplaced()) {
                BlockState blockState = blockItem.getBlock().getStateForPlacement(blockPlaceContext);
                level.removeBlock(placePos, false);
                level.setBlock(placePos, blockState, 3);
                SoundType soundtype = blockState.getSoundType();
                level.playSound(null, placePos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            } else {
                result = blockItem.place(blockPlaceContext);
            }

            BlockEntity blockEntity = level.getBlockEntity(placePos);
            if (blockEntity != null) {
                CompoundTag nbt = blockEntityData.copyTag();
                HolderLookup.Provider registries = level.registryAccess();
                blockEntity.loadWithComponents(nbt, registries);
                blockEntity.setChanged();
                level.sendBlockUpdated(placePos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }

            // Clear the module
            stack.remove(DataComponents.CONTAINER);

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
                    if (storedStack.get(DataComponents.BLOCK_ENTITY_DATA) != null) {
                        tooltipComponents.add(Component.literal("Contains Block Data").withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Handle the case where slot 0 doesn't exist
            }
        }
        tooltipComponents.add(Component.literal("[Empty]"));
    }
}