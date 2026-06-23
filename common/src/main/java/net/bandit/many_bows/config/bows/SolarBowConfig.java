package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SolarBowConfig {

    public static final String FILE_NAME = "solar_bow";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 240;
    public boolean allow_pickup = false;

    // Tornado
    public int tornado_duration_ticks = 100;
    public double tornado_radius = 3.5D;
    public int tornado_fire_ticks = 40;

    // Tornado damage
    public boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_divisor = 1.5F;
    public float tornado_damage_fallback = 4.0F;
    public float final_tornado_damage_multiplier = 1.0F;

    // Target rules
    public boolean exclude_owner = true;
    public boolean exclude_allies_of_owner = true;

    // Visuals / sound
    public boolean spawn_air_spiral_particles = true;
    public boolean spawn_tornado_particles = true;
    public int startup_explosion_particles = 15;

    public int ambient_sound_interval_ticks = 20;
    public float ambient_sound_volume = 1.0F;
    public float ambient_sound_pitch_min = 0.8F;
    public float ambient_sound_pitch_max = 1.2F;

    public float startup_sound_volume = 1.0F;
    public float startup_sound_pitch = 1.2F;

    public static SolarBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SolarBowConfig.class, SolarBowConfig::new);
    }

    public static SolarBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SolarBowConfig.class, SolarBowConfig::new);
    }
}