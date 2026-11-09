package net.bandit.many_bows.config;

public final class AccessoriesConfigHolder {
    private static final String FILE_NAME = "all_accessories";

    private AccessoriesConfigHolder() {
    }

    public static AccessoriesConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, AccessoriesConfig.class, AccessoriesConfig::new);
    }

    public static void preload() {
        get();
    }

    public static void reload() {
        BowJsonConfigHelper.reloadConfig(FILE_NAME, AccessoriesConfig.class, AccessoriesConfig::new);
    }
}
