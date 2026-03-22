package net.bandit.many_bows.config.bows;

public class AethersCallBowConfig {

    public double direct_hit_damage = 6.0D;
    public int max_lifetime_ticks = 80;

    public boolean trail_particles_enabled = true;
    public int trail_particles_per_tick = 1;
    public double trail_particle_offset_y = 0.1D;
    public double trail_particle_velocity_x = 0.0D;
    public double trail_particle_velocity_y = 0.02D;
    public double trail_particle_velocity_z = 0.0D;

    public double burst_radius = 4.0D;
    public float burst_sound_volume = 1.0F;
    public float burst_sound_pitch = 1.5F;

    public boolean burst_particles_enabled = true;
    public int burst_particle_count = 20;
    public double burst_particle_offset_x = 0.75D;
    public double burst_particle_offset_y = 0.75D;
    public double burst_particle_offset_z = 0.75D;
    public double burst_particle_speed = 0.01D;

    public boolean owner_slow_falling_enabled = true;
    public int owner_slow_falling_duration_ticks = 100;
    public int owner_slow_falling_amplifier = 0;

    public boolean target_levitation_enabled = true;
    public int target_levitation_duration_ticks = 40;
    public int target_levitation_amplifier = 0;

    public boolean allow_pickup = false;
}