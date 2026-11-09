package net.bandit.many_bows.config.bows;

public class CrimsonNexusBowConfig {

    // Arrow base damage. Set to -1.0 to keep the fired arrow's own damage.
    public double base_damage = -1.0D;

    // When enabled and available, damage = attribute value / divisor.
    public boolean use_ranged_damage_attribute = false;
    public double ranged_damage_attribute_divisor = 2.5D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;

    public float health_cost = 2.0F;
    // The health cost is skipped at or below this much health.
    public float minimum_health_to_pay_cost = 4.0F;

    // Shots fired at full health are critical and set targets on fire for this many seconds.
    public int full_health_fire_seconds = 200;

    // Life drain while the bow is held and the player is not at full health.
    public boolean life_drain_enabled = true;
    // -1 = always active while held; otherwise only for this many ticks after a shot.
    public int life_drain_window_ticks = -1;
    public int life_drain_interval_ticks = 20;
    public double life_drain_horizontal_radius = 8.0D;
    public double life_drain_vertical_radius = 4.0D;
    public int life_drain_max_targets = 1;
    public boolean life_drain_ignores_players = true;
    public double life_drain_base_damage = 2.0D;
    // Used when use_ranged_damage_attribute is enabled: drain damage = attribute value / divisor.
    public double life_drain_ranged_damage_divisor = 4.0D;
    public float life_drain_heal_per_target = 1.0F;
    public boolean life_drain_damages_bow = true;
}
