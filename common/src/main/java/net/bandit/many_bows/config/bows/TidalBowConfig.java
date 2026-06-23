package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class TidalBowConfig {

    public static final String FILE_NAME = "tidal_bow";

    // Core
    public double base_damage = 5.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = true;

    // Water travel
    public float water_inertia = 1.0F;

    // Trail
    public transient boolean underwater_bubble_trail_enabled = true;
    public transient int underwater_bubble_trail_count = 5;

    // Direct hit debuff
    public transient boolean apply_direct_hit_slowness = true;
    public int direct_hit_slowness_duration_ticks = 60;
    public int direct_hit_slowness_amplifier = 2;

    // Water bind effect
    public transient boolean apply_water_bind_slowness = true;
    public int water_bind_slowness_duration_ticks = 20;
    public int water_bind_slowness_amplifier = 4;
    public transient int water_bind_particle_count = 30;
    public transient float water_bind_sound_volume = 0.5F;
    public transient float water_bind_sound_pitch = 0.8F;

    // Block splash
    public transient int block_splash_particle_count = 20;
    public transient float block_splash_sound_volume = 1.0F;
    public transient float block_splash_sound_pitch = 1.0F;

    public static TidalBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, TidalBowConfig.class, TidalBowConfig::new);
    }

    public static TidalBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, TidalBowConfig.class, TidalBowConfig::new);
    }
}