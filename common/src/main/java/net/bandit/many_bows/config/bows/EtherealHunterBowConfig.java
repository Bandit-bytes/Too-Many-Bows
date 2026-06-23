package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class EtherealHunterBowConfig {

    public static final String FILE_NAME = "ethereal_hunter";

    // Core
    public double base_damage = 5.0D;
    public transient int max_lifetime_ticks = 200;
    public transient int post_hit_linger_ticks = 40;
    public transient boolean allow_pickup = false;
    public boolean use_gravity = true;

    public static EtherealHunterBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, EtherealHunterBowConfig.class, EtherealHunterBowConfig::new);
    }

    public static EtherealHunterBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, EtherealHunterBowConfig.class, EtherealHunterBowConfig::new);
    }
}