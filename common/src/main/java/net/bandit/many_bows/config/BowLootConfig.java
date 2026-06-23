package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small, server-friendly configuration for Too Many Bows.
 *
 * Individual bow behavior is intentionally kept as balanced built-in defaults.
 * This leaves only the broad controls that are useful to pack/server owners.
 */
public final class BowLootConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_FILE = Platform.getConfigFolder()
            .resolve("too_many_bows.json");

    /** Multiplies the final direct arrow damage of every mod bow. */
    public Double globalBowPowerMultiplier = 1.0D;

    /** Base number of ticks needed for a full draw. Lower is faster. */
    public Float globalBowPullSpeed = 16.0F;

    /** Enables the built-in chest loot additions. */
    public Boolean enableChestLoot = true;

    /** Multiplies all built-in chest loot chances. */
    public Float lootChanceMultiplier = 1.0F;

    public static BowLootConfig loadConfig() {
        BowLootConfig config = new BowLootConfig();

        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                BowLootConfig loaded = GSON.fromJson(reader, BowLootConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                System.err.println("[too_many_bows] Could not read config; using defaults.");
                e.printStackTrace();
            }
        }

        config.validate();

        // Always rewrite so old per-tier/list-heavy versions migrate to the new compact format.
        config.saveConfig();
        return config;
    }

    public void saveConfig() {
        try {
            Path parent = CONFIG_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[too_many_bows] Could not save config.");
            e.printStackTrace();
        }
    }

    private void validate() {
        if (globalBowPowerMultiplier == null || !Double.isFinite(globalBowPowerMultiplier)) {
            globalBowPowerMultiplier = 1.0D;
        }
        globalBowPowerMultiplier = clamp(globalBowPowerMultiplier, 0.05D, 20.0D);

        if (globalBowPullSpeed == null || !Float.isFinite(globalBowPullSpeed)) {
            globalBowPullSpeed = 16.0F;
        }
        globalBowPullSpeed = (float) clamp(globalBowPullSpeed, 1.0D, 200.0D);

        if (enableChestLoot == null) {
            enableChestLoot = true;
        }

        if (lootChanceMultiplier == null || !Float.isFinite(lootChanceMultiplier)) {
            lootChanceMultiplier = 1.0F;
        }
        lootChanceMultiplier = (float) clamp(lootChanceMultiplier, 0.0D, 10.0D);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
