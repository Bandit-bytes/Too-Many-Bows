package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class TwinShadowsBowConfig {
    public static final String FILE_NAME = "twin_shadows";

    public float fallback_ranged_damage = 6.0F;
    public float light_arrow_damage_divisor = 3.0F;
    public float dark_arrow_damage_divisor = 2.0F;
    public float dark_arrow_yaw_offset = 5.0F;
    public float projectile_velocity = 2.5F;
    public double crit_bonus_multiplier = 1.5D;

    public static TwinShadowsBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, TwinShadowsBowConfig.class, TwinShadowsBowConfig::new);
    }

    public static TwinShadowsBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, TwinShadowsBowConfig.class, TwinShadowsBowConfig::new);
    }
}
