package net.bandit.many_bows.config.bows;

public class TwinShadowsBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double light_arrow_base_damage = 4.0D;
    public double dark_arrow_base_damage = 6.0D;

    // When enabled and available, damage = attribute value / divisor.
    public boolean use_ranged_damage_attribute = false;
    public double light_arrow_ranged_damage_divisor = 3.0D;
    public double dark_arrow_ranged_damage_divisor = 2.0D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;
    public float dark_arrow_yaw_offset_degrees = 3.0F;
}
