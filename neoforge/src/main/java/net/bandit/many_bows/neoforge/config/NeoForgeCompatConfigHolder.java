package net.bandit.many_bows.neoforge.config;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public final class NeoForgeCompatConfigHolder {
    private static final String FILE_NAME = "all_accessories";

    private NeoForgeCompatConfigHolder() {
    }

    public static NeoForgeCompatConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, NeoForgeCompatConfig.class, NeoForgeCompatConfig::new);
    }

    public static void preload() {
        get();
    }

    public static void reload() {
        BowJsonConfigHelper.reloadConfig(FILE_NAME, NeoForgeCompatConfig.class, NeoForgeCompatConfig::new);
    }
}