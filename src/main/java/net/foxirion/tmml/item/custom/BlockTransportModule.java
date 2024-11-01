package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.datacomponents.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTransportModule extends Item {
    public BlockTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack transportModule = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        // If not sneaking, attempt to pick up or place block
        if (!player.isShiftKeyDown()) {
            return handleBlockTransport(world, pos, player, transportModule);
        }

        return super.useOn(context);
    }

    private InteractionResult handleBlockTransport(Level world, BlockPos pos, Player player, ItemStack transportModule) {
        ItemStack currentBlock = transportModule.get(ModDataComponents.BLOCK_TRANSPORT_MODULE);

        // If not carrying a block, attempt to pick up
        if (currentBlock == null) {
            return pickUpBlock(world, pos, player, transportModule);
        }
        // If already carrying a block, attempt to place
        else {
            return placeCarriedBlock(world, pos, player, transportModule);
        }
    }

    private InteractionResult pickUpBlock(Level world, BlockPos pos, Player player, ItemStack transportModule) {
        BlockState blockState = world.getBlockState(pos);
        Block blockToTransport = blockState.getBlock();

        // Check block transportability conditions
        if (isBlockTransportable(world, pos, blockState)) {
            // Create an ItemStack representing the block
            ItemStack blockItemStack = new ItemStack(blockState.getBlock());

            // Set the block data including block entity data if applicable
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null) {
                // Use NeoForge method for saving block entity data
                CompoundTag blockEntityData = new CompoundTag();
                blockEntity.saveAdditional(blockEntityData);

                // Set the block entity data to the item stack
                blockItemStack.set(DataComponents.BLOCK_ENTITY_DATA, blockEntityData);
            }

            // Set the transported block component
            transportModule.set(ModDataComponents.BLOCK_TRANSPORT_MODULE, blockItemStack);

            // Update module description using modern text methods
            transportModule.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal("Transport Module: " + blockState.getBlock().getDescriptionId())
            );

            // Remove original block
            world.removeBlock(pos, false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private InteractionResult placeCarriedBlock(Level world, BlockPos pos, Player player, ItemStack transportModule) {
        ItemStack carriedBlockStack = transportModule.get(ModDataComponents.BLOCK_TRANSPORT_MODULE);
        if (carriedBlockStack == null) {
            return InteractionResult.FAIL;
        }

        BlockPos placePos = pos.relative(player.getDirection());
        Block blockToPlace = Block.byItem(carriedBlockStack.getItem());
        BlockState placedState = blockToPlace.defaultBlockState();

        // Try to place the block
        if (world.setBlock(placePos, placedState, 3)) {
            // Restore tile entity data if applicable
            BlockEntity placedBlockEntity = world.getBlockEntity(placePos);
            if (placedBlockEntity != null) {
                // Retrieve block entity data from the item stack using NeoForge method
                CompoundTag blockEntityData = carriedBlockStack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (blockEntityData != null) {
                    // Use the new loading method
                    placedBlockEntity.loadAdditional(blockEntityData);
                }
            }

            // Remove the transported block component
            transportModule.remove(ModDataComponents.BLOCK_TRANSPORT_MODULE);

            // Update name using modern text methods
            transportModule.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal("Transport Module: <Empty>")
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private boolean isBlockTransportable(Level world, BlockPos pos, BlockState blockState) {
        return !(
                // Check for unbreakable blocks
                blockState.is(world.registryAccess().registryOrThrow(Registries.BLOCK).getHolder(
                        ResourceLocation.parse("minecraft:bedrock")).orElseThrow()) ||
                        blockState.is(world.registryAccess().registryOrThrow(Registries.BLOCK).getHolder(
                                ResourceLocation.parse("minecraft:reinforced_deepslate")).orElseThrow()) ||

                        // Check for liquid, portal, and other non-transportable blocks
                        blockState.liquid() ||
                        blockState.is(world.registryAccess().registryOrThrow(Registries.BLOCK).get(ResourceLocation.parse("minecraft:nether_portal"))) ||
                        blockState.is(world.registryAccess().registryOrThrow(Registries.BLOCK).get(ResourceLocation.parse("minecraft:end_portal")))
        );
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack currentBlock = stack.get(ModDataComponents.BLOCK_TRANSPORT_MODULE);
        if (currentBlock != null) {
            return Component.literal("Transport Module: " + currentBlock.getItem().getDescriptionId());
        }
        return Component.literal("Transport Module: <Empty>");
    }
}