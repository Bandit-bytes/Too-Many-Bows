package net.bandit.many_bows.config.bows;

public class AstralBoundBowConfig {

    public double direct_hit_damage = 7.0D;

    // Ricochets allowed = max_ricochets - starting_ricochet_count + extra per Ricochet enchantment level.
    public int starting_ricochet_count = 0;
    public int max_ricochets = 3;
    public int extra_ricochets_per_enchantment_level = 1;

    public double ricochet_velocity_multiplier = 0.7D;
    public double ricochet_position_offset = 0.1D;

    public float ricochet_sound_volume = 1.0F;
    public float ricochet_sound_pitch = 1.0F;

    public boolean discard_when_too_slow = true;
    public double minimum_speed_sqr_before_discard = 0.01D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}
