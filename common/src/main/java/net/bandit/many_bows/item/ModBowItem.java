package net.bandit.many_bows.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModBowItem extends BowItem {

    public ModBowItem(Properties properties) {
        super(properties);
    }

    /**
     * Checks if the bow has the Infinity enchantment and the player is in Creative mode.
     * If so, allows firing without arrows in inventory.
     */
    public boolean hasInfinity(ItemStack stack, Player player) {
        return player.getAbilities().instabuild ||
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
    }

    /**
     * Checks if the bow can fire without requiring arrows in the player's inventory.
     */
    public boolean canFireWithoutArrows(ItemStack stack, Player player) {
        return hasInfinity(stack, player);
    }
}
