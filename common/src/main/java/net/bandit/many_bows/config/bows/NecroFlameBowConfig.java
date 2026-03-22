package net.bandit.many_bows.config.bows;

public class NecroFlameBowConfig {

    // Set to -1.0 to leave current arrow base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean set_target_on_fire = true;
    public int fire_ticks_on_hit = 200;

    public boolean bonus_fire_damage_enabled = true;
    public double base_fire_damage = 4.0D;
    public boolean use_ranged_damage_attribute_for_fire_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_divisor = 1.5D;
    public boolean bonus_fire_damage_scales_with_power_multiplier = true;

    public boolean remove_regeneration_on_hit = true;
    public boolean remove_heal_on_hit = true;

    public boolean apply_cursed_flame_effect = true;
    public int cursed_flame_duration_ticks = 160;
    public int cursed_flame_amplifier = 0;
    public boolean cursed_flame_show_particles = true;
    public boolean cursed_flame_show_icon = true;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 5;
    public int max_trail_particle_duration_ticks = 100;
    public double trail_position_step_multiplier = 0.1D;
    public double trail_random_offset_scale = 0.3D;

    public boolean entity_hit_particles_enabled = true;
    public int entity_hit_particle_count = 30;
    public double entity_hit_particle_offset_x = 1.0D;
    public double entity_hit_particle_offset_y = 0.5D;
    public double entity_hit_particle_offset_z = 1.0D;
    public double entity_hit_particle_speed = 0.1D;

    public boolean block_hit_particles_enabled = true;
    public int block_hit_particle_count = 30;
    public double block_hit_particle_offset_x = 0.5D;
    public double block_hit_particle_offset_y = 0.5D;
    public double block_hit_particle_offset_z = 0.5D;
    public double block_hit_particle_speed = 0.01D;

    public boolean block_hit_sound_enabled = true;
    public float block_hit_sound_volume = 1.0F;
    public float block_hit_sound_pitch = 1.0F;

    public boolean stop_trail_particles_after_block_hit = true;
    public boolean allow_pickup = true;
}