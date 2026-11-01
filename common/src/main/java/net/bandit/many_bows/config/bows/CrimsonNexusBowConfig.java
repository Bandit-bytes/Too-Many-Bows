package net.bandit.many_bows.config.bows;

public class CrimsonNexusBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double base_damage = 5.0D;

    // When available, damage = attribute value / divisor.
    public boolean use_ranged_damage_attribute = true;
    public double ranged_damage_attribute_divisor = 2.5D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;

    public float health_cost = 2.0F;
    public float minimum_health_to_pay_cost = 4.0F;

    public int life_drain_window_ticks = 60;
    public int life_drain_interval_ticks = 20;
    public double life_drain_radius = 10.0D;
    public double life_drain_base_damage = 1.0D;
    public double life_drain_ranged_damage_divisor = 4.0D;
    public float life_drain_heal_per_target = 0.25F;
}
