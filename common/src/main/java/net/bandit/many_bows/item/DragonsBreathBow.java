package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.DragonsBreathArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonsBreathBow extends BowItem {

    public DragonsBreathBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.4F, 0.3F);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            // Only fire arrow if an arrow is available or Infinity is present
            if (charge >= 20 && (hasInfinity || consumeArrow(player))) {
                fireDragonArrow(level, player, hasInfinity);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }

    private boolean consumeArrow(Player player) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode doesn't consume arrows
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == net.minecraft.world.item.Items.ARROW) {
                stack.shrink(1); // Reduce arrow count by 1
                return true;
            }
        }
        return false; // Return false if no arrows were found
    }

    private void fireDragonArrow(Level level, Player player, boolean hasInfinity) {
        if (!level.isClientSide()) {
            DragonsBreathArrow arrow = new DragonsBreathArrow(level, player);

            // Prevent pickup if Infinity is present
            if (hasInfinity) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }

            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.0F, 1.0F);
            arrow.setBaseDamage(arrow.getBaseDamage() + 4.0);
            level.addFreshEntity(arrow);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.dragons_breath_bow.tooltip").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.many_bows.dragons_breath_bow.tooltip.ability")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0x8B0000))));
    }
}
