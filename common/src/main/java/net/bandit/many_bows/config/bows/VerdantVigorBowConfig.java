package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class VerdantVigorBowConfig {
    public static final String FILE_NAME = "verdant_vigor";

    public float fallback_ranged_damage = 6.0F;
    public float arrow_damage_divisor = 2.25F;
    public int health_boost_amplifier = 1;
    public double ally_regeneration_radius = 5.0D;
    public int regeneration_interval_ticks = 40;
    public int regeneration_duration_ticks = 40;
    public int regeneration_amplifier = 0;
    public float projectile_velocity = 2.5F;
    public double crit_bonus_multiplier = 1.5D;

    public static VerdantVigorBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, VerdantVigorBowConfig.class, VerdantVigorBowConfig::new);
    }

    public static VerdantVigorBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, VerdantVigorBowConfig.class, VerdantVigorBowConfig::new);
    }
}
