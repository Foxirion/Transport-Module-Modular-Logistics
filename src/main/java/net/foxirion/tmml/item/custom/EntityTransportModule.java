package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class EntityTransportModule extends Item {
    public static final Set<EntityType<?>> BANNED_ENTITY_TYPES = Set.of(
            EntityType.ENDER_DRAGON,
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

        if (!isShiftKeyDown && storedEntity != null) {
            return handleEntityPlace(level, context, itemStack, storedEntity);
        }

        return InteractionResult.PASS;
    }

    public boolean canStoreEntity(LivingEntity entity) {
        return !BANNED_ENTITY_TYPES.contains(entity.getType())
                && !entity.isRemoved()
                && entity.isAlive()
                && !(entity instanceof Player);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand usedHand) {
        if (!canStoreEntity(target)) {
            return InteractionResult.FAIL;
        }

        CompoundTag entityTag = new CompoundTag();
        target.save(entityTag);

        stack.set(TMMLDataComponents.ENTITY_CONTENT.get(), entityTag);

        target.remove(Entity.RemovalReason.DISCARDED);

        player.displayClientMessage(Component.literal("Stored " + target.getName().getString()), true);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, CompoundTag storedEntityTag) {
        try {
            String storedEntityTypeKey = storedEntityTag.getString("id");
            EntityType<?> expectedType = EntityType.byString(storedEntityTypeKey).orElse(null);

            if (expectedType == null) {
                context.getPlayer().displayClientMessage(Component.literal("Cannot recreate entity"), true);
                return InteractionResult.FAIL;
            }

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