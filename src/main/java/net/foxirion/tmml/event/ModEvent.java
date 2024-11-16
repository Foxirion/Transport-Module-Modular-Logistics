package net.foxirion.tmml.event;

import net.foxirion.tmml.item.TMMLItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class ModEvent {
    public static void registerEvents() {
        UseBlockCallback.EVENT.register(ModEvent::onRightClickBlock);
    }

    private static ActionResult onRightClickBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        ItemStack heldItem = player.getStackInHand(hand);

        // Check if player is holding a glass bottle and clicking on void air
        if (heldItem.getItem() == Items.GLASS_BOTTLE && (block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY)) {
            // Create new void bottle item stack
            ItemStack voidBottle = new ItemStack(TMMLItems.VOID_BOTTLE);

            // Shrink the glass bottle stack by 1
            heldItem.decrement(1);

            // Give the player the void bottle
            if (heldItem.isEmpty()) {
                player.setStackInHand(hand, voidBottle);
            } else if (!player.getInventory().insertStack(voidBottle)) {
                player.dropItem(voidBottle, false);
            }

            // Play bottle fill sound
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);

            // Trigger animation, stats, and events
            player.swingHand(hand);
            player.incrementStat(Stats.USED.getOrCreateStat(Items.GLASS_BOTTLE));
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);

            // Add particles
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.5, 0.5, 0.5, 0.1);
            }

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
