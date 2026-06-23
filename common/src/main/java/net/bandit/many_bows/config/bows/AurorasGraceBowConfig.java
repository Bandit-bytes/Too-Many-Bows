package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class AurorasGraceBowConfig {

    public static final String FILE_NAME = "auroras_grace";

    // Core
    public double base_damage = 7.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;
    public transient boolean discard_on_impact = true;

    // Rift behavior
    public boolean spawn_rift_on_entity_hit = true;
    public boolean spawn_rift_on_block_hit = true;

    public static AurorasGraceBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, AurorasGraceBowConfig.class, AurorasGraceBowConfig::new);
    }

    public static AurorasGraceBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, AurorasGraceBowConfig.class, AurorasGraceBowConfig::new);
    }
}