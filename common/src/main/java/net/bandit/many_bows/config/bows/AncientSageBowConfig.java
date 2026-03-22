package net.bandit.many_bows.config.bows;

public class AncientSageBowConfig {

    // Set to -1.0 to leave the arrow's current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean bonus_penetration_damage_enabled = true;
    public double default_armor_penetration_factor = 0.33D;

    public boolean use_ranged_damage_attribute_for_penetration = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_divisor = 14.5D;

    public double min_armor_penetration_factor = 0.0D;
    public double max_armor_penetration_factor = 1.0D;

    public boolean bonus_penetration_scales_with_power_multiplier = true;

    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 60;
    public int trail_particles_per_tick = 1;
    public double trail_particle_offset_y = 0.0D;
    public double trail_particle_velocity_x = 0.0D;
    public double trail_particle_velocity_y = 0.05D;
    public double trail_particle_velocity_z = 0.0D;

    public boolean hit_particles_enabled = true;
    public int hit_particle_count = 15;
    public double hit_particle_offset_x = 0.25D;
    public double hit_particle_offset_y = 0.25D;
    public double hit_particle_offset_z = 0.25D;
    public double hit_particle_speed = 0.1D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}