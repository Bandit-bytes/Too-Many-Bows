package net.bandit.many_bows.fabric;

import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.bandit.many_bows.config.PlatformCompatReloadRegistry;
import net.bandit.many_bows.fabric.client.ManyBowsFabricRenderers;
import net.bandit.many_bows.fabric.client.trinkets.LanternTrinketRenderer;
import net.bandit.many_bows.fabric.config.FabricCompatConfigHolder;
import net.bandit.many_bows.fabric.trinkets.*;
import net.bandit.many_bows.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;

import net.bandit.many_bows.ManyBowsMod;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PlatformCompatReloadRegistry.register(
                FabricCompatConfigHolder::preload,
                FabricCompatConfigHolder::reload
        );

        ManyBowsMod.init();
        ModAttributesFabric.init();
        ManyBowsFabricRenderers.init();
        LanternTrinketRenderer lanternRenderer = new LanternTrinketRenderer();

        TrinketRendererRegistry.registerRenderer(ItemRegistry.SOUL_LANTERN.get(), lanternRenderer);
        TrinketRendererRegistry.registerRenderer(ItemRegistry.CURSED_LANTERN.get(), lanternRenderer);
        TrinketsApi.registerTrinket(ItemRegistry.WIND_GLOVE.get(), new DrawSpeedGloveTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.SHARPSHOT_RING.get(), new SharpshotRingTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.STORMBOUND_SIGNET.get(), new StormboundSignetTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.FLETCHERS_TALISMAN.get(), new FletchersTalismanTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.DEAD_EYES_PENDANT.get(), new DeadEyesPendantTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.SOUL_LANTERN.get(), new SoulLanternTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.CURSED_LANTERN.get(), new CursedLanternTrinket());

    }
}
