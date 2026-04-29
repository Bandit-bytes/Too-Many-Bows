package net.bandit.many_bows.fabric.config;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public final class FabricCompatConfigHolder {
    private static final String FILE_NAME = "all_accessories";

    private FabricCompatConfigHolder() {
    }

    public static FabricCompatConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, FabricCompatConfig.class, FabricCompatConfig::new);
    }

    public static void preload() {
        get();
    }

    public static void reload() {
        BowJsonConfigHelper.reloadConfig(FILE_NAME, FabricCompatConfig.class, FabricCompatConfig::new);
    }
}