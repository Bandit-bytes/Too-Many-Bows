package net.bandit.many_bows.config;

public final class ManyBowsConfigHolder {
    public static BowLootConfig CONFIG;

    private ManyBowsConfigHolder() {
    }

    public static BowLootConfig get() {
        if (CONFIG == null) {
            CONFIG = BowLootConfig.loadConfig();
        }
        return CONFIG;
    }

    public static BowLootConfig reload() {
        CONFIG = BowLootConfig.loadConfig();
        return CONFIG;
    }
}