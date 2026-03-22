package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class FlameBowConfig {

    public static final String FILE_NAME = "flame_bow";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public int post_hit_linger_ticks = 40;
    public boolean allow_pickup = false;

    // Trail
    public boolean trail_particles_enabled = true;
    public int flame_trail_particles = 5;
    public boolean smoke_trail_enabled = true;

    // Direct hit effects
    public int direct_hit_fire_ticks = 80;
    public boolean apply_direct_hit_slowness = true;
    public int direct_hit_slowness_duration_ticks = 200;
    public int direct_hit_slowness_amplifier = 1;

    // Fire burst
    public double fire_burst_radius = 5.0D;
    public float aoe_fire_damage_fallback = 2.0F;
    public boolean use_ranged_damage_attribute_for_aoe_damage = true;
    public float ranged_damage_to_aoe_divisor = 3.0F;
    public int aoe_fire_ticks = 80;

    public int burst_flame_particle_count = 50;
    public int burst_explosion_particle_count = 30;
    public int burst_smoke_particle_count = 30;

    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.2F;

    public static FlameBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, FlameBowConfig.class, FlameBowConfig::new);
    }

    public static FlameBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, FlameBowConfig.class, FlameBowConfig::new);
    }
}