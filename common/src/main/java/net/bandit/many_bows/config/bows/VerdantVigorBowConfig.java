package net.bandit.many_bows.config.bows;

public class VerdantVigorBowConfig {

    // Used when the ranged_weapon:damage attribute is unavailable or use_ranged_damage_attribute is false.
    public double base_damage = 8.0D;

    // When enabled and available, damage = attribute value / divisor.
    public boolean use_ranged_damage_attribute = false;
    public double ranged_damage_attribute_divisor = 2.25D;

    public double crit_damage_multiplier = 1.5D;

    public float arrow_velocity = 3.0F;
    public float arrow_inaccuracy = 1.0F;

    public boolean wither_on_hit_enabled = true;
    public int wither_on_hit_duration_ticks = 100;
    public int wither_on_hit_amplifier = 1;

    // Health Boost while the bow is held. It lingers for health_boost_duration_ticks after the bow is put away.
    public boolean health_boost_enabled = true;
    public int health_boost_amplifier = 1;
    public int health_boost_duration_ticks = 100;
    // The boost is refreshed once its remaining time drops below this.
    public int health_boost_refresh_below_ticks = 60;
    // Health restored each time the boost is refreshed while the player is hurt.
    public float health_boost_refresh_heal = 8.0F;

    // Regeneration for nearby players and non-hostile mobs while the bow is held.
    public boolean ally_regeneration_enabled = true;
    public double ally_regeneration_radius = 5.0D;
    public int ally_regeneration_interval_ticks = 40;
    public int ally_regeneration_duration_ticks = 40;
    public int ally_regeneration_amplifier = 0;
}
