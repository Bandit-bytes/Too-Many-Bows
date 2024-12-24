package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.FlameArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.List;

public class FlameBow extends BowItem {

    public FlameBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : findArrowInInventory(player);

            if (power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                FlameArrow flameArrow = new FlameArrow(level, player);
                flameArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                flameArrow.setBaseDamage(flameArrow.getBaseDamage() * 2.0);
                flameArrow.setSecondsOnFire(100);

                // Enchantment effects
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    flameArrow.setBaseDamage(flameArrow.getBaseDamage() + (double) powerLevel * 0.5 + 0.5);
                }

                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    flameArrow.setKnockback(punchLevel);
                }

                // Set pickup behavior
                flameArrow.pickup = hasInfinity ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;

                level.addFreshEntity(flameArrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Consume arrow if Infinity is not active
                if (!hasInfinity) {
                    arrowStack.shrink(1);
                }
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
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
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.too_many_bows.flame_bow.tooltip").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.too_many_bows.flame_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.flame_bow.tooltip.legend").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
