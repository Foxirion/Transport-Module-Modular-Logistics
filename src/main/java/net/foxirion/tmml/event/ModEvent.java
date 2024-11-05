package net.foxirion.tmml.event;

import net.foxirion.tmml.item.TMMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.foxirion.tmml.init.TMML.TMMLID;

@Mod.EventBusSubscriber(modid = TMMLID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvent {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        Block block = level.getBlockState(pos).getBlock();
        ItemStack heldItem = event.getItemStack();
        InteractionHand hand = event.getHand();

        // Check if player is holding a glass bottle and clicking on void air
        if (heldItem.getItem() == Items.GLASS_BOTTLE && (block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY)) {

            // Create new void bottle item stack
            ItemStack voidBottle = new ItemStack(TMMLItems.VOID_BOTTLE.get());

            // Shrink the glass bottle stack by 1
            heldItem.shrink(1);

            // Give the player the void bottle
            if (heldItem.isEmpty()) {
                player.setItemInHand(event.getHand(), voidBottle);
            } else if (!player.getInventory().add(voidBottle)) {
                player.drop(voidBottle, false);
            }

            // Play bottle fill sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);

            // Trigger the animation and events
            player.startUsingItem(hand);
            player.swing(hand);
            player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

            // Add some particles for effect
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, // particle count
                        0.5, 0.5, 0.5, // spread
                        0.1 // speed
                );
            }
        }
    }
}