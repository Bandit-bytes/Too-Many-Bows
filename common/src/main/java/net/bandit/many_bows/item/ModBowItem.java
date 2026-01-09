package net.bandit.many_bows.item;

import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;

public abstract class ModBowItem extends BowItem {

    private static final ResourceLocation BOW_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "bow_damage");

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

}
