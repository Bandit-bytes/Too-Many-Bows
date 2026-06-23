package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class WebstringBowConfig {

    public static final String FILE_NAME = "webstring";

    // Core
    public double base_damage = 4.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = true;
    public transient boolean discard_on_entity_hit = true;

    // Debuff
    public transient boolean apply_slowness = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 1;

    public static WebstringBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, WebstringBowConfig.class, WebstringBowConfig::new);
    }

    public static WebstringBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, WebstringBowConfig.class, WebstringBowConfig::new);
    }
}