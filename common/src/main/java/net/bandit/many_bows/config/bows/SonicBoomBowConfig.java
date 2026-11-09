package net.bandit.many_bows.config.bows;

public class SonicBoomBowConfig {

    public boolean no_gravity = true;
    public int max_lifetime_ticks = 60;

    // Flat sonic damage on hit (Power adds to it; arrow speed does not).
    public boolean sonic_damage_enabled = true;
    public double sonic_damage_base = 20.0D;
    // When enabled and available, sonic damage = attribute value * multiplier + flat bonus.
    public boolean use_ranged_damage_attribute_for_sonic_damage = false;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_multiplier = 1.5D;
    public double ranged_damage_flat_bonus = 12.0D;

    public boolean apply_knockback = true;
    public float knockback_strength = 2.0F;
    public float knockback_per_punch_level = 0.6F;

    public boolean ignite_target_if_burning = true;
    public int ignite_seconds = 5;

    public boolean hit_sound_enabled = true;
    public float hit_sound_volume = 0.5F;
    public float hit_sound_pitch = 1.0F;

    public boolean spiral_particles_enabled = true;
    public int spiral_particle_count = 25;
    public double spiral_base_radius = 0.5D;
    public double spiral_expansion_rate = 0.15D;
    public double spiral_angle_time_scale = 0.1D;
    public double spiral_y_rise_per_tick = 0.05D;
    public double spiral_y_offset_per_particle = 0.01D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}
