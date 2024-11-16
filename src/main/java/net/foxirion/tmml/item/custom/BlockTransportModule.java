package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.util.TMMLTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class BlockTransportModule extends Item {
    public BlockTransportModule(Settings settings) {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = context.getStack();

        if (player == null) return ActionResult.FAIL;

        ContainerComponent itemContents = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (player.isSneaking()) {
            // If module is not empty, prevent picking up another block
            if (itemContents != ContainerComponent.DEFAULT) {
                if (!world.isClient) {
                    player.sendMessage(Text.literal("Module must be empty before storing another block"), true);
                }
                return ActionResult.FAIL;
            }
            return handleBlockStore(world, pos, stack, player);
        }
        return handleBlockPlace(world, pos, stack, player, context.getSide());
    }

    public ActionResult handleBlockStore(World world, BlockPos pos, ItemStack transportModule, PlayerEntity player) {
        BlockState blockState = world.getBlockState(pos);

        // Send a message to the player if they try to pick up an unpickable block
        if (blockState.isAir() || !world.canPlayerModifyAt(player, pos) || blockState.isIn(TMMLTags.BlockTags.BLOCK_TRANSPORT_UNPICKABLE)) {
            if (player != null && !world.isClient) {
                player.sendMessage(Text.literal("This block cannot be picked up."), true);
            }
            return ActionResult.FAIL;
        }

        // Create ItemStack from the block
        ItemStack blockStack = new ItemStack(blockState.getBlock().asItem());

        // Handle BlockEntity (NBT) data
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            HolderLookup.Provider registries = world.registryAccess();
            CompoundTag nbt = blockEntity.saveWithFullMetadata(registries);
            CustomData blockEntityData = CustomData.of(nbt);
            // Store the BlockEntity data in the ItemStack
            blockStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, blockEntityData);
        }

        // Store the block in the module
        List<ItemStack> itemList = List.(1, ItemStack.EMPTY);
        itemList.set(0, blockStack);
        transportModule.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(itemList));

        // Remove the block from the world and prevent items from dropping if container
        if (blockEntity instanceof Container container) {
            container.clearContent();
        }
        world.removeBlock(pos, false);

        return ActionResult.success(world.isClient());
    }

    public ActionResult handleBlockPlace(World world, BlockPos pos, ItemStack stack, PlayerEntity player, Direction clickedFace) {
        BlockPos placePos = pos.relative(clickedFace);

        if (!world.mayInteract(player, placePos)) {
            return ActionResult.PASS;
        }

        // Get the stored block
        ContainerComponent itemContents = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (itemContents == ContainerComponent.DEFAULT) {
            return ActionResult.PASS;
        }

        try {
            ItemStack storedBlock = getStackInSlot(0);
            if (storedBlock.isEmpty() || !(storedBlock.getItem() instanceof BlockItem blockItem)) {
                return ActionResult.PASS;
            }

            // Get the NBT data before placing
            CustomData blockEntityData = storedBlock.get(DataComponentTypes.BLOCK_ENTITY_DATA);
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
            ActionResult result = ActionResult.SUCCESS;
            if (world.getBlockState(placePos).isReplaceable()) {
                BlockState blockState = blockItem.getBlock().getPlacementState(blockPlaceContext);
                world.removeBlock(placePos, false);
                world.block(placePos, blockState, 3);
                BlockSoundGroup soundtype = blockState.getSoundGroup();
                world.playSound(null, placePos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            } else {
                result = blockItem.place(blockPlaceContext);
            }

            BlockEntity blockEntity = world.getBlockEntity(placePos);
            if (blockEntity != null) {
                CompoundTag nbt = blockEntityData.copyTag();
                HolderLookup.Provider registries = world.registryAccess();
                blockEntity.loadWithComponents(nbt, registries);
                blockEntity.setChanged();
                world.sendBlockUpdated(placePos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }

            // Clear the module
            stack.remove(DataComponentTypes.CONTAINER);

            return result;
        } catch (UnsupportedOperationException e) {
            return ActionResult.PASS;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        ContainerComponent itemContents = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (itemContents != ContainerComponent.DEFAULT) {
            try {
                ItemStack storedStack = itemContents.getStack(0);
                if (!storedStack.isEmpty()) {
                    tooltip.add(storedStack.getName());
                    if (storedStack.get(DataComponentTypes.BLOCK_ENTITY_DATA) != null) {
                        tooltip.add(Text.translatable("Contains Block Data").formatted(Formatting.GRAY));
                    }
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Handle the case where slot 0 doesn't exist
            }
        }
        tooltip.add(Text.translatable("[Empty]"));
    }
}