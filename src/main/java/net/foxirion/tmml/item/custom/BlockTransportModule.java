package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.util.TMMLTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockTransportModule extends Item {
    public BlockTransportModule() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
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
        return handleBlockPlace(world, pos, stack, player, context);
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
            RegistryWrapper.WrapperLookup registries = world.getRegistryManager();
            NbtCompound nbt = blockEntity.createComponentlessNbtWithIdentifyingData(registries);
            NbtComponent blockEntityData = NbtComponent.of(nbt);
            // Store the BlockEntity data in the ItemStack
            blockStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, blockEntityData);
        }

        // Store the block in the module
        List<ItemStack> itemList = new ArrayList<>();
        itemList.add(blockStack);
        transportModule.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(itemList));

        // Remove the block from the world and prevent items from dropping if container
        if (blockEntity instanceof Inventory container) {
            container.clear();
        }
        world.removeBlock(pos, false);

        return ActionResult.success(world.isClient());
    }

    public ActionResult handleBlockPlace(World world, BlockPos pos, ItemStack stack, PlayerEntity player, ItemUsageContext context) {
        Direction clickedFace = context.getSide();
        BlockPos placePos = pos.offset(clickedFace);

        if (!world.canPlayerModifyAt(player, placePos)) {
            return ActionResult.PASS;
        }

        // Get the stored block
        ContainerComponent itemContents = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (itemContents == ContainerComponent.DEFAULT) {
            return ActionResult.PASS;
        }

        try {
            ItemStack storedBlock = itemContents.copyFirstStack();
            if (storedBlock.isEmpty() || !(storedBlock.getItem() instanceof BlockItem blockItem)) {
                return ActionResult.PASS;
            }

            // Get the NBT data before placing
            NbtComponent blockEntityData = storedBlock.get(DataComponentTypes.BLOCK_ENTITY_DATA);
            ItemPlacementContext blockPlaceContext = new ItemPlacementContext(context);
            ActionResult result = ActionResult.SUCCESS;
            if (world.getBlockState(placePos).isReplaceable()) {
                BlockState blockState = blockItem.getBlock().getPlacementState(blockPlaceContext);
                world.removeBlock(placePos, false);
                world.setBlockState(placePos, blockState, 3);
                BlockSoundGroup soundtype = blockState.getSoundGroup();
                world.playSound(null, placePos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            } else {
                result = blockItem.place(blockPlaceContext);
            }

            BlockEntity blockEntity = world.getBlockEntity(placePos);
            if (blockEntity != null) {
                NbtCompound nbt = blockEntityData.copyNbt();
                RegistryWrapper.WrapperLookup registries = world.getRegistryManager();
                blockEntity.readComponentlessNbt(nbt, registries);
                blockEntity.toUpdatePacket();
                world.addBlockEntity(blockEntity);
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
                ItemStack storedStack = itemContents.copyFirstStack();
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