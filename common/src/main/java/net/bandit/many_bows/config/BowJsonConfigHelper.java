package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private BowJsonConfigHelper() {
    }

    public static <T> T getConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        return configClass.cast(CACHE.computeIfAbsent(fileName, key -> loadConfig(key, configClass, defaultSupplier)));
    }

    public static <T> T reloadConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        T config = loadConfig(fileName, configClass, defaultSupplier);
        CACHE.put(fileName, config);
        return config;
    }

    public static <T> void saveConfig(String fileName, T config) {
        try {
            writeConfigToDisk(fileName, config);
            CACHE.put(fileName, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T> T loadConfig(String fileName, Class<T> configClass, Supplier<T> defaultSupplier) {
        try {
            Files.createDirectories(BOW_CONFIG_DIR);
            Path file = BOW_CONFIG_DIR.resolve(fileName + ".json");

            T defaults = defaultSupplier.get();

            if (!Files.exists(file)) {
                writeConfigToDisk(fileName, defaults);
                return defaults;
            }

            JsonObject defaultJson = GSON.toJsonTree(defaults).getAsJsonObject();
            JsonObject mergedJson = defaultJson.deepCopy();

            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);

                if (parsed != null && parsed.isJsonObject()) {
                    mergeInto(mergedJson, parsed.getAsJsonObject());
                }
            }

            T loaded = GSON.fromJson(mergedJson, configClass);
            T finalConfig = loaded != null ? loaded : defaults;

            // Re-save updated fields to disk, but DO NOT touch CACHE here
            writeConfigToDisk(fileName, finalConfig);

            return finalConfig;

        } catch (Exception e) {
            e.printStackTrace();
            return defaultSupplier.get();
        }
    }

    private static <T> void writeConfigToDisk(String fileName, T config) throws Exception {
        Files.createDirectories(BOW_CONFIG_DIR);
        Path file = BOW_CONFIG_DIR.resolve(fileName + ".json");

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
    }

    private static void mergeInto(JsonObject base, JsonObject loaded) {
        for (Map.Entry<String, JsonElement> entry : loaded.entrySet()) {
            String key = entry.getKey();
            JsonElement loadedValue = entry.getValue();

            if (base.has(key) && base.get(key).isJsonObject() && loadedValue.isJsonObject()) {
                mergeInto(base.getAsJsonObject(key), loadedValue.getAsJsonObject());
            } else {
                base.add(key, loadedValue);
            }
        }
    }
}