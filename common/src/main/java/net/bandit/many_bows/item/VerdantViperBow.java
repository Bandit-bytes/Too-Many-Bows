package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.VenomArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VerdantViperBow extends BowItem {

    public VerdantViperBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

            // Fire the Venom Arrow if power is adequate and arrows are consumed or Infinity is enabled
            if (power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                VenomArrow venomArrow = new VenomArrow(level, player);

                // Apply enchantment
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    venomArrow.setBaseDamage(venomArrow.getBaseDamage() + (double) powerLevel * 0.5 + 0.5);
                }

                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    venomArrow.setKnockback(punchLevel);
                }

                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    venomArrow.setSecondsOnFire(100);
                }

                venomArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                // Prevent pickup if Infinity is enabled
                if (hasInfinity) {
                    venomArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                level.addFreshEntity(venomArrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Consume arrow if not in Creative mode or with Infinity enchantment
                if (!hasInfinity && !arrowStack.isEmpty()) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }

                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else if (power >= 0.1F) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip.ability").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
