package net.bandit.many_bows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BowLootConfig {

    public static final String MOD_ID = "too_many_bows";

    private static final int CURRENT_CONFIG_VERSION = 2;
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final File CONFIG_FILE =
            new File("config/too_many_bows.json");

    private static final Map<String, Double> DEFAULT_BOW_POWER =
            createDefaultBowPower();

    public Integer configVersion = CURRENT_CONFIG_VERSION;

    public Map<String, Double> bowPower =
            createDefaultBowPower();

    public Boolean easyLootEnabled = true;
    public Float easyLootDropChance = 0.5F;

    public Boolean mediumLootEnabled = true;
    public Float mediumLootDropChance = 0.4F;

    public Boolean hardLootEnabled = true;
    public Float hardLootDropChance = 0.3F;

    public Boolean endgameLootEnabled = true;
    public Float endgameLootDropChance = 0.2F;

    public Boolean accessoryLootEnabled = true;
    public Float accessoryLootDropChance = 0.12F;

    public Integer emeraldSageXpAmount = 10;

    public List<String> emeraldSageCrimsonNexusBlacklist =
            new ArrayList<>(List.of(
                    "minecraft:armor_stand",
                    "minecraft:wandering_trader",
                    "dummmmmmy:target_dummy"
            ));

    public List<String> easyLootTables =
            new ArrayList<>(List.of(
                    "minecraft:chests/simple_dungeon",
                    "minecraft:chests/abandoned_mineshaft"
            ));

    public List<String> mediumLootTables =
            new ArrayList<>(List.of(
                    "minecraft:chests/jungle_temple",
                    "minecraft:chests/pillager_outpost",
                    "minecraft:chests/abandoned_mineshaft",
                    "minecraft:chests/simple_dungeon"
            ));

    public List<String> hardLootTables =
            new ArrayList<>(List.of(
                    "minecraft:chests/stronghold_corridor",
                    "minecraft:chests/nether_bridge",
                    "minecraft:chests/bastion_treasure"
            ));

    public List<String> endgameLootTables =
            new ArrayList<>(List.of(
                    "minecraft:chests/end_city_treasure",
                    "minecraft:chests/nether_bridge",
                    "minecraft:chests/bastion_treasure"
            ));

    public List<String> accessoryLootTables =
            new ArrayList<>(List.of(
                    "minecraft:chests/simple_dungeon",
                    "minecraft:chests/abandoned_mineshaft",
                    "minecraft:chests/pillager_outpost",
                    "minecraft:chests/jungle_temple",
                    "minecraft:chests/stronghold_corridor",
                    "minecraft:chests/woodland_mansion",
                    "minecraft:chests/ancient_city",
                    "minecraft:chests/bastion_treasure",
                    "minecraft:chests/end_city_treasure"
            ));

    public List<String> easyLootItems =
            new ArrayList<>(List.of(
                    MOD_ID + ":ancient_sage_bow",
                    MOD_ID + ":aethers_call",
                    MOD_ID + ":burnt_relic",
                    MOD_ID + ":power_crystal",
                    MOD_ID + ":emerald_sage_bow",
                    MOD_ID + ":torchbearer",
                    MOD_ID + ":demons_grasp"
            ));

    public List<String> mediumLootItems =
            new ArrayList<>(List.of(
                    MOD_ID + ":cyroheart_bow",
                    MOD_ID + ":power_crystal",
                    MOD_ID + ":torchbearer",
                    MOD_ID + ":emerald_sage_bow",
                    MOD_ID + ":demons_grasp"
            ));

    public List<String> hardLootItems =
            new ArrayList<>(List.of(
                    MOD_ID + ":sentinels_wrath",
                    MOD_ID + ":cursed_stone",
                    MOD_ID + ":solar_bow",
                    MOD_ID + ":arc_heavens",
                    MOD_ID + ":scatter_bow",
                    MOD_ID + ":vitality_weaver",
                    MOD_ID + ":spectral_whisper",
                    MOD_ID + ":webstring"
            ));

    public List<String> endgameLootItems =
            new ArrayList<>(List.of(
                    MOD_ID + ":flame_bow",
                    MOD_ID + ":dark_bow",
                    MOD_ID + ":arcane_bow",
                    MOD_ID + ":dragons_breath",
                    MOD_ID + ":wind_bow",
                    MOD_ID + ":cyroheart_bow",
                    MOD_ID + ":shulker_blast",
                    MOD_ID + ":astral_bound",
                    MOD_ID + ":auroras_grace"
            ));

    public List<String> accessoryLootItems =
            new ArrayList<>(List.of(
                    MOD_ID + ":wind_glove",
                    MOD_ID + ":sharpshot_ring",
                    MOD_ID + ":stormbound_signet",
                    MOD_ID + ":fletchers_talisman",
                    MOD_ID + ":dead_eyes_pendant"
            ));

    public static BowLootConfig loadConfig() {
        BowLootConfig config;

        if (!CONFIG_FILE.exists()) {
            config = new BowLootConfig();
            config.validateAndFillDefaults();
            config.saveConfig();

            System.out.println(
                    "[too_many_bows] Created config v"
                            + CURRENT_CONFIG_VERSION
                            + " at "
                            + CONFIG_FILE.getAbsolutePath()
            );

            return config;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, BowLootConfig.class);

            if (config == null) {
                config = new BowLootConfig();
            }
        } catch (Exception exception) {
            System.err.println(
                    "[too_many_bows] Failed to read "
                            + CONFIG_FILE.getAbsolutePath()
                            + "; recreating it."
            );
            exception.printStackTrace();
            config = new BowLootConfig();
        }

        config.validateAndFillDefaults();

        /*
         * Always rewrite the file so older configs receive newly added
         * settings without losing valid customized values.
         */
        config.saveConfig();

        System.out.println(
                "[too_many_bows] Loaded config v"
                        + config.configVersion
                        + " with "
                        + config.bowPower.size()
                        + " bow power entries from "
                        + CONFIG_FILE.getAbsolutePath()
        );

        return config;
    }

    public void saveConfig() {
        File parent = CONFIG_FILE.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE, false)) {
            GSON.toJson(this, writer);
            writer.flush();
        } catch (IOException exception) {
            System.err.println(
                    "[too_many_bows] Failed to save "
                            + CONFIG_FILE.getAbsolutePath()
            );
            exception.printStackTrace();
        }
    }

    public double getBowPowerAdjustment(String itemId) {
        Double defaultPower = DEFAULT_BOW_POWER.get(itemId);

        if (defaultPower == null) {
            return 0.0D;
        }

        Double configuredPower =
                bowPower == null ? null : bowPower.get(itemId);

        if (configuredPower == null
                || !Double.isFinite(configuredPower)
                || configuredPower < 0.0D) {
            return 0.0D;
        }

        return configuredPower - defaultPower;
    }

    public boolean validateAndFillDefaults() {
        boolean changed = false;
        BowLootConfig defaults = new BowLootConfig();

        if (configVersion == null
                || configVersion < CURRENT_CONFIG_VERSION) {
            configVersion = CURRENT_CONFIG_VERSION;
            changed = true;
        }

        if (bowPower == null) {
            bowPower = createDefaultBowPower();
            changed = true;
        } else {
            for (Map.Entry<String, Double> entry
                    : DEFAULT_BOW_POWER.entrySet()) {
                if (!bowPower.containsKey(entry.getKey())) {
                    bowPower.put(entry.getKey(), entry.getValue());
                    changed = true;
                }
            }

            for (Map.Entry<String, Double> entry
                    : new ArrayList<>(bowPower.entrySet())) {
                Double value = entry.getValue();

                if (value == null
                        || !Double.isFinite(value)
                        || value < 0.0D) {
                    bowPower.put(
                            entry.getKey(),
                            DEFAULT_BOW_POWER.getOrDefault(
                                    entry.getKey(),
                                    0.0D
                            )
                    );
                    changed = true;
                }
            }
        }

        if (easyLootEnabled == null) {
            easyLootEnabled = defaults.easyLootEnabled;
            changed = true;
        }
        if (mediumLootEnabled == null) {
            mediumLootEnabled = defaults.mediumLootEnabled;
            changed = true;
        }
        if (hardLootEnabled == null) {
            hardLootEnabled = defaults.hardLootEnabled;
            changed = true;
        }
        if (endgameLootEnabled == null) {
            endgameLootEnabled = defaults.endgameLootEnabled;
            changed = true;
        }
        if (accessoryLootEnabled == null) {
            accessoryLootEnabled = defaults.accessoryLootEnabled;
            changed = true;
        }

        changed |= validateEasyChance(defaults);
        changed |= validateMediumChance(defaults);
        changed |= validateHardChance(defaults);
        changed |= validateEndgameChance(defaults);
        changed |= validateAccessoryChance(defaults);

        if (emeraldSageXpAmount == null
                || emeraldSageXpAmount < 0) {
            emeraldSageXpAmount =
                    Math.max(0, defaults.emeraldSageXpAmount);
            changed = true;
        }

        if (emeraldSageCrimsonNexusBlacklist == null) {
            emeraldSageCrimsonNexusBlacklist =
                    new ArrayList<>(
                            defaults.emeraldSageCrimsonNexusBlacklist
                    );
            changed = true;
        }

        if (easyLootTables == null) {
            easyLootTables =
                    new ArrayList<>(defaults.easyLootTables);
            changed = true;
        }
        if (mediumLootTables == null) {
            mediumLootTables =
                    new ArrayList<>(defaults.mediumLootTables);
            changed = true;
        }
        if (hardLootTables == null) {
            hardLootTables =
                    new ArrayList<>(defaults.hardLootTables);
            changed = true;
        }
        if (endgameLootTables == null) {
            endgameLootTables =
                    new ArrayList<>(defaults.endgameLootTables);
            changed = true;
        }
        if (accessoryLootTables == null) {
            accessoryLootTables =
                    new ArrayList<>(defaults.accessoryLootTables);
            changed = true;
        }

        if (easyLootItems == null) {
            easyLootItems =
                    new ArrayList<>(defaults.easyLootItems);
            changed = true;
        }
        if (mediumLootItems == null) {
            mediumLootItems =
                    new ArrayList<>(defaults.mediumLootItems);
            changed = true;
        }
        if (hardLootItems == null) {
            hardLootItems =
                    new ArrayList<>(defaults.hardLootItems);
            changed = true;
        }
        if (endgameLootItems == null) {
            endgameLootItems =
                    new ArrayList<>(defaults.endgameLootItems);
            changed = true;
        }
        if (accessoryLootItems == null) {
            accessoryLootItems =
                    new ArrayList<>(defaults.accessoryLootItems);
            changed = true;
        }

        return changed;
    }

    private boolean validateEasyChance(BowLootConfig defaults) {
        Float result =
                clampChance(easyLootDropChance, defaults.easyLootDropChance);

        if (sameFloat(easyLootDropChance, result)) {
            return false;
        }

        easyLootDropChance = result;
        return true;
    }

    private boolean validateMediumChance(BowLootConfig defaults) {
        Float result =
                clampChance(
                        mediumLootDropChance,
                        defaults.mediumLootDropChance
                );

        if (sameFloat(mediumLootDropChance, result)) {
            return false;
        }

        mediumLootDropChance = result;
        return true;
    }

    private boolean validateHardChance(BowLootConfig defaults) {
        Float result =
                clampChance(hardLootDropChance, defaults.hardLootDropChance);

        if (sameFloat(hardLootDropChance, result)) {
            return false;
        }

        hardLootDropChance = result;
        return true;
    }

    private boolean validateEndgameChance(BowLootConfig defaults) {
        Float result =
                clampChance(
                        endgameLootDropChance,
                        defaults.endgameLootDropChance
                );

        if (sameFloat(endgameLootDropChance, result)) {
            return false;
        }

        endgameLootDropChance = result;
        return true;
    }

    private boolean validateAccessoryChance(BowLootConfig defaults) {
        Float result =
                clampChance(
                        accessoryLootDropChance,
                        defaults.accessoryLootDropChance
                );

        if (sameFloat(accessoryLootDropChance, result)) {
            return false;
        }

        accessoryLootDropChance = result;
        return true;
    }

    private static Float clampChance(
            Float value,
            Float defaultValue
    ) {
        if (value == null || !Float.isFinite(value)) {
            return defaultValue;
        }

        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static boolean sameFloat(
            Float first,
            Float second
    ) {
        if (first == null || second == null) {
            return first == second;
        }

        return Float.compare(first, second) == 0;
    }

    private static LinkedHashMap<String, Double>
    createDefaultBowPower() {
        LinkedHashMap<String, Double> values =
                new LinkedHashMap<>();

        values.put(MOD_ID + ":aethers_call", 2.0D);
        values.put(MOD_ID + ":ancient_sage_bow", 8.0D);
        values.put(MOD_ID + ":arc_heavens", 2.0D);
        values.put(MOD_ID + ":arcane_bow", 6.0D);
        values.put(MOD_ID + ":astral_bound", 7.0D);
        values.put(MOD_ID + ":auroras_grace", 7.0D);
        values.put(MOD_ID + ":burnt_relic", 2.0D);
        values.put(MOD_ID + ":crimson_nexus", 2.0D);
        values.put(MOD_ID + ":cyroheart_bow", 3.0D);
        values.put(MOD_ID + ":dark_bow", 20.0D);
        values.put(MOD_ID + ":demons_grasp", 2.0D);
        values.put(MOD_ID + ":dragons_breath", 5.0D);
        values.put(MOD_ID + ":dusk_reaper", 8.0D);
        values.put(MOD_ID + ":emerald_sage_bow", 2.0D);
        values.put(MOD_ID + ":ethereal_hunter", 9.0D);
        values.put(MOD_ID + ":flame_bow", 4.0D);
        values.put(MOD_ID + ":frostbite", 2.0D);
        values.put(MOD_ID + ":hunter_bow", 2.0D);
        values.put(MOD_ID + ":ironclad_bow", 8.0D);
        values.put(MOD_ID + ":necro_flame_bow", 2.0D);
        values.put(MOD_ID + ":radiance", 9.0D);
        values.put(MOD_ID + ":scatter_bow", 1.0D);
        values.put(MOD_ID + ":sentinels_wrath", 8.0D);
        values.put(MOD_ID + ":shulker_blast", 10.0D);
        values.put(MOD_ID + ":solar_bow", 6.0D);
        values.put(MOD_ID + ":spectral_whisper", 7.0D);
        values.put(MOD_ID + ":tidal_bow", 2.0D);
        values.put(MOD_ID + ":torchbearer", 5.0D);
        values.put(MOD_ID + ":twin_shadows", 5.0D);
        values.put(MOD_ID + ":verdant_vigor", 8.0D);
        values.put(MOD_ID + ":verdant_viper", 2.0D);
        values.put(MOD_ID + ":vitality_weaver", 8.0D);
        values.put(MOD_ID + ":webstring", 5.0D);
        values.put(MOD_ID + ":wind_bow", 3.0D);

        return values;
    }
}
