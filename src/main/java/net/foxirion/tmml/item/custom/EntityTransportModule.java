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
import net.minecraft.world.phys.Vec3;

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player == null || level.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag storedEntity = getStoredEntity(itemStack);

        if (player.isShiftKeyDown() && storedEntity == null) {
            // Attempt to pick up an entity
            InteractionResult result = handleEntityPickup(level, player, itemStack);
            return new InteractionResultHolder<>(result, itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player == null || level.isClientSide) return InteractionResult.PASS;

        CompoundTag storedEntity = getStoredEntity(itemStack);

        if (!player.isShiftKeyDown() && storedEntity != null) {
            // Attempt to place the stored entity
            return handleEntityPlace(level, context, itemStack, storedEntity);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleEntityPickup(Level level, Player player, ItemStack itemStack) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(lookVec.scale(5)); // Ray trace up to 5 blocks
        AABB box = new AABB(start, end).inflate(1);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, this::canStoreEntity);

        if (!entities.isEmpty()) {
            LivingEntity target = entities.get(0);
            return handleEntityStore(level, itemStack, target, player);
        }

        player.displayClientMessage(Component.literal("No valid entity found"), true);
        return InteractionResult.FAIL;
    }

    private boolean canStoreEntity(LivingEntity entity) {
        return !BANNED_ENTITY_TYPES.contains(entity.getType())
                && !entity.isRemoved()
                && entity.isAlive()
                && !(entity instanceof Player);
    }

    private InteractionResult handleEntityStore(Level level, ItemStack itemStack, LivingEntity target, Player player) {
        CompoundTag entityTag = new CompoundTag();
        target.save(entityTag);

        itemStack.set(TMMLDataComponents.ENTITY_CONTENT.get(), entityTag);
        target.remove(Entity.RemovalReason.DISCARDED);

        player.displayClientMessage(Component.literal("Stored " + target.getType().getDescriptionId()), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, CompoundTag storedEntityTag) {
        try {
            String storedEntityTypeKey = storedEntityTag.getString("id");
            EntityType<?> expectedType = EntityType.byString(storedEntityTypeKey).orElse(null);

            if (expectedType == null) {
                context.getPlayer().displayClientMessage(Component.literal("Cannot recreate entity"), true);
                return InteractionResult.FAIL;
            }

            BlockPos targetPos = context.getClickedPos().above();
            storedEntityTag.putDouble("Pos.0", targetPos.getX() + 0.5);
            storedEntityTag.putDouble("Pos.1", targetPos.getY());
            storedEntityTag.putDouble("Pos.2", targetPos.getZ() + 0.5);

            Entity recreatedEntity = EntityType.loadEntityRecursive(storedEntityTag, level, (entity) -> entity);

            if (recreatedEntity != null) {
                level.addFreshEntity(recreatedEntity);
                itemStack.remove(TMMLDataComponents.ENTITY_CONTENT.get());
                context.getPlayer().displayClientMessage(Component.literal("Placed " + recreatedEntity.getType().getDescriptionId()), true);
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