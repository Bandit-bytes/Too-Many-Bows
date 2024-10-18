package net.bandit.many_bows.fabric;

import net.bandit.many_bows.fabric.loot.BowLootInjectorPlatformImpl;
import net.fabricmc.api.ModInitializer;

import net.bandit.many_bows.ManyBowsMod;

public final class ManyBowsModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ManyBowsMod.init();
        BowLootInjectorPlatformImpl.registerLootTables();
    }
}
