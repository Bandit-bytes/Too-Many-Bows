package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScatterBow extends BowItem {

    private static final int NUM_ARROWS = 8;

    public ScatterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            // Determine the number of arrows to shoot based on inventory (up to a max of 8)
            int arrowsToShoot = hasInfinity ? NUM_ARROWS : Math.min(NUM_ARROWS, countArrows(player));

            // Proceed if power is adequate and arrows are consumed or player has Infinity
            if (power >= 0.1F && (hasInfinity || consumeArrow(player, arrowsToShoot))) {
                for (int i = 0; i < arrowsToShoot; i++) {
                    Arrow arrow = new Arrow(level, player);
                    arrow.setBaseDamage(1.0); // Set each arrow’s base damage

                    // Prevent pickup if Infinity is enabled
                    if (hasInfinity) {
                        arrow.pickup = Arrow.Pickup.DISALLOWED;
                    }

                    // Slightly randomize the shooting direction
                    float yawOffset = (level.getRandom().nextFloat() - 0.5F) * 20F;
                    float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * 10F;
                    arrow.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset, 0.0F, power * 3.0F, 1.0F);

                    level.addFreshEntity(arrow);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    // Adjust consumeArrow to account for multiple arrows
    private boolean consumeArrow(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode doesn't consume arrows
        }

        int arrowsRemoved = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW && arrowsRemoved < count) {
                int removeAmount = Math.min(stack.getCount(), count - arrowsRemoved);
                stack.shrink(removeAmount);
                arrowsRemoved += removeAmount;
            }
        }
        return arrowsRemoved >= count;
    }

    // Count total arrows in the player’s inventory
    private int countArrows(Player player) {
        int arrowCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                arrowCount += stack.getCount();
            }
        }
        return arrowCount;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
    }
}
