package net.bandit.many_bows.fabric.trinkets;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;


public class FletchersTalismanTrinket implements TrinketCallback {

    public static final String EQUIPPED_TAG = ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    @Override
    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!entity.entityTags().contains(EQUIPPED_TAG)) {
            entity.addTag(EQUIPPED_TAG);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        entity.removeTag(EQUIPPED_TAG);
    }
}
