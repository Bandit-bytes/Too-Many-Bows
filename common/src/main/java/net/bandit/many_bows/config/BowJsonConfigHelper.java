package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class BowJsonConfigHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path BOW_CONFIG_DIR = Platform.getConfigFolder()
            .resolve("too_many_bows")
            .resolve("bows");

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ReloadEntry<?>> REGISTERED_CONFIGS = new ConcurrentHashMap<>();

    private BowJsonConfigHelper() {
    }

    public static <T> T getConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        REGISTERED_CONFIGS.putIfAbsent(fileName, new ReloadEntry<>(configClass, defaultSupplier));
        return configClass.cast(CACHE.computeIfAbsent(fileName, key -> loadConfig(key, configClass, defaultSupplier)));
    }

    public static <T> T reloadConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        REGISTERED_CONFIGS.putIfAbsent(fileName, new ReloadEntry<>(configClass, defaultSupplier));
        T config = loadConfig(fileName, configClass, defaultSupplier);
        CACHE.put(fileName, config);
        return config;
    }

    public static int reloadAllConfigs() {
        int reloaded = 0;

        for (Map.Entry<String, ReloadEntry<?>> entry : REGISTERED_CONFIGS.entrySet()) {
            reloadRegistered(entry.getKey(), entry.getValue());
            reloaded++;
        }

        return reloaded;
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static <T> void saveConfig(String fileName, T config) {
        try {
            Files.createDirectories(BOW_CONFIG_DIR);
            Path file = BOW_CONFIG_DIR.resolve(fileName + ".json");

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }

            CACHE.put(fileName, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void reloadRegistered(String fileName, ReloadEntry<?> entry) {
        Object config = loadConfig(fileName, (Class) entry.configClass(), (Supplier) entry.defaultSupplier());
        CACHE.put(fileName, config);
    }

    private static <T> T loadConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        try {
            Files.createDirectories(BOW_CONFIG_DIR);
            Path file = BOW_CONFIG_DIR.resolve(fileName + ".json");

            T defaults = defaultSupplier.get();

            if (!Files.exists(file)) {
                try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    GSON.toJson(defaults, writer);
                }
                return defaults;
            }

            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                T loaded = GSON.fromJson(reader, configClass);
                return loaded != null ? loaded : defaults;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultSupplier.get();
        }
    }

    private record ReloadEntry<T>(Class<T> configClass, Supplier<T> defaultSupplier) {
    }

}