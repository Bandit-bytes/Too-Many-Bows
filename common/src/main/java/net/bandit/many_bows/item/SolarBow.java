package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
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

public class SolarBow extends BowItem {

    public SolarBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4F, 0.3F);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            // Fire arrow if charge is adequate and arrows are consumed or Infinity is enabled
            if (charge >= 20 && (hasInfinity || consumeArrow(player))) {
                fireFlamingArrow(level, player, hasInfinity);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private void fireFlamingArrow(Level level, Player player, boolean hasInfinity) {
        if (!level.isClientSide()) {
            Arrow arrow = new Arrow(level, player);

            // Set pickup status to DISALLOWED if Infinity is enabled
            if (hasInfinity) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }

            arrow.setSecondsOnFire(100);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            arrow.setBaseDamage(arrow.getBaseDamage() + 4.0);
            level.addFreshEntity(arrow);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    private boolean consumeArrow(Player player) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode doesn't consume arrows
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                stack.shrink(1); // Reduce arrow count by 1
                return true;
            }
        }
        return false; // No arrows found
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        super.onUseTick(level, entity, stack, timeLeft);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge == 10) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.0F);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.solar_bow.tooltip").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.many_bows.solar_bow.tooltip.ability")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF4500))));
    }
}
