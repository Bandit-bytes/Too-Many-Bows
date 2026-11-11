package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ArcaneBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.util.AttributeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneBow extends ModBowItem {


    private static final String CONFIG_NAME = "arcane_bow";

    public ArcaneBow(Properties properties) {
        super(properties);
    }

    private static ArcaneBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, ArcaneBowConfig.class, ArcaneBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge, entity);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHROOMLIGHT_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level.isClientSide) return;

        ArcaneBowConfig config = config();
        boolean hasInfinity = canFireWithoutArrows(stack, player);
        ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

        if (power >= config.minimum_pull_power && (hasInfinity || !arrowStack.isEmpty())) {
            fireConfiguredArrows(level, player, hasInfinity, arrowStack, stack, power, config);

            if (config.damage_bow_on_release) {
                damageBow(stack, player, player.getUsedItemHand());
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void fireConfiguredArrows(Level level, Player player, boolean hasInfinity, ItemStack arrowStack,
                                      ItemStack bowStack, float power, ArcaneBowConfig config) {
        ItemStack projectileStack = arrowStack.isEmpty() ? new ItemStack(Items.ARROW) : arrowStack;
        ArrowItem arrowItem = projectileStack.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;

        int arrowCount = Math.max(1, config.arrow_count);
        int centerIndex = arrowCount / 2;
        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();

        for (int i = 0; i < arrowCount; i++) {
            AbstractArrow arrow = arrowItem.createArrow(level, projectileStack, player);

            double damage = arrow.getBaseDamage();

            if (config.direct_hit_damage_override >= 0.0D) {
                damage = config.direct_hit_damage_override;
            } else if (config.use_ranged_damage_attribute_for_damage && config.ranged_damage_attribute_divisor != 0.0D) {
                Double value = AttributeHelper.getValue(player,
                        config.ranged_damage_attribute_namespace, config.ranged_damage_attribute_path);
                if (value != null) {
                    damage = value / config.ranged_damage_attribute_divisor;
                }
            }

            damage = (damage * config.direct_hit_damage_multiplier) + config.direct_hit_damage_bonus;
            arrow.setBaseDamage(damage);

            applyStandardEnchantments(arrow, bowStack, 0.0D);

            if (config.apply_bow_damage_attribute) {
                applyBowDamageAttribute(arrow, player);
            }

            if (config.apply_bow_crit) {
                tryApplyBowCrit(arrow, player, config.bow_crit_multiplier);
            }

            float spreadOffset = (i - centerIndex) * config.spread_angle_degrees;
            arrow.shootFromRotation(player, basePitch, baseYaw + spreadOffset, 0.0F,
                    power * config.velocity_multiplier, config.inaccuracy);

            boolean isCenterArrow = i == centerIndex;
            arrow.pickup = isCenterArrow && !hasInfinity
                    ? parsePickupMode(config.center_arrow_pickup)
                    : parsePickupMode(config.side_arrow_pickup);

            level.addFreshEntity(arrow);
        }

        // Consumed after the arrows are created so a last tipped arrow keeps its effect.
        if (!hasInfinity && !player.getAbilities().instabuild && config.consume_one_arrow_per_shot && !arrowStack.isEmpty()) {
            arrowStack.shrink(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeItem(arrowStack);
            }
        }

        if (config.shoot_sound_enabled) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, config.shoot_sound_volume, config.shoot_sound_pitch);
        }
    }

    private static AbstractArrow.Pickup parsePickupMode(String value) {
        if (value == null) {
            return AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "allowed" -> AbstractArrow.Pickup.ALLOWED;
            case "disallowed" -> AbstractArrow.Pickup.DISALLOWED;
            default -> AbstractArrow.Pickup.CREATIVE_ONLY;
        };
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
