package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.CursedFlameArrow;
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

public class CursedFlameBow extends BowItem {

    public CursedFlameBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                // Check for Creative mode or Infinity enchantment
                boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : findArrowInInventory(player);

                // Proceed if arrow is available or Infinity is enabled
                if (hasInfinity || !arrowStack.isEmpty()) {
                    CursedFlameArrow cursedFlameArrow = new CursedFlameArrow(level, player);
                    cursedFlameArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                    cursedFlameArrow.setBaseDamage(cursedFlameArrow.getBaseDamage() * 2.25);

                    // Prevent pickup if Infinity is present
                    if (hasInfinity) {
                        cursedFlameArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    level.addFreshEntity(cursedFlameArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.2F);

                    // Consume arrow if not in Creative mode or with Infinity enchantment
                    if (!hasInfinity) {
                        arrowStack.shrink(1);
                    }
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.ability")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.legend")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }
}
