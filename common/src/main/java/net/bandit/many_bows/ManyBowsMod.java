package net.bandit.many_bows;

import net.bandit.many_bows.client.ClientInit;
import net.bandit.many_bows.registry.*;

public final class ManyBowsMod {
    public static final String MOD_ID = "too_many_bows";

    public static void init() {
        ItemRegistry.register();
        TabRegistry.init();
        EntityRegistry.register();
        EffectRegistry.register();
        EnchantmentRegistry.register();

    }

    public static void initClient() {
        ClientInit.registerClientProperties();
    }
}
