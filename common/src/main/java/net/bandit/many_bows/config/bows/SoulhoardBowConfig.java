package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SoulhoardBowConfig {

    // direct hit
    public double direct_hit_damage = 9.5D;
    public boolean use_ranged_damage_attribute_for_direct_hit = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double direct_damage_attribute_divisor = 2.5D;

    // soul storage
    public int max_souls = 3;
    public int souls_gained_per_kill = 1;
    public boolean release_requires_full_charge = true;
    public float full_charge_required_power = 1.0F;

    // empowered arrow
    public double bonus_arrow_damage_per_soul = 1.5D;
    public int ignite_seconds_on_empowered_shot = 2;

    // hoarded skulls
    public double skull_damage_base = 6.0D;
    public boolean use_ranged_damage_attribute_for_skull_damage = true;
    public double skull_damage_attribute_divisor = 5.0D;
    public boolean skull_damage_scales_with_power_multiplier = true;
    public int skull_hover_ticks_on_block_hit = 8;
    public int skull_burn_seconds = 2;
    public int skull_lifetime_ticks = 55;
    public double skull_seek_radius = 14.0D;
    public double skull_speed = 0.72D;
    public double skull_steering = 0.42D;
    public boolean skulls_can_retarget = true;

    // skull chain harvest
    public int skull_max_hits = 3;
    public boolean skull_retarget_after_hit = true;
    public double skull_post_hit_speed_multiplier = 1.18D;
    public double skull_kill_speed_multiplier = 1.35D;
    public int skull_bonus_souls_on_kill = 1;


    // sounds
    public float normal_fire_sound_volume = 1.0F;
    public float normal_fire_sound_pitch = 0.9F;
    public float empowered_fire_sound_volume = 1.0F;
    public float empowered_fire_sound_pitch = 0.8F;
    public boolean play_harvest_sound = true;
    public float harvest_sound_volume = 0.75F;
    public float harvest_sound_pitch = 1.1F;

    // trail particles
    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 40;
    public int trail_steps = 3;
    public double trail_spacing = 0.18D;
    public double trail_position_randomness = 0.04D;
    public double trail_velocity_scale = 0.01D;
    public double trail_velocity_randomness = 0.01D;
    public double trail_stationary_speed_threshold = 0.08D;
    public boolean spawn_stationary_trail_particle = false;

    // impact particles
    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 18;
    public double impact_particle_offset_x = 0.18D;
    public double impact_particle_offset_y = 0.18D;
    public double impact_particle_offset_z = 0.18D;
    public double impact_particle_speed = 0.02D;
    public double impact_particle_base_y_offset = 0.10D;

    // soul lantern synergy
    public boolean soul_lantern_synergy_enabled = true;
    public int soul_lantern_max_souls = 10;
    public double soul_lantern_bonus_arrow_damage_per_soul = 9.0D;
    public double soul_lantern_skull_damage_multiplier = 6.0D;
    public double soul_lantern_flat_release_bonus_damage = 12.0D;
    public int soul_lantern_bonus_extra_skulls = 3;

    // misc
    public boolean allow_pickup = false;
    public boolean discard_after_entity_hit = true;
    public int max_lifetime_ticks = 100;

    public static final String FILE_NAME = "soulhoard_bow";

    public static SoulhoardBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SoulhoardBowConfig.class, SoulhoardBowConfig::new);
    }

    public static SoulhoardBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SoulhoardBowConfig.class, SoulhoardBowConfig::new);
    }
}
