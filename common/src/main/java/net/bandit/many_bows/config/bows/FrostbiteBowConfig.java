package net.bandit.many_bows.config.bows;

public class FrostbiteBowConfig {

    public double direct_hit_damage = 5.0D;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;
    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 40;

    public boolean trail_particles_enabled = true;
    public int trail_particle_steps = 5;
    public double trail_speed_factor = 0.1D;
    public double trail_random_offset_scale = 0.3D;

    public boolean trail_snowflake_enabled = true;
    public boolean trail_cloud_enabled = true;
    public int trail_cloud_particles_per_step = 1;

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
    public double snowflake_burst_offset_xz = 2.0D;
    public double snowflake_burst_offset_y = 1.0D;
    public double snowflake_burst_speed_y = 0.1D;

    public int cloud_burst_particle_count = 30;
    public double cloud_burst_offset_xz = 2.0D;
    public double cloud_burst_offset_y = 0.5D;
    public double cloud_burst_speed = 0.0D;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.8F;

    public boolean allow_pickup = true;
}