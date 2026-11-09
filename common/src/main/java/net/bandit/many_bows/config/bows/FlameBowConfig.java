package net.bandit.many_bows.config.bows;

public class FlameBowConfig {

    // Set to -1.0 to leave the arrow's base damage unchanged.
    public double direct_hit_damage_override = 4.0D;

    public boolean set_primary_target_on_fire = true;
    public int primary_target_fire_seconds = 5;

    public boolean apply_primary_target_slowness = true;
    public int primary_target_slowness_duration_ticks = 200;
    public int primary_target_slowness_amplifier = 1;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;
    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 40;

    public boolean trail_particles_enabled = true;
    public int trail_steps = 5;
    public double trail_speed_factor = 0.1D;
    public double trail_random_offset_scale = 0.3D;
    public boolean trail_smoke_enabled = true;

    // Fire burst around the impact point.
    public boolean aoe_enabled = true;
    public double aoe_radius = 5.0D;
    public int aoe_fire_seconds = 5;
    public boolean aoe_affects_owner = false;
    public boolean aoe_fire_affects_primary_target = false;

    // Optional burst damage (off by default on 1.20.1).
    public boolean aoe_damage_enabled = false;
    public double aoe_damage_base = 4.0D;
    public boolean aoe_damage_affects_primary_target = false;

    public boolean explosion_particles_enabled = true;
    public int flame_burst_particle_count = 50;
    public int explosion_particle_count = 30;

    public boolean explosion_sound_enabled = true;
    public float explosion_sound_volume = 1.0F;
    public float explosion_sound_pitch = 1.2F;

    public boolean allow_pickup = true;
}
