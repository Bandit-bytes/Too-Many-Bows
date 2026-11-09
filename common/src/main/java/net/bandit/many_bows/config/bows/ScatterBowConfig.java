package net.bandit.many_bows.config.bows;

public class ScatterBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double base_damage = 1.0D;

    // When enabled and available, damage per arrow = attribute value / divisor.
    public boolean use_ranged_damage_attribute = false;
    public double ranged_damage_attribute_divisor = 11.0D;

    public double crit_damage_multiplier = 1.5D;

    public int max_arrows = 8;
    public float yaw_spread_degrees = 20.0F;
    public float pitch_spread_degrees = 10.0F;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;
}
