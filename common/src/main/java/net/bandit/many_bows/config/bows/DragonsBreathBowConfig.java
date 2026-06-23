package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class DragonsBreathBowConfig {

    public static final String FILE_NAME = "dragons_breath";

    // Core
    public double base_damage = 7.0D;
    public transient int max_lifetime_ticks = 100;
    public transient boolean allow_pickup = true;
    public transient boolean discard_on_hit = true;

    // Trail
    public transient boolean breath_puff_particles_enabled = true;
    public transient int breath_puff_particles_per_tick = 2;

    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_lifespan_ticks = 40;
    public transient int trail_particle_steps = 10;

    // Area damage
    public transient boolean use_ranged_damage_attribute_for_area_damage = true;
    public transient float ranged_damage_fallback = 6.0F;
    public double aoe_radius = 2.0D;
    public float aoe_damage_multiplier = 0.6666667F;

    // Cloud
    public transient boolean spawn_damage_cloud = true;
    public int cloud_duration_ticks = 100;
    public transient int cloud_wait_time_ticks = 10;
    public transient float cloud_radius = 3.0F;
    public transient float cloud_radius_per_tick = -0.05F;
    public float cloud_dot_damage_multiplier = 0.5F;

    // Impact
    public transient int impact_particle_count = 40;
    public transient float impact_sound_volume = 1.0F;
    public transient float impact_sound_pitch = 1.0F;

    public static DragonsBreathBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }

    public static DragonsBreathBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }
}