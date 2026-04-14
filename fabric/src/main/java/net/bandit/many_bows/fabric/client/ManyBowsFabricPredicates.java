package net.bandit.many_bows.fabric.client;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.client.renderer.LanternRenderState;
import net.bandit.many_bows.registry.ItemRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.resources.ResourceLocation;

public final class ManyBowsFabricPredicates {

    private static final ResourceLocation WORN_PREDICATE =
            ResourceLocation.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "worn");

    private ManyBowsFabricPredicates() {
    }

    public static void init() {
        FabricModelPredicateProviderRegistry.register(
                ItemRegistry.SOUL_LANTERN.get(),
                WORN_PREDICATE,
                (stack, world, entity, seed) -> LanternRenderState.isEquippedRender() ? 1.0F : 0.0F
        );

        FabricModelPredicateProviderRegistry.register(
                ItemRegistry.CURSED_LANTERN.get(),
                WORN_PREDICATE,
                (stack, world, entity, seed) -> LanternRenderState.isEquippedRender() ? 1.0F : 0.0F
        );
    }
}