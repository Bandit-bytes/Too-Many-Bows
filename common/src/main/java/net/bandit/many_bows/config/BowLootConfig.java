package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BowLootConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/many_bows.json");

    public boolean easyLootEnabled = true;
    public float easyLootDropChance = 0.5F;

    public boolean mediumLootEnabled = true;
    public float mediumLootDropChance = 0.4F;

    public boolean hardLootEnabled = true;
    public float hardLootDropChance = 0.3F;

    public boolean endgameLootEnabled = true;
    public float endgameLootDropChance = 0.2F;

    public static BowLootConfig loadConfig() {
        if (!CONFIG_FILE.exists()) {
            BowLootConfig defaultConfig = new BowLootConfig();
            defaultConfig.saveConfig();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return GSON.fromJson(reader, BowLootConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new BowLootConfig();
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
