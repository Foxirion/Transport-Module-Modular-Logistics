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
import net.minecraft.world.InteractionResultHolder;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class EntityTransportModule extends Item {
    private static final Set<EntityType<?>> BANNED_ENTITY_TYPES = Set.of(
            EntityType.PLAYER,
            EntityType.ENDER_DRAGON,
            EntityType.ENDER_PEARL,
            EntityType.WITHER
    );

    public EntityTransportModule(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        CompoundTag storedEntity = getStoredEntity(itemStack);
        boolean isShiftKeyDown = player.isShiftKeyDown();

        if (isShiftKeyDown && storedEntity == null) {
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(context.getClickedPos()).inflate(1.5),
                    this::canStoreEntity
            );

            if (!nearbyEntities.isEmpty()) {
                return handleEntityStore(level, itemStack, nearbyEntities.get(0), player);
            }
        }

        if (!isShiftKeyDown && storedEntity != null) {
            return handleEntityPlace(level, context, itemStack, storedEntity);
        }

        return InteractionResult.PASS;
    }

    private boolean canStoreEntity(LivingEntity entity) {
        return !BANNED_ENTITY_TYPES.contains(entity.getType())
                && !entity.isRemoved()
                && entity.isAlive()
                && !(entity instanceof Player);
    }

    private InteractionResult handleEntityStore(Level level, ItemStack itemStack, LivingEntity target, Player player) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        if (!canStoreEntity(target)) {
            player.displayClientMessage(Component.literal("Cannot store this entity"), true);
            return InteractionResult.FAIL;
        }

        CompoundTag entityTag = new CompoundTag();
        target.save(entityTag);

        itemStack.set(TMMLDataComponents.ENTITY_CONTENT.get(), entityTag);
        target.remove(Entity.RemovalReason.DISCARDED);

        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("Stored " + target.getType().getDescriptionId()), true);
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, CompoundTag storedEntityTag) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        try {
            String storedEntityTypeKey = storedEntityTag.getString("id");
            EntityType<?> expectedType = EntityType.byString(storedEntityTypeKey).orElse(null);

            if (expectedType == null) {
                context.getPlayer().displayClientMessage(Component.literal("Cannot recreate entity"), true);
                return InteractionResult.FAIL;
            }

            storedEntityTag.putDouble("Pos.0", context.getClickedPos().getX() + 0.5);
            storedEntityTag.putDouble("Pos.1", context.getClickedPos().getY() + 1);
            storedEntityTag.putDouble("Pos.2", context.getClickedPos().getZ() + 0.5);

            Entity recreatedEntity = EntityType.loadEntityRecursive(storedEntityTag, level, (loadedEntity) -> loadedEntity);

            if (recreatedEntity != null) {
                level.addFreshEntity(recreatedEntity);
                itemStack.remove(TMMLDataComponents.ENTITY_CONTENT.get());
                return InteractionResult.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.getPlayer().displayClientMessage(Component.literal("Failed to place entity"), true);
        }

        return InteractionResult.PASS;
    }

    private CompoundTag getStoredEntity(ItemStack stack) {
        return stack.get(TMMLDataComponents.ENTITY_CONTENT.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag storedEntity = getStoredEntity(stack);
        if (storedEntity != null) {
            String entityTypeName = storedEntity.getString("id");
            tooltipComponents.add(Component.literal("Stored Entity: " + entityTypeName).withStyle(ChatFormatting.WHITE));
            return;
        }
        tooltipComponents.add(Component.literal("[Empty]").withStyle(ChatFormatting.WHITE));
    }
}

