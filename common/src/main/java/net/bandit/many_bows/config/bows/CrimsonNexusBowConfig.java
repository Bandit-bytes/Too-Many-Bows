package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class CrimsonNexusBowConfig {
    public static final String FILE_NAME = "crimson_nexus";

    public double base_damage = 3.0D;
    public float projectile_velocity = 3.0F;
    public float health_cost = 2.0F;
    public int life_drain_duration_ticks = 60;
    public int life_drain_interval_ticks = 20;
    public double life_drain_radius = 10.0D;
    public float life_drain_damage = 1.0F;
    public float heal_per_target = 0.25F;
    public double crit_bonus_multiplier = 1.5D;

    public static CrimsonNexusBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, CrimsonNexusBowConfig.class, CrimsonNexusBowConfig::new);
    }

    public static CrimsonNexusBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, CrimsonNexusBowConfig.class, CrimsonNexusBowConfig::new);
    }
}
