package net.bandit.many_bows.config.bows;

public class WindBowConfig {

    public double direct_hit_damage = 3.0D;

    public boolean bonus_magic_damage_on_direct_hit = true;
    public double gust_radius = 4.0D;

    public boolean gust_knockback_enabled = true;
    public double gust_knockback_strength = 1.1D;
    public double gust_vertical_push = 0.25D;

    public boolean gust_damage_enabled = true;
    public double gust_damage = 1.5D;

    public boolean gust_affects_owner = false;

    public boolean owner_buff_enabled = true;
    public boolean owner_buff_requires_being_in_radius = true;
    public int owner_speed_duration_ticks = 60;
    public int owner_speed_amplifier = 1;
    public int owner_slow_fall_duration_ticks = 60;
    public int owner_slow_fall_amplifier = 0;

    public boolean gust_particles_enabled = true;
    public int gust_particle_count = 30;
    public double gust_particle_radius_multiplier = 1.5D;
    public double gust_particle_height = 1.5D;
    public double gust_particle_speed_y = 0.05D;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 1;
    public double trail_particle_speed_y = 0.05D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}