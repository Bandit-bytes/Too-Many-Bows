package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class RadiantBowConfig {

    public static final String FILE_NAME = "radiant";

    // Core
    public double base_damage = 4.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;
    public boolean discard_on_impact = true;

    // Explosion
    public float explosion_visual_power = 2.0F;
    public double radiant_damage_radius = 5.0D;

    // Damage scaling
    public boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_divisor = 2.0F;
    public float radiant_damage_fallback = 3.0F;
    public float final_damage_multiplier = 1.0F;
    public float inverted_heal_and_harm_damage_multiplier = 2.0F;

    // Target rules
    public boolean exclude_owner = true;
    public boolean exclude_allies_of_owner = true;

    public static RadiantBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, RadiantBowConfig.class, RadiantBowConfig::new);
    }

    public static RadiantBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, RadiantBowConfig.class, RadiantBowConfig::new);
    }
}