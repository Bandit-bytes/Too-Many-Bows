package net.bandit.many_bows.config.bows;

public class NecroFlameBowConfig {

    // Set to -1.0 to leave the arrow's base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean set_target_on_fire = false;
    public int fire_ticks_on_hit = 200;

    // Extra fire damage on hit (off by default on 1.20.1).
    public boolean bonus_fire_damage_enabled = false;
    public double base_fire_damage = 4.0D;
    public boolean use_ranged_damage_attribute_for_fire_damage = false;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_divisor = 1.5D;

    public boolean remove_regeneration_on_hit = false;

    public boolean apply_cursed_flame_effect = true;
    public int cursed_flame_duration_ticks = 200;
    public int cursed_flame_amplifier = 0;
    public boolean cursed_flame_show_particles = true;
    public boolean cursed_flame_show_icon = true;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 5;
    // How long the trail keeps going after the arrow lands.
    public int max_trail_particle_duration_ticks = 100;
    public double trail_position_step_multiplier = 0.1D;
    public double trail_random_offset_scale = 0.3D;

    public boolean entity_hit_particles_enabled = true;
    public int entity_hit_particle_count = 30;
    public double entity_hit_particle_offset_x = 1.0D;
    public double entity_hit_particle_offset_y = 0.25D;
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
