package net.bandit.many_bows.config;

public final class ManyBowsConfigHolder {

    public static BowLootConfig CONFIG =
            BowLootConfig.loadConfig();

    private ManyBowsConfigHolder() {
    }

    public static BowLootConfig getConfig() {
        return CONFIG;
    }
}
