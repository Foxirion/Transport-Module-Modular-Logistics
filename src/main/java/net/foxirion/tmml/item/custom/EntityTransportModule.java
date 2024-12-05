package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

public class EntityTransportModule extends Item {
    // Banned entity types (configurable)
    private static final Set<EntityType<?>> BANNED_ENTITY_TYPES = Set.of(
            EntityType.PLAYER,  // Prevent storing players
            EntityType.ENDER_DRAGON,  // Prevent storing dragons
            EntityType.ENDER_PEARL,  // Prevent storing pearls
            EntityType.WITHER   // Prevent storing withers
    );

    public EntityTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        // Check for stored entity
        CompoundTag storedEntity = getStoredEntity(itemStack);
        boolean isShiftKeyDown = player.isShiftKeyDown();

        // Storing entity
        if (isShiftKeyDown && storedEntity == null) {
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(clickedPos).inflate(1.5),
                    entity -> canStoreEntity(entity)
            );

            if (!nearbyEntities.isEmpty()) {
                return handleEntityStore(level, itemStack, nearbyEntities.get(0), player);
            }
        }

        // Placing entity
        if (!isShiftKeyDown && storedEntity != null) {
            return handleEntityPlace(level, context, itemStack, storedEntity);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        // Check if shift key is down for storing entity
        if (player.isShiftKeyDown()) {
            // Prevent storing if module already contains an entity
            CompoundTag storedEntity = getStoredEntity(itemStack);
            if (storedEntity != null) {
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

    // Validate if an entity can be stored
    private boolean canStoreEntity(LivingEntity entity) {
        return !BANNED_ENTITY_TYPES.contains(entity.getType())
                && !entity.isRemoved()
                && entity.isAlive()
                && !(entity instanceof Player);
    }

    // Handle storing an entity in the transport module
    private InteractionResult handleEntityStore(Level level, ItemStack itemStack, LivingEntity target, Player player) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        if (!canStoreEntity(target)) {
            player.displayClientMessage(Component.literal("Cannot store this entity"), true);
            return InteractionResult.FAIL;
        }

        // More precise NBT saving
        CompoundTag entityTag = new CompoundTag();
        target.saveWithoutId(entityTag);

        // Preserve additional context
        entityTag.putString("StoredEntityType", EntityType.getKey(target.getType()).toString());
        entityTag.putUUID("OriginalUUID", target.getUUID());

        // Set entity content in the item
        itemStack.set(TMMLDataComponents.ENTITY_CONTENT, entityTag);

        // Remove original entity
        target.remove(Entity.RemovalReason.DISCARDED);

        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("Stored " + target.getType().getDescriptionId()), true);
        }

        return InteractionResult.SUCCESS;
    }

    // Handle placing a stored entity in the world
    private InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, CompoundTag entityTag) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        try {
            // Validate stored entity type before recreation
            String storedEntityTypeKey = entityTag.getString("StoredEntityType");
            EntityType<?> expectedType = EntityType.byString(storedEntityTypeKey)
                    .orElse(null);

            if (expectedType == null) {
                return InteractionResult.PASS;
            }

            Entity recreatedEntity = EntityType.loadEntityRecursive(entityTag, level, (loadedEntity) -> {
                // Reposition the entity
                loadedEntity.setPos(
                        context.getClickedPos().getX() + 0.5,
                        context.getClickedPos().getY() + 1,
                        context.getClickedPos().getZ() + 0.5
                );
                return loadedEntity;
            });

            if (recreatedEntity != null) {
                level.addFreshEntity(recreatedEntity);
                // Remove entity content from the item
                itemStack.remove(TMMLDataComponents.ENTITY_CONTENT);
                return InteractionResult.SUCCESS;
            }
        } catch (Exception e) {
            // Log or handle specific exceptions
            e.printStackTrace();
        }

        return InteractionResult.PASS;
    }

    // Retrieve stored entity from the item
    private CompoundTag getStoredEntity(ItemStack stack) {
        return stack.get(TMMLDataComponents.ENTITY_CONTENT);
    }

    // Add hover text to show stored entity information
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag storedEntity = getStoredEntity(stack);

        if (storedEntity != null) {
            String storedEntityTypeKey = storedEntity.getString("StoredEntityType");
            tooltipComponents.add(Component.literal("Stored Entity: " + storedEntityTypeKey)
                    .withStyle(ChatFormatting.BLUE));
            tooltipComponents.add(Component.literal("Contains Entity Data")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.literal("[Empty]"));
        }
    }
}