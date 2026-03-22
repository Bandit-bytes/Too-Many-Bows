package net.bandit.many_bows.config.bows;

public class DragonsBreathBowConfig {

    public double direct_hit_damage = 7.0D;

    public int max_lifetime_ticks = 100;

    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 40;
    public boolean spawn_stationary_trail_particle = true;
    public double trail_stationary_speed_threshold = 0.01D;
    public int trail_steps = 10;
    public double trail_spacing = 0.15D;
    public double trail_position_randomness = 0.05D;
    public double trail_velocity_scale = 0.02D;
    public double trail_velocity_randomness = 0.01D;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.0F;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 40;
    public double impact_particle_offset_x = 0.4D;
    public double impact_particle_offset_y = 0.2D;
    public double impact_particle_offset_z = 0.4D;
    public double impact_particle_base_y_offset = 0.3D;
    public double impact_particle_speed = 0.1D;

    public boolean splash_damage_enabled = true;
    public double splash_radius = 2.0D;
    public double splash_damage_base = 4.0D;
    public boolean use_ranged_damage_attribute_for_splash_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double splash_damage_attribute_divisor = 1.5D;
    public boolean splash_damage_affects_owner = false;
    public boolean splash_damage_affects_primary_target = false;

    public boolean damage_cloud_enabled = true;
    public double cloud_damage_base = 6.0D;
    public boolean use_ranged_damage_attribute_for_cloud_damage = true;
    public double cloud_damage_attribute_divisor = 2.0D;
    public boolean cloud_damage_scales_with_power_multiplier = true;
    public int cloud_damage_interval_ticks = 20;
    public int cloud_duration_ticks = 100;
    public int cloud_wait_time_ticks = 10;
    public float cloud_radius = 3.0F;
    public float cloud_radius_per_tick = -0.05F;
    public boolean cloud_affects_owner = false;
    public boolean cloud_affects_primary_target = true;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}