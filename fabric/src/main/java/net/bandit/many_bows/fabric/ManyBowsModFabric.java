package net.bandit.many_bows.fabric;

import eu.pb4.trinkets.api.callback.TrinketCallback;
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
        TrinketCallback.setCallback(ItemRegistry.WIND_GLOVE.get(), new DrawSpeedGloveTrinket());
        TrinketCallback.setCallback(ItemRegistry.SHARPSHOT_RING.get(), new SharpshotRingTrinket());
        TrinketCallback.setCallback(ItemRegistry.STORMBOUND_SIGNET.get(), new StormboundSignetTrinket());
        TrinketCallback.setCallback(ItemRegistry.FLETCHERS_TALISMAN.get(), new FletchersTalismanTrinket());
        TrinketCallback.setCallback(ItemRegistry.DEAD_EYES_PENDANT.get(), new DeadEyesPendantTrinket());
        TrinketCallback.setCallback(ItemRegistry.SOUL_LANTERN.get(), new SoulLanternTrinket());
        TrinketCallback.setCallback(ItemRegistry.CURSED_LANTERN.get(), new CursedLanternTrinket());
    }
}
