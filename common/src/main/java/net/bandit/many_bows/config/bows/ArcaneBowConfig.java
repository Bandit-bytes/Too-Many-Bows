package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class ArcaneBowConfig {

    public static final String FILE_NAME = "arcane_bow";

    // Firing pattern
    public int projectile_count = 3;
    public float spread_angle_degrees = 5.0F;
    public float projectile_velocity_multiplier = 2.5F;
    public float projectile_inaccuracy = 1.0F;

    // Charge / power
    public float charge_divisor = 16.0F;
    public float minimum_power_to_fire = 0.1F;
    public float max_power = 1.0F;

    // Crit / damage behavior
    public double crit_bonus_multiplier = 1.5D;

    // Pickup behavior
    public boolean center_arrow_pickup_allowed = true;
    public boolean side_arrows_creative_only = true;

    // Durability
    public boolean damage_bow_when_fired = true;

    public static ArcaneBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, ArcaneBowConfig.class, ArcaneBowConfig::new);
    }

    public static ArcaneBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, ArcaneBowConfig.class, ArcaneBowConfig::new);
    }
}