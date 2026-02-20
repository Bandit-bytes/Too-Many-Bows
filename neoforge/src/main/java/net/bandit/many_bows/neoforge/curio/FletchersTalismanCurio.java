package net.bandit.many_bows.neoforge.curio;

import net.bandit.many_bows.ManyBowsMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class FletchersTalismanCurio implements ICurioItem {

    public static final String EQUIPPED_TAG = ManyBowsMod.MOD_ID + ":fletchers_talisman_equipped";

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        Entity entity = slotContext.entity();
        if (!entity.getTags().contains(EQUIPPED_TAG)) {
            entity.addTag(EQUIPPED_TAG);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        slotContext.entity().removeTag(EQUIPPED_TAG);
    }
}
