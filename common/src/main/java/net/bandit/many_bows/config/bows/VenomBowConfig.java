package net.bandit.many_bows.config.bows;

public class VenomBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;
    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 40;

    public boolean trail_particles_enabled = true;
    public int trail_particle_steps = 5;
    public double trail_speed_factor = 0.1D;
    public double trail_random_offset_scale = 0.3D;
    public boolean trail_spore_particles_enabled = true;
    public boolean trail_glow_particle_enabled = true;

    public boolean direct_poison_enabled = true;
    public int direct_poison_duration_ticks = 200;
    public int direct_poison_amplifier = 1;

    public boolean aoe_poison_enabled = true;
    public double aoe_radius = 4.0D;
    public boolean aoe_affects_owner = false;
    public boolean aoe_affects_primary_target = false;
    public int aoe_poison_duration_ticks = 200;
    public int aoe_poison_amplifier = 1;

    public boolean explosion_particles_enabled = true;

    public int effect_particle_count = 100;
    public double effect_particle_offset_xz = 2.0D;
    public double effect_particle_offset_y = 1.0D;
    public double effect_particle_speed_y = 0.1D;

    public int sculk_soul_particle_count = 30;
    public double sculk_soul_particle_offset_xz = 2.0D;
    public double sculk_soul_particle_offset_y = 0.5D;

    public boolean explosion_sound_enabled = true;
    public float explosion_sound_volume = 1.0F;
    public float explosion_sound_pitch = 0.8F;

    public boolean allow_pickup = true;
}