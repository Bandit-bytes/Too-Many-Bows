package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class ScatterBowConfig {
    public static final String FILE_NAME = "scatter_bow";

    public int pellet_count = 8;
    public float pellet_damage_fallback = 2.0F;
    public float ranged_damage_divisor = 11.0F;
    public float horizontal_spread_degrees = 20.0F;
    public float vertical_spread_degrees = 10.0F;
    public float projectile_velocity = 3.0F;
    public double crit_bonus_multiplier = 1.5D;

    public static ScatterBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, ScatterBowConfig.class, ScatterBowConfig::new);
    }

    public static ScatterBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, ScatterBowConfig.class, ScatterBowConfig::new);
    }
}
