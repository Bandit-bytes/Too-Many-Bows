package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
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

    protected ModBowItem(Properties properties) {
        super(properties);
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
