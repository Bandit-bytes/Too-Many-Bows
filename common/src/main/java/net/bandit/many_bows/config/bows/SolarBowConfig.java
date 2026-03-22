package net.bandit.many_bows.config.bows;

public class SolarBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean start_tornado_on_entity_hit = true;
    public boolean start_tornado_on_block_hit = true;
    public boolean start_tornado_on_in_ground = true;

    public int tornado_duration_ticks = 100;
    public double tornado_damage_radius = 3.5D;
    public double tornado_damage_base = 4.0D;
    public boolean use_ranged_damage_attribute_for_tornado_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double tornado_damage_attribute_divisor = 1.5D;
    public boolean tornado_damage_scales_with_power_multiplier = true;

    public int tornado_fire_ticks = 40;
    public boolean tornado_affects_owner = false;
    public boolean tornado_affects_allies = false;

    public boolean startup_explosion_particles_enabled = true;
    public int startup_explosion_particle_count = 15;
    public double startup_explosion_offset_x = 0.3D;
    public double startup_explosion_offset_y = 0.1D;
    public double startup_explosion_offset_z = 0.3D;
    public double startup_explosion_speed = 0.05D;

    public boolean startup_sound_enabled = true;
    public float startup_sound_volume = 1.0F;
    public float startup_sound_pitch = 1.2F;

    public boolean pre_tornado_particles_enabled = true;
    public double pre_tornado_radius = 1.2D;
    public int pre_tornado_points = 20;
    public double pre_tornado_angle_scale = 0.3D;
    public double pre_tornado_y_offset = 0.1D;

    public boolean tornado_particles_enabled = true;
    public int tornado_height_cap = 40;
    public double tornado_height_step = 0.2D;
    public int tornado_spiral_count = 6;
    public float tornado_max_radius = 3.5F;
    public double tornado_age_angle_scale = 0.25D;
    public double tornado_vertical_angle_scale = 0.3D;
    public float tornado_progress_base_multiplier = 0.2F;
    public float tornado_progress_growth_multiplier = 0.8F;

    public int tornado_flame_particles_per_point = 2;
    public boolean tornado_lava_enabled = true;
    public int tornado_lava_every_n_height = 5;
    public int tornado_lava_every_n_spiral = 2;

    public boolean tornado_smoke_enabled = true;
    public int tornado_smoke_every_n_height = 7;
    public int tornado_smoke_every_n_spiral = 3;

    public boolean ambient_sound_enabled = true;
    public int ambient_sound_interval_ticks = 20;
    public float ambient_sound_volume = 1.0F;
    public float ambient_sound_base_pitch = 0.8F;
    public float ambient_sound_random_pitch_range = 0.4F;

    public boolean allow_pickup = false;
}