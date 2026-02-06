package net.bandit.many_bows.fabric;

//import dev.emi.trinkets.api.TrinketsApi;
//import net.bandit.many_bows.fabric.trinkets.*;
//import net.bandit.many_bows.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;

import net.bandit.many_bows.ManyBowsMod;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManyBowsMod.init();
        ModAttributesFabric.init();
//        TrinketsApi.registerTrinket(ItemRegistry.WIND_GLOVE.get(), new DrawSpeedGloveTrinket());
//        TrinketsApi.registerTrinket(ItemRegistry.SHARPSHOT_RING.get(), new SharpshotRingTrinket());
//        TrinketsApi.registerTrinket(ItemRegistry.STORMBOUND_SIGNET.get(), new StormboundSignetTrinket());
//        TrinketsApi.registerTrinket(ItemRegistry.FLETCHERS_TALISMAN.get(), new FletchersTalismanTrinket());
//        TrinketsApi.registerTrinket(ItemRegistry.DEAD_EYES_PENDANT.get(), new DeadEyesPendantTrinket());

    }
}
