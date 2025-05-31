package net.bandit.many_bows.fabric;

import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.fabricmc.api.ModInitializer;

import net.bandit.many_bows.ManyBowsMod;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManyBowsConfigHolder.CONFIG = BowLootConfig.loadConfig();
        ManyBowsMod.init();
    }
}
