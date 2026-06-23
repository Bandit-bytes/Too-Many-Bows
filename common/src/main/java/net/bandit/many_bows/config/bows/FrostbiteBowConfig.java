package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class FrostbiteBowConfig {

    public static final String FILE_NAME = "frostbite";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public int post_hit_linger_ticks = 40;
    public boolean allow_pickup = false;

    // Trail
    public boolean trail_particles_enabled = true;
    public int snowflake_trail_particles = 5;
    public boolean cloud_trail_enabled = true;

    // Frost burst
    public double frost_burst_radius = 4.0D;
    public boolean apply_direct_hit_slowness = true;
    public int direct_hit_slowness_duration_ticks = 75;
    public int direct_hit_slowness_amplifier = 5;

    public boolean apply_aoe_slowness = true;
    public int aoe_slowness_duration_ticks = 75;
    public int aoe_slowness_amplifier = 5;

    public int burst_snowflake_particles = 100;
    public int burst_cloud_particles = 30;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.8F;

    public static FrostbiteBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, FrostbiteBowConfig.class, FrostbiteBowConfig::new);
    }

    public static FrostbiteBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, FrostbiteBowConfig.class, FrostbiteBowConfig::new);
    }
}