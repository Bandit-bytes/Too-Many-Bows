package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.PullSpeedItem;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public abstract class ModBowItem extends BowItem {

    private static final String FLETCHERS_TALISMAN_TAG =
            ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";
    private static final float FLETCHERS_SAVE_CHANCE = 0.25F;

    public static final Predicate<ItemStack> ARROW_ONLY = (arg) -> {
        return arg.is(ItemTags.ARROWS);
    };

    protected ModBowItem(Properties properties) {
        super(properties);
    }

    /**
     * Applies the player's bow damage attribute
     */
    protected void applyBowDamageAttribute(AbstractArrow arrow, Player player) {
        double mult = getBowDamageMultiplier(player);
        setArrowDamage(arrow, getArrowDamage(arrow) * mult);
    }

    protected double getBowDamageMultiplier(Player player) {
        var inst = player.getAttribute(AttributesRegistry.BOW_DAMAGE);
        if (inst == null) return 1.0D;
        double v = inst.getValue();
        return v <= 0.0D ? 1.0D : v;
    }

    protected double getBowCritChance(Player player) {
        var inst = player.getAttribute(AttributesRegistry.BOW_CRIT_CHANCE);
        if (inst == null) return 0.0D;
        return Math.max(0.0D, Math.min(1.0D, inst.getValue()));
    }


    /**
     * Rolls crit and boosts arrow damage if it crits (simple version)
     */
    protected boolean tryApplyBowCrit(AbstractArrow arrow, Player player, double critMultiplier) {
        double chance = getBowCritChance(player);
        if (chance <= 0.0D) return false;

        if (player.getRandom().nextDouble() < chance) {
            setArrowDamage(arrow, getArrowDamage(arrow) * critMultiplier);
            return true;
        }
        return false;
    }

    /**
     * Damages the bow unless Fletcher's Talisman prevents it
     */
    protected void damageBow(ItemStack bowStack, Player player, InteractionHand hand) {
        if (!bowStack.isDamageableItem()) return;

        if (player.getTags().contains(FLETCHERS_TALISMAN_TAG)) {
            if (player.getRandom().nextFloat() < FLETCHERS_SAVE_CHANCE) return;
        }

        EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND)
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        bowStack.hurtAndBreak(1, player, slot);
    }

    private static final ResourceKey<Attribute> BOW_DRAW_SPEED_KEY =
            ResourceKey.create(
                    Registries.ATTRIBUTE,
                    Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed")
            );

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Vanilla bow behavior expects this to be huge; don’t change it.
        return 72000;
    }


    /**
     * No getDamage() in your AbstractArrow, so read baseDamage via accessor
     */
    protected double getArrowDamage(AbstractArrow arrow) {
        return ((AbstractArrowAccessor) arrow).manybows$getBaseDamage();
    }

    /**
     * Write using the real setter that exists
     */
    protected void setArrowDamage(AbstractArrow arrow, double value) {
        arrow.setBaseDamage(value);
    }

    protected float manybows$getChargeMultiplier(ItemStack stack, LivingEntity entity) {
        float basePullTicks = ManyBowsConfigHolder.CONFIG.globalBowPullSpeed;

        if (this instanceof PullSpeedItem psi) {
            basePullTicks = psi.getPullTicks(stack, entity);
        }

        float drawSpeed = 1.0F;
        var lookup = entity.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var refOpt = lookup.get(BOW_DRAW_SPEED_KEY);

        if (refOpt.isPresent()) {
            AttributeInstance inst = entity.getAttribute(refOpt.get());
            if (inst != null) drawSpeed = (float) inst.getValue();
        }

        float pullTicks = basePullTicks / Math.max(0.05F, drawSpeed);

        return 20.0F / Math.max(1.0F, pullTicks);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return s-> false;
    }
}
