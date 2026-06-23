package net.bandit.many_bows.fabric.client;

import net.bandit.many_bows.client.ManyBowsClient;
import net.fabricmc.api.ClientModInitializer;

public final class ManyBowsModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ManyBowsClient.init();
        ManyBowsFabricPredicates.init();
        ManyBowsFabricRenderers.init();
    }
}
