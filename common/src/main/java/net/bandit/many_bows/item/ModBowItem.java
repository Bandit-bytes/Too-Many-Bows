package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public abstract class ModBowItem extends BowItem {

    private static final ResourceLocation BOW_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_damage");

    private static final ResourceLocation BOW_CRIT_CHANCE_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_crit_chance");

    private static final String FLETCHERS_TALISMAN_TAG =
            ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    private static final float FLETCHERS_SAVE_CHANCE = 0.25F;

    private static final ResourceLocation BOW_DRAW_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed");

    private static final float DEFAULT_PULL_TICKS = 16.0F;
    private static final float MIN_DRAW_SPEED = 0.05F;

    protected ModBowItem(Properties properties) {
        super(properties);
    }

    /**
     * Single source of truth for draw time. Used by the server (shot power) and the
     * client (pull animation) so both reach full draw on the same tick.
     *
     * @return ticks needed to reach full draw: config base / bow_draw_speed attribute
     */
    public static float getPullTicks(LivingEntity entity) {
        Float configured = ManyBowsConfigHolder.get().globalBowPullSpeed;
        float base = configured != null && configured > 0.0F ? configured : DEFAULT_PULL_TICKS;
        return base / getDrawSpeedMultiplier(entity);
    }

    /** Returns the entity's bow_draw_speed attribute value (1.0 if missing/unregistered) */
    public static float getDrawSpeedMultiplier(LivingEntity entity) {
        if (entity == null) return 1.0F;

        Holder<Attribute> holder = entity.level().registryAccess()
                .registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(BOW_DRAW_SPEED_ID)
                .orElse(null);
        if (holder == null) return 1.0F;

        AttributeInstance instance = entity.getAttribute(holder);
        if (instance == null) return 1.0F;

        return Math.max(MIN_DRAW_SPEED, (float) instance.getValue());
    }

    /** Vanilla bow power curve, scaled to the entity's draw time instead of a fixed 20 ticks */
    public static float getPowerForTime(int charge, LivingEntity entity) {
        float f = charge / getPullTicks(entity);
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    /**
     * Returns attribute value / divisor + flatBonus when ranged_weapon:damage is available and enabled,
     * otherwise the configured fallback damage.
     */
    protected double resolveConfiguredDamage(Player player, double fallback, boolean useAttribute, double divisor, double flatBonus) {
        if (useAttribute && divisor > 0.0D) {
            Holder<Attribute> holder = player.level().registryAccess()
                    .registryOrThrow(Registries.ATTRIBUTE)
                    .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                    .orElse(null);

            if (holder != null) {
                AttributeInstance instance = player.getAttribute(holder);
                if (instance != null) {
                    return instance.getValue() / divisor + flatBonus;
                }
            }
        }
        return fallback;
    }

    /**
     * Returns how much the player's gear raises ranged_weapon:damage above its base value
     * (0 if Ranged Weapon API is absent). Only modifiers count, so it adds on top of a bow's
     * own damage instead of replacing it.
     */
    protected double getRangedWeaponGearBonus(Player player) {
        Holder<Attribute> holder = player.level().registryAccess()
                .registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                .orElse(null);
        if (holder == null) return 0.0D;

        AttributeInstance instance = player.getAttribute(holder);
        if (instance == null) return 0.0D;

        return Math.max(0.0D, instance.getValue() - instance.getBaseValue());
    }

    /** Applies the player's bow damage attribute */
    protected void applyBowDamageAttribute(AbstractArrow arrow, Player player) {
        double mult = getBowDamageMultiplier(player);
        arrow.setBaseDamage(arrow.getBaseDamage() * mult);
    }

    /** Returns bow damage multiplier (1.0 if missing/unregistered) */
    protected double getBowDamageMultiplier(Player player) {
        Holder<Attribute> holder = player.level().registryAccess()
                .registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(BOW_DAMAGE_ID)
                .orElse(null);

        if (holder == null) return 1.0D;

        return player.getAttributeValue(holder);
    }

    /** Returns bow crit chance (0.0 if missing/unregistered) */
    protected double getBowCritChance(Player player) {
        Holder<Attribute> holder = player.level().registryAccess()
                .registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(BOW_CRIT_CHANCE_ID)
                .orElse(null);

        if (holder == null) return 0.0D;

        double v = player.getAttributeValue(holder);
        return Math.max(0.0D, Math.min(1.0D, v));
    }

    /** Rolls crit and boosts arrow damage if it crits (simple version) */
    protected boolean tryApplyBowCrit(AbstractArrow arrow, Player player, double critMultiplier) {
        double chance = getBowCritChance(player);
        if (chance <= 0.0D) return false;

        if (player.getRandom().nextDouble() < chance) {
            arrow.setBaseDamage(arrow.getBaseDamage() * critMultiplier);
            return true;
        }
        return false;
    }
    /** Damages the bow unless Fletcher's Talisman prevents it */
    protected void damageBow(ItemStack bowStack, Player player, InteractionHand hand) {
        if (!bowStack.isDamageableItem()) return;

        if (player.getTags().contains(FLETCHERS_TALISMAN_TAG)) {
            if (player.getRandom().nextFloat() < FLETCHERS_SAVE_CHANCE) {
                return;
            }
        }
        bowStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
    }
}
