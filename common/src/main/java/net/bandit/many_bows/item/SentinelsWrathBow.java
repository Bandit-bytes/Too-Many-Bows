package net.bandit.many_bows.item;


import net.bandit.many_bows.entity.SentinelArrow;
import net.bandit.many_bows.entity.SentinelArrow;
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

public class SentinelsWrathBow extends BowItem {

    public SentinelsWrathBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

            if (power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                SentinelArrow arrow = new SentinelArrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                arrow.setBaseDamage(8.0);
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 0.5);
                }
                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    arrow.setKnockback(punchLevel);
                }
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    arrow.setSecondsOnFire(100);
                }
                arrow.pickup = hasInfinity ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;
                level.addFreshEntity(arrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (!hasInfinity && !arrowStack.isEmpty()) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }

                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.tooltip").withStyle(ChatFormatting.GOLD));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.shift_title").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob1").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob2").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob3").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob4").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob5").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob6").withStyle(ChatFormatting.GREEN));
        } else {
            // Message prompting the player to hold Shift
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.hold_shift").withStyle(ChatFormatting.GRAY));
        }
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
   
}
