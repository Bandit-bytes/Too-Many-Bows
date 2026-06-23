package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class WindBowConfig {

    public static final String FILE_NAME = "wind_bow";

    // Core
    public double base_damage = 3.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = true;

    // Gust
    public double gust_radius = 4.0D;
    public double knockback_strength = 1.1D;
    public float gust_bonus_damage = 1.5F;

    // Owner buffs
    public boolean buff_owner_if_in_radius = true;
    public int owner_speed_duration_ticks = 60;
    public int owner_speed_amplifier = 1;
    public int owner_slow_falling_duration_ticks = 60;
    public int owner_slow_falling_amplifier = 0;

    // Particles
    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 1;

    public boolean gust_particles_enabled = true;
    public int gust_particle_count = 30;

    public static WindBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, WindBowConfig.class, WindBowConfig::new);
    }

    public static WindBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, WindBowConfig.class, WindBowConfig::new);
    }
}