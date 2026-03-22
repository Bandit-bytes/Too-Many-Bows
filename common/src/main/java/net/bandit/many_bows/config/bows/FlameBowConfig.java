package net.bandit.many_bows.config.bows;

public class FlameBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean set_primary_target_on_fire = true;
    public int primary_target_fire_ticks = 100;

    public boolean apply_primary_target_slowness = true;
    public int primary_target_slowness_duration_ticks = 200;
    public int primary_target_slowness_amplifier = 1;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;
    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 60;

    public boolean trail_particles_enabled = true;
    public int trail_steps = 6;
    public double trail_speed_factor = 0.12D;
    public double trail_random_offset_scale = 0.35D;

    public boolean trail_flame_enabled = true;
    public boolean trail_lava_enabled = true;
    public int trail_lava_every_n_steps = 2;

    public boolean trail_smoke_enabled = true;
    public int trail_smoke_every_n_ticks = 2;
    public double trail_smoke_velocity_scale = -0.1D;

    public boolean trail_soul_fire_spiral_enabled = true;
    public int trail_soul_fire_spiral_every_n_ticks = 3;
    public double trail_soul_fire_spiral_angle_scale = 0.3D;
    public double trail_soul_fire_spiral_radius = 0.3D;

    public boolean lingering_ring_particles_enabled = true;
    public int lingering_ring_interval_ticks = 3;
    public int lingering_ring_points = 16;
    public boolean lingering_ring_flame_enabled = true;
    public boolean lingering_ring_lava_enabled = true;
    public int lingering_ring_lava_every_n_points = 2;
    public double lingering_ring_random_y_scale = 0.5D;

    public double aoe_radius = 6.0D;
    public double aoe_damage_base = 4.0D;
    public int aoe_fire_duration_ticks = 100;

    public boolean use_ranged_damage_attribute_for_aoe_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double aoe_damage_attribute_divisor = 2.5D;
    public boolean aoe_damage_scales_with_power_multiplier = true;

    public boolean aoe_affects_owner = false;
    public boolean aoe_damage_affects_primary_target = false;
    public boolean aoe_fire_affects_primary_target = false;

    public boolean aoe_apply_slowness = true;
    public int aoe_slowness_duration_ticks = 100;
    public int aoe_slowness_amplifier = 0;
    public boolean aoe_slowness_affects_primary_target = true;

    public boolean inferno_explosion_particles_enabled = true;

    public int flash_particle_count = 1;

    public int flame_burst_particle_count = 120;
    public double flame_burst_offset_xz_multiplier = 0.6D;
    public double flame_burst_offset_y_multiplier = 0.25D;
    public double flame_burst_speed = 0.02D;

    public int lava_burst_particle_count = 60;
    public double lava_burst_offset_xz_multiplier = 0.5D;
    public double lava_burst_offset_y_multiplier = 0.2D;
    public double lava_burst_speed = 0.02D;

    public int soul_fire_burst_particle_count = 50;
    public double soul_fire_burst_offset_xz_multiplier = 0.45D;
    public double soul_fire_burst_offset_y_multiplier = 0.25D;
    public double soul_fire_burst_speed = 0.01D;

    public int smoke_burst_particle_count = 80;
    public double smoke_burst_offset_xz_multiplier = 0.6D;
    public double smoke_burst_offset_y_multiplier = 0.3D;
    public double smoke_burst_speed = 0.02D;

    public int explosion_particle_count = 12;
    public double explosion_offset_xz_multiplier = 0.35D;
    public double explosion_offset_y_multiplier = 0.2D;

    public boolean inferno_spiral_particles_enabled = true;
    public int inferno_spiral_steps = 42;
    public double inferno_spiral_rotations = 3.0D;
    public double inferno_spiral_min_radius_multiplier = 0.25D;
    public double inferno_spiral_radius_growth_multiplier = 0.55D;
    public double inferno_spiral_height_multiplier = 0.55D;
    public double inferno_spiral_particle_spread = 0.02D;
    public boolean inferno_spiral_flame_enabled = true;
    public boolean inferno_spiral_soul_fire_enabled = true;
    public int inferno_spiral_soul_fire_every_n_steps = 3;

    public boolean explosion_sound_enabled = true;
    public float explosion_sound_volume = 2.0F;
    public float explosion_sound_pitch = 0.8F;

    public boolean fire_ambient_sound_enabled = true;
    public float fire_ambient_sound_volume = 1.5F;
    public float fire_ambient_sound_pitch = 1.0F;

    public boolean ignite_blocks_enabled = false;
    public boolean respect_mob_griefing_for_ignite = true;
    public double ignite_block_chance = 0.35D;
    public int ignite_search_y_min = -1;
    public int ignite_search_y_max = 2;

    public boolean block_damage_enabled = false;
    public float minimum_block_damage_explosion_power = 1.5F;
    public double block_damage_radius_multiplier = 0.35D;

    public boolean allow_pickup = true;
}