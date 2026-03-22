package net.bandit.many_bows.config.bows;

public class BeaconBeamBowConfig {

    public double direct_hit_damage = 7.0D;

    public boolean link_damage_enabled = true;
    public double link_radius = 6.0D;
    public int max_links = 4;
    public double link_damage_multiplier = 0.45D;
    public double minimum_link_damage = 1.0D;

    public boolean trail_particles_enabled = true;
    public int base_trail_samples = 6;
    public int max_extra_trail_samples = 6;
    public double extra_samples_distance_divisor = 0.75D;
    public double trail_spread = 0.01D;

    public float hit_sound_volume = 1.0F;
    public float hit_sound_pitch = 1.35F;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}