package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.VitalityArrow;
import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VerdantVigorBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VerdantVigorBow extends ModBowItem {

    public VerdantVigorBow(Properties properties) {
        super(properties);
    }

    private static VerdantVigorBowConfig config() {
        return BowJsonConfigHelper.getConfig("verdant_vigor", VerdantVigorBowConfig.class, VerdantVigorBowConfig::new);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || world.isClientSide) return;

        boolean isHoldingBow = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        if (!isHoldingBow) return;

        VerdantVigorBowConfig config = config();

        if (config.health_boost_enabled) {
            MobEffectInstance healthBoost = player.getEffect(MobEffects.HEALTH_BOOST);
            if (healthBoost == null
                    || healthBoost.getAmplifier() < config.health_boost_amplifier
                    || (healthBoost.getAmplifier() == config.health_boost_amplifier
                    && healthBoost.getDuration() < config.health_boost_refresh_below_ticks)) {
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,
                        config.health_boost_duration_ticks, config.health_boost_amplifier, true, false, false));
                if (config.health_boost_refresh_heal > 0.0F && player.getHealth() < player.getMaxHealth()) {
                    player.heal(config.health_boost_refresh_heal);
                }
            }
        }

        if (config.ally_regeneration_enabled
                && world.getGameTime() % Math.max(1, config.ally_regeneration_interval_ticks) == 0) {
            double radius = config.ally_regeneration_radius;
            AABB area = new AABB(
                    player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                    player.getX() + radius, player.getY() + radius, player.getZ() + radius
            );

            world.getEntities(player, area, e ->
                    e instanceof LivingEntity
                            && e.isAlive()
                            && e != player
                            && (e.getType().getCategory().isFriendly() || e instanceof Player)
            ).forEach(entityNearby -> ((LivingEntity) entityNearby).addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    config.ally_regeneration_duration_ticks,
                    config.ally_regeneration_amplifier,
                    true, false, false
            )));
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge, entity);

            boolean isCreative = player.getAbilities().instabuild;
            boolean hasInfinity = hasInfinity(stack, player);
            boolean canFireNoArrows = canFireWithoutArrows(stack, player);

            ItemStack arrowStack = player.getProjectile(stack);
            boolean hasArrows = !arrowStack.isEmpty() || isCreative || canFireNoArrows;

            if (power >= 0.1F && hasArrows) {
                if (arrowStack.isEmpty() && canFireNoArrows) {
                    arrowStack = new ItemStack(Items.ARROW);
                }

                VerdantVigorBowConfig config = config();
                VitalityArrow arrow = new VitalityArrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                        power * config.arrow_velocity, config.arrow_inaccuracy);
                arrow.setBaseDamage(resolveConfiguredDamage(player, config.base_damage,
                        config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, 0.0D));

                applyStandardEnchantments(arrow, stack);

                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

                arrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;

                if (config.wither_on_hit_enabled) {
                    int witherTicks = config.wither_on_hit_duration_ticks;
                    int witherAmplifier = config.wither_on_hit_amplifier;
                    arrow.setOnHitCallback(target ->
                            target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherTicks, witherAmplifier)));
                }

                level.addFreshEntity(arrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (!isCreative && !hasInfinity && !arrowStack.isEmpty()) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }

                damageBow(stack, player, player.getUsedItemHand());
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
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

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.verdant_vigor.tooltip").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.many_bows.verdant_vigor.tooltip.ability").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
