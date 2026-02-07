package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.PullSpeedItem;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public abstract class ModBowItem extends BowItem {

    private static final String FLETCHERS_TALISMAN_TAG =
            ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";
    private static final float FLETCHERS_SAVE_CHANCE = 0.25F;
    private static final Identifier BOW_DAMAGE_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_damage");
    private static final Identifier BOW_CRIT_CHANCE_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_crit_chance");
    private static final Identifier BOW_DRAW_SPEED_ID =
            Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_draw_speed");

    @Nullable
    private static Holder<Attribute> manybows$getAttributeHolder(LivingEntity entity, Identifier id) {
        var lookup = entity.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        return lookup.get(id).orElse(null);
    }

    @Nullable
    private static AttributeInstance manybows$getAttributeInstance(LivingEntity entity, Identifier id) {
        Holder<Attribute> holder = manybows$getAttributeHolder(entity, id);
        return holder == null ? null : entity.getAttribute(holder);
    }

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
        AttributeInstance inst = manybows$getAttributeInstance(player, BOW_DAMAGE_ID);
        if (inst == null) return 1.0D;

        double v = inst.getValue();
        return v <= 0.0D ? 1.0D : v;
    }
    protected double getBowCritChance(Player player) {
        AttributeInstance inst = manybows$getAttributeInstance(player, BOW_CRIT_CHANCE_ID);
        if (inst == null) return 0.0D;
        return Math.max(0.0D, Math.min(1.0D, inst.getValue()));
    }
    /**
     * Rolls crit and boosts arrow damage if it crits
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
        if (entity instanceof Player player) {
            AttributeInstance inst = manybows$getAttributeInstance(player, BOW_DRAW_SPEED_ID);
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
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.onUseTick(level, user, stack, remainingUseTicks);

        if (!level.isClientSide()) return;
        if (!(user instanceof Player)) return;

        int rawCharge = this.getUseDuration(stack, user) - remainingUseTicks;

        float mult = this.manybows$getChargeMultiplier(stack, user);

        float scaledCharge = rawCharge * mult;

        float pull01 = Mth.clamp(scaledCharge / 20.0F, 0.0F, 1.0F);

        manybows$setPullVisual(stack, pull01);
    }
    protected void manybows$setPullVisual(ItemStack stack, float pull01) {
        stack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(
                        List.of(pull01),
                        List.of(),
                        List.of(),
                        List.of()
                )
        );
    }
    protected void manybows$resetPullVisual(ItemStack stack) {
        manybows$setPullVisual(stack, 0.0F);
    }
}
