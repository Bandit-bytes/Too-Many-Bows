package net.bandit.many_bows.config.bows;

public class TidalBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public float water_inertia = 1.0F;

    public boolean underwater_trail_particles_enabled = true;
    public int underwater_trail_particle_count = 5;
    public double underwater_trail_spacing = 0.1D;
    public double underwater_trail_offset_scale = 0.3D;

    public boolean entity_hit_slowness_enabled = true;
    public int entity_hit_slowness_duration_ticks = 60;
    public int entity_hit_slowness_amplifier = 2;

    public boolean binding_slowness_enabled = true;
    public int binding_slowness_duration_ticks = 20;
    public int binding_slowness_amplifier = 4;

    public boolean binding_particles_enabled = true;
    public int binding_particle_count = 30;
    public double binding_ring_radius = 0.5D;
    public double binding_y_step = 0.3D;

    public boolean binding_sound_enabled = true;
    public float binding_sound_volume = 0.5F;
    public float binding_sound_pitch = 0.8F;

    public boolean block_splash_particles_enabled = true;
    public int block_splash_particle_count = 20;
    public double block_splash_offset_xz = 2.0D;
    public double block_splash_offset_y = 1.0D;
    public double block_splash_speed_y = 0.1D;

    public boolean block_splash_sound_enabled = true;
    public float block_splash_sound_volume = 1.0F;
    public float block_splash_sound_pitch = 1.0F;

    public boolean allow_pickup = true;
}