package net.bandit.many_bows.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Supplies built-in bow balance profiles without creating per-bow files.
 * Only config/too_many_bows.json is user-facing.
 */
public final class BowJsonConfigHelper {

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    private BowJsonConfigHelper() {
    }

    public static <T> T getConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        return configClass.cast(CACHE.computeIfAbsent(fileName, ignored -> defaultSupplier.get()));
    }

    public static <T> T reloadConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        T config = defaultSupplier.get();
        CACHE.put(fileName, config);
        return config;
    }

    /** Kept for source compatibility with older bow code. Nothing is written to disk. */
    public static <T> void saveConfig(String fileName, T config) {
        CACHE.put(fileName, config);
    }
}
