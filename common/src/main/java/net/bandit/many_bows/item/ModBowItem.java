package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class ModBowItem extends BowItem {

    private static final String FLETCHERS_TALISMAN_TAG =
            ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    private static final float FLETCHERS_SAVE_CHANCE = 0.25F;
    private static final double DEFAULT_CRIT_MULTIPLIER = 1.5D;

    public ModBowItem(Properties properties) {
        super(properties);
    }

    public boolean hasInfinity(ItemStack stack, Player player) {
        return player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.INFINITY_ARROWS,
                stack
        ) > 0;
    }

    public boolean canFireWithoutArrows(ItemStack stack, Player player) {
        return hasInfinity(stack, player);
    }

    /**
     * Shared vanilla-style firing path for bows that do not provide their
     * own releaseUsing implementation.
     *
     * Custom bow classes may override releaseUsing while still using the
     * shared configurable damage methods in this class.
     */
    @Override
    public void releaseUsing(
            ItemStack bowStack,
            Level level,
            LivingEntity shooter,
            int timeLeft
    ) {
        if (!(shooter instanceof Player player)) {
            return;
        }

        boolean hasInfinity = hasInfinity(bowStack, player);
        ItemStack arrowStack = player.getProjectile(bowStack);

        if (arrowStack.isEmpty() && !hasInfinity) {
            return;
        }

        if (arrowStack.isEmpty()) {
            arrowStack = new ItemStack(Items.ARROW);
        }

        int charge = getUseDuration(bowStack) - timeLeft;
        float drawPower = getPowerForTime(charge);

        if (drawPower < 0.1F) {
            return;
        }

        boolean infiniteNormalArrow =
                hasInfinity && arrowStack.is(Items.ARROW);

        if (!level.isClientSide) {
            ArrowItem arrowItem;

            if (arrowStack.getItem() instanceof ArrowItem item) {
                arrowItem = item;
            } else {
                arrowItem = (ArrowItem) Items.ARROW;
            }

            AbstractArrow arrow =
                    arrowItem.createArrow(level, arrowStack, player);

            arrow.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    drawPower * 3.0F,
                    1.0F
            );

            if (drawPower == 1.0F) {
                arrow.setCritArrow(true);
            }

            int powerLevel =
                    EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.POWER_ARROWS,
                            bowStack
                    );

            if (powerLevel > 0) {
                arrow.setBaseDamage(
                        arrow.getBaseDamage()
                                + powerLevel * 0.5D
                                + 0.5D
                );
            }

            int punchLevel =
                    EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.PUNCH_ARROWS,
                            bowStack
                    );

            if (punchLevel > 0) {
                arrow.setKnockback(punchLevel);
            }

            int flameLevel =
                    EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.FLAMING_ARROWS,
                            bowStack
                    );

            if (flameLevel > 0) {
                arrow.setSecondsOnFire(100);
            }

            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(
                    arrow,
                    player,
                    DEFAULT_CRIT_MULTIPLIER
            );

            if (infiniteNormalArrow
                    || player.getAbilities().instabuild) {
                arrow.pickup =
                        AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            damageBow(
                    bowStack,
                    player,
                    player.getUsedItemHand()
            );

            level.addFreshEntity(arrow);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
                        / (
                        level.getRandom().nextFloat()
                                * 0.4F
                                + 1.2F
                )
                        + drawPower * 0.5F
        );

        if (!infiniteNormalArrow
                && !player.getAbilities().instabuild) {
            arrowStack.shrink(1);

            if (arrowStack.isEmpty()) {
                player.getInventory().removeItem(arrowStack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    /**
     * Applies this bow's configured power adjustment and then applies the
     * player's equipped bow-damage accessory multiplier.
     */
    protected void applyBowDamageAttribute(
            AbstractArrow arrow,
            Player player
    ) {
        String itemId =
                BuiltInRegistries.ITEM
                        .getKey(this)
                        .toString();

        double configuredAdjustment =
                ManyBowsConfigHolder.CONFIG
                        .getBowPowerAdjustment(itemId);

        if (configuredAdjustment != 0.0D) {
            double adjustedDamage =
                    arrow.getBaseDamage()
                            + configuredAdjustment;

            arrow.setBaseDamage(
                    Math.max(0.0D, adjustedDamage)
            );
        }

        double accessoryMultiplier =
                getBowDamageMultiplier(player);

        if (accessoryMultiplier != 1.0D) {
            arrow.setBaseDamage(
                    arrow.getBaseDamage()
                            * accessoryMultiplier
            );
        }
    }

    protected double getBowDamageMultiplier(Player player) {
        Attribute attribute =
                safeGetAttribute(
                        AttributesRegistry.BOW_DAMAGE.get()
                );

        if (attribute == null) {
            return 1.0D;
        }

        return player.getAttributeValue(attribute);
    }

    protected double getBowCritChance(Player player) {
        Attribute attribute =
                safeGetAttribute(
                        AttributesRegistry.BOW_CRIT_CHANCE.get()
                );

        if (attribute == null) {
            return 0.0D;
        }

        double value =
                player.getAttributeValue(attribute);

        return Math.max(0.0D, Math.min(1.0D, value));
    }

    protected boolean tryApplyBowCrit(
            AbstractArrow arrow,
            Player player,
            double critMultiplier
    ) {
        double chance = getBowCritChance(player);

        if (chance <= 0.0D) {
            return false;
        }

        if (player.getRandom().nextDouble() < chance) {
            arrow.setBaseDamage(
                    arrow.getBaseDamage()
                            * critMultiplier
            );

            return true;
        }

        return false;
    }

    protected void damageBow(
            ItemStack bowStack,
            Player player,
            InteractionHand hand
    ) {
        if (!bowStack.isDamageableItem()) {
            return;
        }

        boolean saveDurability =
                player.getTags()
                        .contains(FLETCHERS_TALISMAN_TAG)
                        && player.getRandom().nextFloat()
                        < FLETCHERS_SAVE_CHANCE;

        if (saveDurability) {
            return;
        }

        bowStack.hurtAndBreak(
                1,
                player,
                entity -> entity.broadcastBreakEvent(hand)
        );
    }

    private static Attribute safeGetAttribute(Attribute attribute) {
        return attribute;
    }
}
