package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class BurntRelicBowConfig {
    public static final String FILE_NAME = "burnt_relic";

    public double base_damage = 3.0D;
    public float projectile_velocity = 3.5F;
    public double crit_bonus_multiplier = 1.5D;

    public static BurntRelicBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, BurntRelicBowConfig.class, BurntRelicBowConfig::new);
    }

    public static BurntRelicBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, BurntRelicBowConfig.class, BurntRelicBowConfig::new);
    }
}
