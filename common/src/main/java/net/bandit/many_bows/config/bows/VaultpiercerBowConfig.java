package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class VaultpiercerBowConfig {

    // direct hit
    public double direct_hit_damage = 7.5D;
    public transient boolean use_ranged_damage_attribute_for_direct_hit = true;
    public transient String ranged_damage_attribute_namespace = "ranged_weapon";
    public transient String ranged_damage_attribute_path = "damage";
    public transient double direct_damage_attribute_divisor = 2.35D;

    // projectile
    public float projectile_velocity = 2.8F;
    public transient int max_lifetime_ticks = 120;
    public transient boolean allow_pickup = false;
    public transient boolean discard_after_entity_hit = true;

    // mark / setup
    public transient boolean apply_mark_on_hit = true;
    public transient int mark_glowing_duration_ticks = 60;
    public transient int mark_slowness_duration_ticks = 25;
    public transient int mark_slowness_amplifier = 1;

    // portal strike
    public transient boolean portal_strike_enabled = true;
    public int base_portal_count = 3;
    public int bonus_portal_count_at_full_charge = 2;
    public transient float full_charge_threshold = 0.90F;

    public transient int portal_warmup_ticks = 10;
    public transient int portal_stagger_ticks = 6;
    public transient int portal_lifetime_ticks = 50;

    public transient boolean portal_tracks_target = true;
    public transient double portal_height = 3.0D;
    public transient double portal_radius = 1.6D;
    public transient double portal_vertical_bob = 0.16D;

    // follow-up projectile damage
    public double follow_up_damage_base = 4.5D;
    public transient boolean use_ranged_damage_attribute_for_follow_up_damage = true;
    public transient double follow_up_damage_attribute_divisor = 4.0D;
    public transient boolean follow_up_damage_scales_with_power_multiplier = true;

    public transient float follow_up_projectile_velocity = 2.35F;
    public transient boolean follow_up_no_gravity = true;

    // follow-up homing
    public transient boolean follow_up_homing_enabled = true;
    public transient int follow_up_homing_ticks = 12;
    public double follow_up_homing_strength = 0.28D;

    // retarget
    public transient boolean retarget_if_target_dies = true;
    public double retarget_radius = 10.0D;

    // follow-up impact burst
    public transient boolean follow_up_impact_burst_enabled = true;
    public double follow_up_impact_burst_radius = 2.5D;
    public double follow_up_impact_burst_damage_base = 2.5D;
    public transient boolean use_ranged_damage_attribute_for_burst_damage = true;
    public transient double follow_up_impact_burst_damage_attribute_divisor = 6.0D;
    public transient boolean follow_up_impact_burst_scales_with_power_multiplier = true;
    public transient boolean burst_affects_primary_target = false;
    public transient boolean burst_affects_owner = false;

    // sounds
    public transient boolean impact_sound_enabled = true;
    public transient float impact_sound_volume = 0.5F;
    public transient float impact_sound_pitch = 0.8F;

    public transient boolean portal_open_sound_enabled = true;
    public transient float portal_open_sound_volume = 0.3F;
    public transient float portal_open_sound_pitch = 0.7F;

    public transient boolean portal_fire_sound_enabled = true;
    public transient float portal_fire_sound_volume = 0.3F;
    public transient float portal_fire_sound_pitch = 0.5F;

    // visuals
    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_lifespan_ticks = 40;
    public transient int trail_steps = 3;
    public transient double trail_spacing = 0.18D;
    public transient double trail_position_randomness = 0.05D;
    public transient double trail_velocity_scale = 0.01D;
    public transient double trail_velocity_randomness = 0.01D;
    public transient double trail_stationary_speed_threshold = 0.08D;
    public transient boolean spawn_stationary_trail_particle = false;

    public transient boolean impact_particles_enabled = true;
    public transient int impact_particle_count = 20;
    public transient double impact_particle_offset_x = 0.25D;
    public transient double impact_particle_offset_y = 0.25D;
    public transient double impact_particle_offset_z = 0.25D;
    public transient double impact_particle_speed = 0.03D;
    public transient double impact_particle_base_y_offset = 0.15D;

    public transient boolean portal_particles_enabled = true;
    public transient int portal_particle_count = 8;

    public static final String FILE_NAME = "vaultpiercer";

    public static VaultpiercerBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, VaultpiercerBowConfig.class, VaultpiercerBowConfig::new);
    }

    public static VaultpiercerBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, VaultpiercerBowConfig.class, VaultpiercerBowConfig::new);
    }
}
