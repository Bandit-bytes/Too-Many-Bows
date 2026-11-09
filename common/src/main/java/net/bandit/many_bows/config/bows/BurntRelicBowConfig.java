package net.bandit.many_bows.config.bows;

public class BurntRelicBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double base_damage = 2.0D;

    // When enabled and available, damage = attribute value / divisor + flat bonus.
    public boolean use_ranged_damage_attribute = false;
    public double ranged_damage_attribute_divisor = 2.5D;
    public double ranged_damage_flat_bonus = 1.5D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;
}
