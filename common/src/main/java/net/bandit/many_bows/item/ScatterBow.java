package net.bandit.many_bows.item;

import net.bandit.many_bows.compat.OriginsQuiverCompat;
import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ScatterBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScatterBow extends ModBowItem {

    public ScatterBow(Properties properties) {
        super(properties);
    }

    private static ScatterBowConfig config() {
        return BowJsonConfigHelper.getConfig("scatter_bow", ScatterBowConfig.class, ScatterBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge, entity);

        ScatterBowConfig config = config();
        int maxArrows = Math.max(1, config.max_arrows);
        boolean hasInfinity = canFireWithoutArrows(stack, player);
        int arrowsToShoot = hasInfinity ? maxArrows : Math.min(maxArrows, countArrows(player));

        if (power >= 0.1F && arrowsToShoot > 0 && (hasInfinity || consumeArrow(player, arrowsToShoot))) {
            double baseDamage = resolveConfiguredDamage(player, config.base_damage,
                    config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, 0.0D);

            for (int i = 0; i < arrowsToShoot; i++) {
                Arrow arrow = new Arrow(level, player);
                arrow.setBaseDamage(baseDamage);

                applyStandardEnchantments(arrow, stack);

                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

                if (hasInfinity) {
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                float yawOffset = (level.getRandom().nextFloat() - 0.5F) * config.yaw_spread_degrees;
                float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * config.pitch_spread_degrees;
                arrow.shootFromRotation(player,
                        player.getXRot() + pitchOffset,
                        player.getYRot() + yawOffset,
                        0.0F,
                        power * config.arrow_velocity,
                        config.arrow_inaccuracy);

                level.addFreshEntity(arrow);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

            damageBow(stack, player, player.getUsedItemHand());
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private boolean consumeArrow(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        if (countArrows(player) < count) {
            return false;
        }

        int remaining = count;

        for (int slot = 0;
             slot < player.getInventory().getContainerSize() && remaining > 0;
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!isSupportedArrow(stack)) {
                continue;
            }

            int amount = Math.min(stack.getCount(), remaining);
            ItemStack extracted = player.getInventory().removeItem(slot, amount);
            remaining -= extracted.getCount();
        }

        if (remaining > 0) {
            remaining -= OriginsQuiverCompat.consumeProjectiles(
                    player,
                    this.getAllSupportedProjectiles(),
                    remaining
            );
        }

        return remaining == 0;
    }

    private int countArrows(Player player) {
        int total = 0;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isSupportedArrow(stack)) {
                total += stack.getCount();
            }
        }

        return total + OriginsQuiverCompat.countProjectiles(
                player,
                this.getAllSupportedProjectiles()
        );
    }

    private boolean isSupportedArrow(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof net.minecraft.world.item.ArrowItem
                && this.getAllSupportedProjectiles().test(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
