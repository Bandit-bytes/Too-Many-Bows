package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class DemonsGraspBowConfig {
    public static final String FILE_NAME = "demons_grasp";

    public double base_damage = 2.0D;
    public float projectile_velocity = 3.0F;
    public float charge_divisor = 20.0F;
    public double crit_bonus_multiplier = 1.5D;

    public static DemonsGraspBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, DemonsGraspBowConfig.class, DemonsGraspBowConfig::new);
    }

    public static DemonsGraspBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, DemonsGraspBowConfig.class, DemonsGraspBowConfig::new);
    }
}
