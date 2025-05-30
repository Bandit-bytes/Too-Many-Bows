package net.bandit.many_bows.fabric;

import net.fabricmc.api.ModInitializer;

import net.bandit.many_bows.ManyBowsMod;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManyBowsMod.init();
    }
}
