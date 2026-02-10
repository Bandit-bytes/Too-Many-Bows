package net.bandit.many_bows.fabric.trinkets;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;


public class FletchersTalismanTrinket implements Trinket {

    public static final String EQUIPPED_TAG = ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getTags().contains(EQUIPPED_TAG)) {
            entity.addTag(EQUIPPED_TAG);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        entity.removeTag(EQUIPPED_TAG);
    }
}
