package net.foxirion.tmml.item.custom;

import net.foxirion.tmml.init.TMMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EntityTransportModule extends Item {
    private static final Set<EntityType<?>> BANNED_ENTITY_TYPES = Set.of(

            EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.GIANT, EntityType.ELDER_GUARDIAN, EntityType.WARDEN, //vanilla bosses

            EntityType.ENDER_PEARL, EntityType.FIREBALL, EntityType.DRAGON_FIREBALL, EntityType.SMALL_FIREBALL,
            EntityType.FIREWORK_ROCKET, EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.WIND_CHARGE,
            EntityType.BREEZE_WIND_CHARGE, EntityType.EGG, EntityType.SNOWBALL, EntityType.EYE_OF_ENDER,EntityType.POTION,
            EntityType.LLAMA_SPIT, //projectiles

            EntityType.EXPERIENCE_BOTTLE, EntityType.EXPERIENCE_ORB, //xp

            EntityType.ITEM, EntityType.ITEM_DISPLAY, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, EntityType.ARMOR_STAND,
            EntityType.AREA_EFFECT_CLOUD, EntityType.BLOCK_DISPLAY,EntityType.CHEST_BOAT, EntityType.BOAT, EntityType.CHEST_MINECART,
            EntityType.MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, EntityType.SPAWNER_MINECART, EntityType.COMMAND_BLOCK_MINECART,
            EntityType.TNT_MINECART, EntityType.END_CRYSTAL, EntityType.EVOKER_FANGS, EntityType.FALLING_BLOCK,
            EntityType.FISHING_BOBBER, EntityType.INTERACTION, EntityType.LEASH_KNOT, EntityType.LIGHTNING_BOLT, EntityType.TEXT_DISPLAY,//items & blocks & effects

            EntityType.PLAYER //players


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
        CompoundTag storedEntity = itemStack.getOrDefault(TMMLDataComponents.ENTITY_CONTENT, null);

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

        CompoundTag storedEntity = itemStack.getOrDefault(TMMLDataComponents.ENTITY_CONTENT, null);

        if (!player.isShiftKeyDown() && storedEntity != null) {
            // Attempt to place the stored entity
            return handleEntityPlace(level, context, itemStack, storedEntity);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleEntityPickup(Level level, Player player, ItemStack itemStack) {
        // Create a more precise raycast
        Vec3 start = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 end = start.add(lookVec.scale(5));  // Keep the 5-block reach

        // Use EntityHitResult to get precise entity targeting
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                level,
                player,
                start,
                end,
                new AABB(start, end).inflate(0.5), // Smaller inflation for more precise selection
                entity -> entity instanceof LivingEntity && canStoreEntity((LivingEntity) entity)
        );

        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity target) {
            return handleEntityStore(level, itemStack, target, player);
        }

        player.displayClientMessage(Component.literal("No valid entity found").withStyle(ChatFormatting.RED), true);
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

        itemStack.set(TMMLDataComponents.ENTITY_CONTENT, entityTag);

        target.remove(Entity.RemovalReason.DISCARDED);

        // Get the clean entity name instead of the translation key
        String entityName = Component.translatable(target.getType().getDescriptionId()).getString();
        player.displayClientMessage(Component.literal("Picked up: " + entityName).withStyle(ChatFormatting.WHITE), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEntityPlace(Level level, UseOnContext context, ItemStack itemStack, CompoundTag storedEntityTag) {
        try {
            String storedEntityTypeKey = storedEntityTag.getString("id");
            EntityType<?> expectedType = EntityType.byString(storedEntityTypeKey).orElse(null);

            if (expectedType == null) {
                context.getPlayer().displayClientMessage(Component.literal("Cannot recreate entity").withStyle(ChatFormatting.WHITE), true);
                return InteractionResult.FAIL;
            }

            BlockPos targetPos = context.getClickedPos().above();
            double x = targetPos.getX() + 0.5;
            double y = targetPos.getY();
            double z = targetPos.getZ() + 0.5;

            storedEntityTag.put("Pos", newListTagForPosition(x, y, z));

            Entity recreatedEntity = EntityType.loadEntityRecursive(storedEntityTag, level, (entity) -> entity);

            if (recreatedEntity != null) {
                level.addFreshEntity(recreatedEntity);
                itemStack.remove(TMMLDataComponents.ENTITY_CONTENT);

                // Get the clean entity name
                String entityName = Component.translatable(recreatedEntity.getType().getDescriptionId()).getString();
                context.getPlayer().displayClientMessage(Component.literal("Placed: " + entityName).withStyle(ChatFormatting.WHITE), true);
                return InteractionResult.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.getPlayer().displayClientMessage(Component.literal("Failed to place entity").withStyle(ChatFormatting.WHITE), true);
        }

        return InteractionResult.PASS;
    }

    private static ListTag newListTagForPosition(double x, double y, double z) {
        ListTag posList = new ListTag();
        posList.add(DoubleTag.valueOf(x));
        posList.add(DoubleTag.valueOf(y));
        posList.add(DoubleTag.valueOf(z));
        return posList;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag storedEntity = stack.getOrDefault(TMMLDataComponents.ENTITY_CONTENT, null);
        if (storedEntity != null) {
            // Get the entity type and translate it to a readable name
            String entityTypeId = storedEntity.getString("id");
            Optional<EntityType<?>> entityType = EntityType.byString(entityTypeId);
            String displayName = entityType
                    .map(type -> Component.translatable(type.getDescriptionId()).getString())
                    .orElse(entityTypeId);

            tooltipComponents.add(Component.literal("Stored: " + displayName).withStyle(ChatFormatting.WHITE));
        } else {
            tooltipComponents.add(Component.literal("[Empty]").withStyle(ChatFormatting.WHITE));
        }
    }
}
