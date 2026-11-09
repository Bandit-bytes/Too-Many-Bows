package net.bandit.many_bows.config.bows;

/** Cyroheart's piercing Icicle Javelin. */
public class IcicleJavelinBowConfig {

    public double direct_hit_damage = 3.0D;
    public int pierce_level = 3;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;
    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 40;

    public boolean trail_particles_enabled = true;
    public int trail_particle_steps = 5;
    public double trail_speed_factor = 0.1D;
    public double trail_random_offset_scale = 0.3D;

    public boolean apply_primary_target_slowness = true;
    public int primary_target_slowness_duration_ticks = 75;
    public int primary_target_slowness_amplifier = 5;

    public boolean aoe_slowness_enabled = true;
    public double aoe_radius = 4.0D;
    public boolean aoe_affects_owner = false;
    public boolean aoe_affects_primary_target = false;
    public int aoe_slowness_duration_ticks = 75;
    public int aoe_slowness_amplifier = 5;

    public boolean frost_explosion_particles_enabled = true;
    public int snowflake_burst_particle_count = 100;
    public int cloud_burst_particle_count = 30;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.8F;

    public boolean allow_pickup = true;
}
