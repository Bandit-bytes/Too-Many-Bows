package net.bandit.many_bows.config.bows;

public class VerdantVigorBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double base_damage = 2.0D;

    // When available, damage = attribute value / divisor.
    public boolean use_ranged_damage_attribute = true;
    public double ranged_damage_attribute_divisor = 2.25D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 2.5F;
    public float arrow_inaccuracy = 1.0F;

    public boolean health_boost_enabled = true;
    public int health_boost_amplifier = 1;

    public boolean ally_regeneration_enabled = true;
    public double ally_regeneration_radius = 5.0D;
    public int ally_regeneration_interval_ticks = 40;
    public int ally_regeneration_duration_ticks = 40;
    public int ally_regeneration_amplifier = 0;
}
