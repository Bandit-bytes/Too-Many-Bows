package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class DragonsBreathBowConfig {

    public static final String FILE_NAME = "dragons_breath";

    // Core
    public double base_damage = 7.0D;
    public int max_lifetime_ticks = 100;
    public boolean allow_pickup = true;
    public boolean discard_on_hit = true;

    // Trail
    public boolean breath_puff_particles_enabled = true;
    public int breath_puff_particles_per_tick = 2;

    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 40;
    public int trail_particle_steps = 10;

    // Area damage
    public boolean use_ranged_damage_attribute_for_area_damage = true;
    public float ranged_damage_fallback = 6.0F;
    public double aoe_radius = 2.0D;
    public float aoe_damage_multiplier = 0.6666667F;

    // Cloud
    public boolean spawn_damage_cloud = true;
    public int cloud_duration_ticks = 100;
    public int cloud_wait_time_ticks = 10;
    public float cloud_radius = 3.0F;
    public float cloud_radius_per_tick = -0.05F;
    public float cloud_dot_damage_multiplier = 0.5F;

    // Impact
    public int impact_particle_count = 40;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.0F;

    public static DragonsBreathBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }

    public static DragonsBreathBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }
}