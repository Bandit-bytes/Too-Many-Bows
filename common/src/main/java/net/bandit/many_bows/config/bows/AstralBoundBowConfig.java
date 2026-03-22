package net.bandit.many_bows.config.bows;

public class AstralBoundBowConfig {

    public double direct_hit_damage = 7.0D;

    public int starting_ricochet_count = 3;
    public int max_ricochets = 6;

    public double ricochet_velocity_multiplier = 0.7D;
    public double ricochet_position_offset = 0.1D;

    public float ricochet_sound_volume = 1.0F;
    public float ricochet_sound_pitch = 1.0F;

    public boolean discard_when_too_slow = true;
    public double minimum_speed_sqr_before_discard = 0.01D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}