package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class ScatterBow extends BowItem {

    private static final int MAX_ARROWS = 8; // Max arrows fired at once

    public ScatterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            List<ItemStack> projectiles = draw(bowStack, player.getProjectile(bowStack), player);

            if (!projectiles.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                // Count available arrows (limit to max 8)
                int arrowsAvailable = player.getAbilities().instabuild ? MAX_ARROWS : Math.min(MAX_ARROWS, getArrowCount(player));

                if (power >= 0.1F && arrowsAvailable > 0) {
                    for (int i = 0; i < arrowsAvailable; i++) {
                        Arrow arrow = EntityType.ARROW.create(level);
                        if (arrow != null) {
                            arrow.setOwner(player);
                            arrow.setPos(player.getX(), player.getEyeY(), player.getZ());
                            arrow.setBaseDamage(1.0);
                            arrow.pickup = AbstractArrow.Pickup.ALLOWED;

                            // Randomized spread for scatter effect
                            float yawOffset = (level.getRandom().nextFloat() - 0.5F) * 20F;
                            float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * 10F;
                            arrow.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset, 0.0F, power * 3.0F, 1.0F);

                            level.addFreshEntity(arrow);
                        }
                    }

                    // Play shooting sound & damage the bow
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                        removeArrowsFromInventory(player, arrowsAvailable);
                    }
                }
            }
        }
    }

    /**
     * Get the number of arrows in the player's inventory.
     */
    private int getArrowCount(Player player) {
        int arrowCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            ItemStack projectile = player.getProjectile(stack);
            if (!projectile.isEmpty()) {
                arrowCount += projectile.getCount();
            }
        }
        return arrowCount;
    }

    /**
     * Remove a specific number of arrows from the inventory.
     */
    private void removeArrowsFromInventory(Player player, int count) {
        int removed = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                int toRemove = Math.min(stack.getCount(), count - removed);
                stack.shrink(toRemove);
                removed += toRemove;
                if (removed >= count) {
                    break;
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.scatter_bow.tooltip").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.scatter_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}