package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class BlockTransportModule extends Item {
    // NBT keys
    private static final String NBT_STORED_ITEM = "StoredItem";
    private static final String NBT_BLOCK_ENTITY = "BlockEntityData";

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
            // Check if module already contains a block
            if (hasStoredBlock(stack)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Module must be empty before storing another block"), true);
                }
                return InteractionResult.FAIL;
            }
            return handleBlockStore(level, pos, stack, player);
        }
        return handleBlockPlace(level, pos, clickedFace, stack, player, context);
    }

    private boolean hasStoredBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_STORED_ITEM);
    }

    public InteractionResult handleBlockStore(Level level, BlockPos pos, ItemStack transportModule, Player player) {
        BlockState blockState = level.getBlockState(pos);

        // Check if block can be picked up
        if (blockState.isAir() || !level.mayInteract(player, pos) || blockState.is(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)) {
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.literal("This block cannot be picked up."), true);
            }
            return InteractionResult.FAIL;
        }

        // Create ItemStack from the block
        ItemStack blockStack = new ItemStack(blockState.getBlock().asItem());

        // Handle BlockEntity data
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            CompoundTag blockEntityTag = blockEntity.saveWithoutMetadata();
            CompoundTag moduleTag = transportModule.getOrCreateTag();
            moduleTag.put(NBT_BLOCK_ENTITY, blockEntityTag);
        }

        // Store the block information
        CompoundTag moduleTag = transportModule.getOrCreateTag();
        CompoundTag storedItemTag = new CompoundTag();
        blockStack.save(storedItemTag);
        moduleTag.put(NBT_STORED_ITEM, storedItemTag);

        // Clear container contents and remove block
        if (blockEntity instanceof Container container) {
            container.clearContent();
        }
        level.removeBlock(pos, false);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean isUnpickable(BlockState state) {
        // Add your unpickable block tags logic here
        return false; // Replace with actual implementation
    }

    public InteractionResult handleBlockPlace(Level level, BlockPos pos, Direction clickedFace, ItemStack stack, Player player, UseOnContext context) {
        BlockPos placePos = pos.relative(clickedFace);

        if (!level.mayInteract(player, placePos)) {
            return InteractionResult.PASS;
        }

        CompoundTag moduleTag = stack.getTag();
        if (moduleTag == null || !moduleTag.contains(NBT_STORED_ITEM)) {
            return InteractionResult.PASS;
        }

        try {
            ItemStack storedBlock = ItemStack.of(moduleTag.getCompound(NBT_STORED_ITEM));
            if (storedBlock.isEmpty() || !(storedBlock.getItem() instanceof BlockItem blockItem)) {
                return InteractionResult.PASS;
            }

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

            // Handle BlockEntity data if present
            if (moduleTag.contains(NBT_BLOCK_ENTITY)) {
                BlockEntity blockEntity = level.getBlockEntity(placePos);
                if (blockEntity != null) {
                    CompoundTag blockEntityTag = moduleTag.getCompound(NBT_BLOCK_ENTITY);
                    blockEntity.load(blockEntityTag);
                    blockEntity.setChanged();
                    level.sendBlockUpdated(placePos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                }
            }

            // Clear the module
            stack.setTag(null);

            return result;
        } catch (Exception e) {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        CompoundTag moduleTag = stack.getTag();
        if (moduleTag != null && moduleTag.contains(NBT_STORED_ITEM)) {
            try {
                ItemStack storedStack = ItemStack.of(moduleTag.getCompound(NBT_STORED_ITEM));
                if (!storedStack.isEmpty()) {
                    tooltipComponents.add(storedStack.getDisplayName());
                    if (moduleTag.contains(NBT_BLOCK_ENTITY)) {
                        tooltipComponents.add(Component.literal("Contains Block Data").withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
            } catch (Exception e) {
                // Handle any NBT reading errors
            }
        }
        tooltipComponents.add(Component.literal("[Empty]"));
    }
}