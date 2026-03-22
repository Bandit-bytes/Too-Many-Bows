package net.bandit.many_bows.config.bows;

public class ShulkerBlastBowConfig {

    public boolean no_gravity = true;

    public int max_lifetime_ticks = 100;

    public boolean homing_enabled = true;
    public double homing_range = 20.0D;
    public double homing_speed = 0.7D;
    public double homing_factor = 0.2D;

    public boolean exclude_owner = true;
    public boolean exclude_armor_stands = true;
    public boolean exclude_tamed_animals = true;

    public double direct_hit_damage = 6.0D;
    public boolean use_ranged_damage_attribute_for_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public boolean damage_scales_with_power_multiplier = true;

    public boolean apply_levitation = true;
    public int levitation_duration_ticks = 40;
    public int levitation_amplifier = 1;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.0F;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 1;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 10;
    public double impact_particle_offset_xz = 0.3D;
    public double impact_particle_offset_y = 0.3D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}