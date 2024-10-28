package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.TidalArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TidalBow extends BowItem {

    public TidalBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            // Fire the Tidal Arrow if power is adequate and arrows are consumed or Infinity is enabled
            if (power >= 0.1F && (hasInfinity || consumeArrow(player))) {
                TidalArrow tidalArrow = new TidalArrow(level, player);
                tidalArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                tidalArrow.setBaseDamage(tidalArrow.getBaseDamage() * 1.5);

                // Prevent pickup if Infinity is enabled
                if (hasInfinity) {
                    tidalArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                level.addFreshEntity(tidalArrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.3F, 1.0F);

                // Damage the bow if Infinity isn't enabled
                if (!hasInfinity) {
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            } else if (power >= 0.1F) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private boolean consumeArrow(Player player) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode doesn't consume arrows
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.tidal_bow.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.too_many_bows.tidal_bow.tooltip.ability").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.too_many_bows.tidal_bow.tooltip.legend").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
    }
}
