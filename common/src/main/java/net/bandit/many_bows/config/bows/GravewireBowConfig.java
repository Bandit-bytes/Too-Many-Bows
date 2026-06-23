package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class GravewireBowConfig {

    // direct hit
    public double direct_hit_damage = 6.5D;
    public transient boolean use_ranged_damage_attribute_for_direct_hit = true;
    public transient String ranged_damage_attribute_namespace = "ranged_weapon";
    public transient String ranged_damage_attribute_path = "damage";
    public transient double direct_damage_attribute_divisor = 2.5D;

    // dual shot
    public int arrows_per_shot = 2;
    public float arrow_spread_degrees = 4.0F;
    public transient boolean require_full_ammo_for_volley = true;
    public transient boolean ignore_hurt_iframes = true;

    // primary target curse
    public transient boolean mark_primary_target = true;
    public transient int primary_glowing_duration_ticks = 100;
    public transient int primary_weakness_duration_ticks = 80;
    public transient int primary_weakness_amplifier = 0;

    // chain lash
    public transient boolean chain_enabled = true;
    public int chain_targets = 2;
    public double chain_radius = 5.0D;
    public double chain_damage_base = 2.75D;
    public transient boolean use_ranged_damage_attribute_for_chain_damage = true;
    public transient double chain_damage_attribute_divisor = 4.0D;
    public transient boolean chain_damage_scales_with_power_multiplier = true;
    public transient boolean chain_affects_owner = false;
    public transient boolean chain_affects_primary_target = false;

    // sound
    public transient boolean impact_sound_enabled = true;
    public transient float impact_sound_volume = 0.9F;
    public transient float impact_sound_pitch = 0.8F;

    // particles
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
    public transient int impact_particle_count = 16;
    public transient double impact_particle_offset_x = 0.20D;
    public transient double impact_particle_offset_y = 0.20D;
    public transient double impact_particle_offset_z = 0.20D;
    public transient double impact_particle_speed = 0.02D;
    public transient double impact_particle_base_y_offset = 0.10D;

    // misc
    public transient boolean allow_pickup = false;
    public transient boolean discard_after_entity_hit = true;
    public transient int max_lifetime_ticks = 100;

    // chain visuals
    public transient boolean chain_visuals_enabled = true;
    public transient int chain_particle_segments = 14;
    public transient int chain_particles_per_segment = 1;
    public transient double chain_visual_randomness = 0.03D;
    public transient double chain_visual_y_offset = 0.35D;
    public transient boolean chain_visual_double_strand = true;
    public transient double chain_visual_strand_offset = 0.06D;

    // grave mark visual
    public transient boolean grave_mark_entity_enabled = true;
    public transient int grave_mark_lifetime_ticks = 50;
    public transient double grave_mark_y_offset = 2.65D;
    public transient float grave_mark_scale = 0.85F;

    // soul rip
    public transient boolean soul_rip_enabled = true;
    public transient int soul_rip_particle_count = 16;
    public transient double soul_rip_height = 1.15D;
    public transient double soul_rip_speed = 0.02D;

    // grave mist / burial feel
    public transient boolean grave_mist_enabled = true;
    public transient int grave_mist_particle_count = 14;
    public transient double grave_mist_y_offset = 0.05D;
    public transient double grave_mist_radius = 0.25D;

    // grave bloom on kill
    public boolean grave_bloom_on_kill = true;
    public double grave_bloom_radius = 3.5D;
    public double grave_bloom_damage_base = 3.0D;
    public transient boolean use_ranged_damage_attribute_for_grave_bloom_damage = true;
    public transient double grave_bloom_damage_attribute_divisor = 5.5D;
    public transient boolean grave_bloom_damage_scales_with_power_multiplier = true;
    public transient boolean grave_bloom_affects_owner = false;
    public transient boolean grave_bloom_affects_primary_target = false;

    // bloom visuals
    public transient int grave_bloom_particle_count = 26;
    public transient double grave_bloom_particle_speed = 0.03D;
    public transient double grave_mark_rise_amount = 1.45D;

    public static final String FILE_NAME = "gravewire_bow";

    public static GravewireBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, GravewireBowConfig.class, GravewireBowConfig::new);
    }

    public static GravewireBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, GravewireBowConfig.class, GravewireBowConfig::new);
    }
}
