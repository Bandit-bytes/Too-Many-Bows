package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BowLootConfig {

    public static final String MOD_ID = "too_many_bows";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/too_many_bows.json");

    public Boolean easyLootEnabled = true;
    public Float easyLootDropChance = 0.5F;

    public Boolean mediumLootEnabled = true;
    public Float mediumLootDropChance = 0.4F;

    public Boolean hardLootEnabled = true;
    public Float hardLootDropChance = 0.3F;

    public Boolean endgameLootEnabled = true;
    public Float endgameLootDropChance = 0.2F;

    public Float globalBowPullSpeed = 16.0F;

    public Integer emeraldSageXpAmount = 10;
    public List<String> emeraldSageXpBlacklist = List.of(
            "minecraft:armor_stand",
            "minecraft:wandering_trader"
    );

    public List<String> easyLootTables = List.of(
            "minecraft:chests/simple_dungeon",
            "minecraft:chests/abandoned_mineshaft"
    );

    public List<String> mediumLootTables = List.of(
            "minecraft:chests/jungle_temple",
            "minecraft:chests/pillager_outpost",
            "minecraft:chests/abandoned_mineshaft",
            "minecraft:chests/simple_dungeon"
    );

    public List<String> hardLootTables = List.of(
            "minecraft:chests/stronghold_corridor",
            "minecraft:chests/nether_bridge",
            "minecraft:chests/bastion_treasure"
    );

    public List<String> endgameLootTables = List.of(
            "minecraft:chests/end_city_treasure",
            "minecraft:chests/nether_bridge",
            "minecraft:chests/bastion_treasure"
    );

    public List<String> easyLootItems = List.of(
            MOD_ID + ":ancient_sage_bow",
            MOD_ID + ":aethers_call",
            MOD_ID + ":burnt_relic",
            MOD_ID + ":arcane_bow",
            MOD_ID + ":sharpshot_ring",
            MOD_ID + ":cyroheart_bow",
            MOD_ID + ":power_crystal",
            MOD_ID + ":emerald_sage_bow",
            MOD_ID + ":torchbearer",
            MOD_ID + ":demons_grasp",
            MOD_ID + ":dead_eyes_pendant",
            MOD_ID + ":fletchers_talisman"
    );

    public List<String> mediumLootItems = List.of(
            MOD_ID + ":arcane_bow",
            MOD_ID + ":cyroheart_bow",
            MOD_ID + ":power_crystal",
            MOD_ID + ":torchbearer",
            MOD_ID + ":sharpshot_ring",
            MOD_ID + ":emerald_sage_bow",
            MOD_ID + ":demons_grasp",
            MOD_ID + ":stormbound_signet",
            MOD_ID + ":dead_eyes_pendant",
            MOD_ID + ":fletchers_talisman"
    );

    public List<String> hardLootItems = List.of(
            MOD_ID + ":sentinels_wrath",
            MOD_ID + ":cursed_stone",
            MOD_ID + ":solar_bow",
            MOD_ID + ":arc_heavens",
            MOD_ID + ":stormbound_signet",
            MOD_ID + ":scatter_bow",
            MOD_ID + ":sharpshot_ring",
            MOD_ID + ":wind_glove",
            MOD_ID + ":vitality_weaver",
            MOD_ID + ":spectral_whisper",
            MOD_ID + ":webstring",
            MOD_ID + ":dead_eyes_pendant",
            MOD_ID + ":fletchers_talisman"
    );

    public List<String> endgameLootItems = List.of(
            MOD_ID + ":flame_bow",
            MOD_ID + ":dark_bow",
            MOD_ID + ":dragons_breath",
            MOD_ID + ":wind_bow",
            MOD_ID + ":stormbound_signet",
            MOD_ID + ":wind_glove",
            MOD_ID + ":sharpshot_ring",
            MOD_ID + ":shulker_blast",
            MOD_ID + ":astral_bound",
            MOD_ID + ":auroras_grace",
            MOD_ID + ":dead_eyes_pendant",
            MOD_ID + ":fletchers_talisman"
    );

    public static BowLootConfig loadConfig() {
        BowLootConfig config;

        if (!CONFIG_FILE.exists()) {
            config = new BowLootConfig();
            config.saveConfig();
            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, BowLootConfig.class);
            if (config == null) config = new BowLootConfig();
        } catch (IOException e) {
            e.printStackTrace();
            config = new BowLootConfig();
        }

        boolean changed = config.validateAndFillDefaults();
        if (changed) config.saveConfig();

        return config;
    }

    public void saveConfig() {
        File parent = CONFIG_FILE.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validateAndFillDefaults() {
        boolean changed = false;
        BowLootConfig defaults = new BowLootConfig();

        if (easyLootEnabled == null) { easyLootEnabled = defaults.easyLootEnabled; changed = true; }
        if (easyLootDropChance == null) { easyLootDropChance = defaults.easyLootDropChance; changed = true; }

        if (mediumLootEnabled == null) { mediumLootEnabled = defaults.mediumLootEnabled; changed = true; }
        if (mediumLootDropChance == null) { mediumLootDropChance = defaults.mediumLootDropChance; changed = true; }

        if (hardLootEnabled == null) { hardLootEnabled = defaults.hardLootEnabled; changed = true; }
        if (hardLootDropChance == null) { hardLootDropChance = defaults.hardLootDropChance; changed = true; }

        if (endgameLootEnabled == null) { endgameLootEnabled = defaults.endgameLootEnabled; changed = true; }
        if (endgameLootDropChance == null) { endgameLootDropChance = defaults.endgameLootDropChance; changed = true; }

        if (globalBowPullSpeed == null) { globalBowPullSpeed = defaults.globalBowPullSpeed; changed = true; }

        if (emeraldSageXpAmount == null) { emeraldSageXpAmount = defaults.emeraldSageXpAmount; changed = true; }
        if (emeraldSageXpBlacklist == null) { emeraldSageXpBlacklist = new ArrayList<>(defaults.emeraldSageXpBlacklist); changed = true; }

        if (easyLootTables == null) { easyLootTables = new ArrayList<>(defaults.easyLootTables); changed = true; }
        if (mediumLootTables == null) { mediumLootTables = new ArrayList<>(defaults.mediumLootTables); changed = true; }
        if (hardLootTables == null) { hardLootTables = new ArrayList<>(defaults.hardLootTables); changed = true; }
        if (endgameLootTables == null) { endgameLootTables = new ArrayList<>(defaults.endgameLootTables); changed = true; }

        if (easyLootItems == null) { easyLootItems = new ArrayList<>(defaults.easyLootItems); changed = true; }
        if (mediumLootItems == null) { mediumLootItems = new ArrayList<>(defaults.mediumLootItems); changed = true; }
        if (hardLootItems == null) { hardLootItems = new ArrayList<>(defaults.hardLootItems); changed = true; }
        if (endgameLootItems == null) { endgameLootItems = new ArrayList<>(defaults.endgameLootItems); changed = true; }

        changed |= clampChanceFields();
        return changed;
    }

    private boolean clampChanceFields() {
        boolean changed = false;

        if (easyLootDropChance != null) {
            float c = clamp01(easyLootDropChance);
            if (c != easyLootDropChance) { easyLootDropChance = c; changed = true; }
        }
        if (mediumLootDropChance != null) {
            float c = clamp01(mediumLootDropChance);
            if (c != mediumLootDropChance) { mediumLootDropChance = c; changed = true; }
        }
        if (hardLootDropChance != null) {
            float c = clamp01(hardLootDropChance);
            if (c != hardLootDropChance) { hardLootDropChance = c; changed = true; }
        }
        if (endgameLootDropChance != null) {
            float c = clamp01(endgameLootDropChance);
            if (c != endgameLootDropChance) { endgameLootDropChance = c; changed = true; }
        }

        return changed;
    }

    private float clamp01(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }
}
