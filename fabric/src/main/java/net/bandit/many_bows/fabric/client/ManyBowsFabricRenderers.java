package net.bandit.many_bows.fabric.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.bandit.many_bows.fabric.client.trinkets.LanternTrinketRenderer;
import net.bandit.many_bows.registry.ItemRegistry;

public final class ManyBowsFabricRenderers {

    private ManyBowsFabricRenderers() {
    }

    public static void init() {
        LanternTrinketRenderer renderer = new LanternTrinketRenderer();

        TrinketRendererRegistry.registerRenderer(ItemRegistry.SOUL_LANTERN.get(), renderer);
        TrinketRendererRegistry.registerRenderer(ItemRegistry.CURSED_LANTERN.get(), renderer);
    }
}