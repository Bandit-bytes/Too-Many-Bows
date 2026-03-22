package net.bandit.many_bows.config.bows;

public class IcicleJavelinBowConfig {

    public double direct_hit_damage = 8.0D;

    public boolean bonus_magic_damage_enabled = true;
    public double bonus_magic_damage_base = 8.0D;
    public boolean use_ranged_damage_attribute_for_bonus_magic_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_multiplier = 1.5D;

    public boolean apply_slowness_on_hit = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 4;

    public boolean freeze_on_entity_hit = true;
    public boolean freeze_on_in_ground = true;
    public String freeze_block = "minecraft:packed_ice";
    public boolean allow_freeze_adjacent_if_impact_pos_blocked = true;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 20;
    public double impact_particle_offset_x = 0.25D;
    public double impact_particle_offset_y = 0.25D;
    public double impact_particle_offset_z = 0.25D;
    public double impact_particle_speed = 0.0D;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 5;
    public double trail_particle_offset_scale = 0.2D;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.0F;

    public boolean discard_after_entity_hit = true;
    public boolean discard_after_in_ground_freeze = true;

    public boolean allow_pickup = false;
}