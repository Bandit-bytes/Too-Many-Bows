package net.bandit.many_bows.fabric;

import dev.emi.trinkets.api.TrinketsApi;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.config.PlatformCompatReloadRegistry;
import net.bandit.many_bows.fabric.config.FabricCompatConfigHolder;
import net.bandit.many_bows.fabric.trinkets.*;
import net.bandit.many_bows.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PlatformCompatReloadRegistry.register(
                FabricCompatConfigHolder::preload,
                FabricCompatConfigHolder::reload
        );

        ManyBowsMod.init();
        ModAttributesFabric.init();
        TrinketsApi.registerTrinket(ItemRegistry.WIND_GLOVE.get(), new DrawSpeedGloveTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.SHARPSHOT_RING.get(), new SharpshotRingTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.STORMBOUND_SIGNET.get(), new StormboundSignetTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.FLETCHERS_TALISMAN.get(), new FletchersTalismanTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.DEAD_EYES_PENDANT.get(), new DeadEyesPendantTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.SOUL_LANTERN.get(), new SoulLanternTrinket());
        TrinketsApi.registerTrinket(ItemRegistry.CURSED_LANTERN.get(), new CursedLanternTrinket());
    }
}
