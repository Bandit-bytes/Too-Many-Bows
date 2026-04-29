package net.bandit.many_bows.neoforge.client;

import net.bandit.many_bows.ManyBowsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ManyBowsMod.MOD_ID, value = Dist.CLIENT)
public final class ManyBowsNeoForgePredicates {

    private ManyBowsNeoForgePredicates() {
    }

    /*
     * 1.21.11 removed/reworked the old ItemProperties predicate system.
     *
     * The old "worn" predicate:
     *
     * ItemProperties.register(item, worn, ...)
     *
     * needs to be replaced later with a 1.21.11 client item model condition
     * using RegisterConditionalItemModelPropertyEvent + assets/<modid>/items/*.json.
     */
}