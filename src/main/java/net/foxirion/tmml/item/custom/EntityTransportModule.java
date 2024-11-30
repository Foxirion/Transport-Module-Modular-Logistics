package net.foxirion.tmml.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityTransportModule extends Item {
    public EntityTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        // Get the stored entities
        ItemContainerContents itemContents = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        // Check if shift key is down for storing entity
        if (player.isShiftKeyDown()) {
            // Prevent storing if module is not empty
            if (itemContents != ItemContainerContents.EMPTY) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Module must be empty before storing another entity"), true);
                }
                return InteractionResult.FAIL;
            }
        }

        // Handle entity placement
        if (!player.isShiftKeyDown() && itemContents != ItemContainerContents.EMPTY) {
            return handleEntityPlace(level, context, itemStack, itemContents);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        // Check if shift key is down for storing entity
        if (player.isShiftKeyDown()) {
            // Get current contents
            ItemContainerContents itemContents = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

            // Prevent storing if module is not empty
            if (itemContents != ItemContainerContents.EMPTY) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Module must be empty before storing another entity"), true);
                }
                return InteractionResult.FAIL;
            }

            // Store the entity in the module
            return handleEntityStore(level, itemStack, target, player);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleEntityStore(Level level, ItemStack itemStack, LivingEntity target, Player player) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        // Prepare entity data
        CompoundTag entityTag = new CompoundTag();
        target.save(entityTag);

        // Create ItemStack to store in container
        ItemStack entityStack = ItemStack.EMPTY.copy();
        entityStack.set(DataComponents.CUSTOM_DATA, CustomData.of(entityTag));

        // Store in container
        NonNullList<ItemStack> itemList = NonNullList.withSize(1, ItemStack.EMPTY);
        itemList.set(0, entityStack);
        itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(itemList));

        // Remove original entity
        target.remove(Entity.RemovalReason.DISCARDED);

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, ItemContainerContents itemContents) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        try {
            ItemStack storedEntityStack = itemContents.getStackInSlot(0);
            if (storedEntityStack.isEmpty()) return InteractionResult.PASS;

            // Get entity data
            CustomData entityData = storedEntityStack.get(DataComponents.CUSTOM_DATA);
            if (entityData == null) return InteractionResult.PASS;

            CompoundTag nbt = entityData.copyTag();

            // Recreate entity
            Entity recreatedEntity = EntityType.loadEntityRecursive(nbt, level, (loadedEntity) -> loadedEntity);

            if (recreatedEntity != null) {
                // Position the entity
                recreatedEntity.setPos(
                        context.getClickedPos().getX() + 0.5,
                        context.getClickedPos().getY() + 1,
                        context.getClickedPos().getZ() + 0.5
                );

                // Add entity to world
                level.addFreshEntity(recreatedEntity);

                // Clear the module
                itemStack.remove(DataComponents.CONTAINER);

                return InteractionResult.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemContainerContents itemContents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        if (itemContents != ItemContainerContents.EMPTY) {
            try {
                ItemStack storedStack = itemContents.getStackInSlot(0);
                if (!storedStack.isEmpty()) {
                    tooltipComponents.add(storedStack.getDisplayName());
                    if (storedStack.get(DataComponents.CUSTOM_DATA) != null) {
                        tooltipComponents.add(Component.literal("Contains Entity Data").withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
            } catch (UnsupportedOperationException e) {
                // Handle potential exceptions
            }
        }

        tooltipComponents.add(Component.literal("[Empty]"));
    }
}
