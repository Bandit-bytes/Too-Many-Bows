package net.bandit.many_bows.fabric.client;

import net.bandit.many_bows.ManyBowsMod;
import net.fabricmc.api.ClientModInitializer;

public final class ManyBowsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ManyBowsMod.initClient();
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
