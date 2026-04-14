package net.bandit.many_bows.config.bows;

public class VaultpiercerBowConfig {

    // direct hit
    public double direct_hit_damage = 7.5D;
    public boolean use_ranged_damage_attribute_for_direct_hit = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double direct_damage_attribute_divisor = 2.35D;

    // projectile
    public float projectile_velocity = 2.8F;
    public int max_lifetime_ticks = 120;
    public boolean allow_pickup = false;
    public boolean discard_after_entity_hit = true;

    // mark / setup
    public boolean apply_mark_on_hit = true;
    public int mark_glowing_duration_ticks = 60;
    public int mark_slowness_duration_ticks = 25;
    public int mark_slowness_amplifier = 1;

    // portal strike
    public boolean portal_strike_enabled = true;
    public int base_portal_count = 3;
    public int bonus_portal_count_at_full_charge = 2;
    public float full_charge_threshold = 0.90F;

    public int portal_warmup_ticks = 10;
    public int portal_stagger_ticks = 6;
    public int portal_lifetime_ticks = 50;

    public boolean portal_tracks_target = true;
    public double portal_height = 3.0D;
    public double portal_radius = 1.6D;
    public double portal_vertical_bob = 0.16D;

    // follow-up projectile damage
    public double follow_up_damage_base = 4.5D;
    public boolean use_ranged_damage_attribute_for_follow_up_damage = true;
    public double follow_up_damage_attribute_divisor = 4.0D;
    public boolean follow_up_damage_scales_with_power_multiplier = true;

    public float follow_up_projectile_velocity = 2.35F;
    public boolean follow_up_no_gravity = true;

    // follow-up homing
    public boolean follow_up_homing_enabled = true;
    public int follow_up_homing_ticks = 12;
    public double follow_up_homing_strength = 0.28D;

    // retarget
    public boolean retarget_if_target_dies = true;
    public double retarget_radius = 10.0D;

    // follow-up impact burst
    public boolean follow_up_impact_burst_enabled = true;
    public double follow_up_impact_burst_radius = 2.5D;
    public double follow_up_impact_burst_damage_base = 2.5D;
    public boolean use_ranged_damage_attribute_for_burst_damage = true;
    public double follow_up_impact_burst_damage_attribute_divisor = 6.0D;
    public boolean follow_up_impact_burst_scales_with_power_multiplier = true;
    public boolean burst_affects_primary_target = false;
    public boolean burst_affects_owner = false;

    // sounds
    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 0.5F;
    public float impact_sound_pitch = 0.8F;

    public boolean portal_open_sound_enabled = true;
    public float portal_open_sound_volume = 0.3F;
    public float portal_open_sound_pitch = 0.7F;

    public boolean portal_fire_sound_enabled = true;
    public float portal_fire_sound_volume = 0.3F;
    public float portal_fire_sound_pitch = 0.5F;

    // visuals
    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 40;
    public int trail_steps = 3;
    public double trail_spacing = 0.18D;
    public double trail_position_randomness = 0.05D;
    public double trail_velocity_scale = 0.01D;
    public double trail_velocity_randomness = 0.01D;
    public double trail_stationary_speed_threshold = 0.08D;
    public boolean spawn_stationary_trail_particle = false;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 20;
    public double impact_particle_offset_x = 0.25D;
    public double impact_particle_offset_y = 0.25D;
    public double impact_particle_offset_z = 0.25D;
    public double impact_particle_speed = 0.03D;
    public double impact_particle_base_y_offset = 0.15D;

    public boolean portal_particles_enabled = true;
    public int portal_particle_count = 8;
}