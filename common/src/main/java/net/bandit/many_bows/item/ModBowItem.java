package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModBowItem extends BowItem {

    private static final String FLETCHERS_TALISMAN_TAG =
            ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    private static final float FLETCHERS_SAVE_CHANCE = 0.25F;

    public ModBowItem(Properties properties) {
        super(properties);
    }


    public boolean hasInfinity(ItemStack stack, Player player) {
        return player.getAbilities().instabuild ||
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
    }


    public boolean canFireWithoutArrows(ItemStack stack, Player player) {
        return hasInfinity(stack, player);
    }


    protected void applyBowDamageAttribute(AbstractArrow arrow, Player player) {
        double mult = getBowDamageMultiplier(player);
        if (mult != 1.0D) {
            arrow.setBaseDamage(arrow.getBaseDamage() * mult);
        }
    }


    protected double getBowDamageMultiplier(Player player) {
        Attribute attr = safeGetAttribute(AttributesRegistry.BOW_DAMAGE.get());
        if (attr == null) return 1.0D;
        return player.getAttributeValue(attr);
    }


    protected double getBowCritChance(Player player) {
        Attribute attr = safeGetAttribute(AttributesRegistry.BOW_CRIT_CHANCE.get());
        if (attr == null) return 0.0D;

        double v = player.getAttributeValue(attr);
        if (v < 0.0D) return 0.0D;
        if (v > 1.0D) return 1.0D;
        return v;
    }


    protected boolean tryApplyBowCrit(AbstractArrow arrow, Player player, double critMultiplier) {
        double chance = getBowCritChance(player);
        if (chance <= 0.0D) return false;

        if (player.getRandom().nextDouble() < chance) {
            arrow.setBaseDamage(arrow.getBaseDamage() * critMultiplier);
            return true;
        }
        return false;
    }


    protected void damageBow(ItemStack bowStack, Player player, InteractionHand hand) {
        if (!bowStack.isDamageableItem()) return;

        if (player.getTags().contains(FLETCHERS_TALISMAN_TAG)) {
            if (player.getRandom().nextFloat() < FLETCHERS_SAVE_CHANCE) {
                return;
            }
        }

        bowStack.hurtAndBreak(1, player, (LivingEntity e) -> e.broadcastBreakEvent(hand));
    }


    private static Attribute safeGetAttribute(Attribute attr) {
        return attr;
    }
}
