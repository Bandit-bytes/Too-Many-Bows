package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SolarBowConfig {

    public static final String FILE_NAME = "solar_bow";

    // Core
    public double base_damage = 5.0D;
    public transient int max_lifetime_ticks = 240;
    public transient boolean allow_pickup = false;

    // Tornado
    public int tornado_duration_ticks = 100;
    public double tornado_radius = 3.5D;
    public int tornado_fire_ticks = 40;

    // Tornado damage
    public transient boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_divisor = 1.5F;
    public float tornado_damage_fallback = 4.0F;
    public float final_tornado_damage_multiplier = 1.0F;

    // Target rules
    public transient boolean exclude_owner = true;
    public transient boolean exclude_allies_of_owner = true;

    // Visuals / sound
    public transient boolean spawn_air_spiral_particles = true;
    public transient boolean spawn_tornado_particles = true;
    public transient int startup_explosion_particles = 15;

    public transient int ambient_sound_interval_ticks = 20;
    public transient float ambient_sound_volume = 1.0F;
    public transient float ambient_sound_pitch_min = 0.8F;
    public transient float ambient_sound_pitch_max = 1.2F;

    public transient float startup_sound_volume = 1.0F;
    public transient float startup_sound_pitch = 1.2F;

    public static SolarBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SolarBowConfig.class, SolarBowConfig::new);
    }

    public static SolarBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SolarBowConfig.class, SolarBowConfig::new);
    }
}