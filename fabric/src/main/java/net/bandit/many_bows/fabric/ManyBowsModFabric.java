package net.bandit.many_bows.fabric;

import dev.emi.trinkets.api.TrinketsApi;
import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.fabric.trinkets.DeadEyesPendantTrinket;
import net.bandit.many_bows.fabric.trinkets.DrawSpeedGloveTrinket;
import net.bandit.many_bows.fabric.trinkets.FletchersTalismanTrinket;
import net.bandit.many_bows.fabric.trinkets.SharpshotRingTrinket;
import net.bandit.many_bows.fabric.trinkets.StormboundSignetTrinket;
import net.bandit.many_bows.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;

public final class ManyBowsModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        /*
         * Common initialization owns config loading.
         * Do not call BowLootConfig.loadConfig() from the platform entrypoint.
         */
        ManyBowsMod.init();

        ModAttributesFabric.init();

        TrinketsApi.registerTrinket(
                ItemRegistry.WIND_GLOVE.get(),
                new DrawSpeedGloveTrinket()
        );

        TrinketsApi.registerTrinket(
                ItemRegistry.SHARPSHOT_RING.get(),
                new SharpshotRingTrinket()
        );

        TrinketsApi.registerTrinket(
                ItemRegistry.STORMBOUND_SIGNET.get(),
                new StormboundSignetTrinket()
        );

        TrinketsApi.registerTrinket(
                ItemRegistry.FLETCHERS_TALISMAN.get(),
                new FletchersTalismanTrinket()
        );

        TrinketsApi.registerTrinket(
                ItemRegistry.DEAD_EYES_PENDANT.get(),
                new DeadEyesPendantTrinket()
        );
    }
}
