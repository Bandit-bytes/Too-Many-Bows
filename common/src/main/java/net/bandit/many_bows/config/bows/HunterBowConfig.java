package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class HunterBowConfig {

    public static final String FILE_NAME = "hunter_bow";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;

    // Trigger rules
    public boolean require_player_owner = true;
    public boolean require_killing_blow = true;

    // Supported mobs
    public boolean affect_cows = true;
    public boolean affect_pigs = true;
    public boolean affect_sheep = true;
    public boolean affect_chickens = true;
    public boolean affect_rabbits = true;

    // Bonus drop ranges
    public int cow_beef_min = 1;
    public int cow_beef_max = 3;
    public int cow_leather_min = 0;
    public int cow_leather_max = 1;

    public int pig_pork_min = 1;
    public int pig_pork_max = 3;

    public int sheep_mutton_min = 1;
    public int sheep_mutton_max = 3;
    public boolean sheep_drop_wool_if_unsheared = true;

    public int chicken_meat_min = 1;
    public int chicken_meat_max = 1;
    public int chicken_feather_min = 0;
    public int chicken_feather_max = 2;

    public int rabbit_meat_min = 1;
    public int rabbit_meat_max = 2;
    public int rabbit_hide_min = 0;
    public int rabbit_hide_max = 1;
    public float rabbit_foot_chance = 0.10F;

    public static HunterBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, HunterBowConfig.class, HunterBowConfig::new);
    }

    public static HunterBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, HunterBowConfig.class, HunterBowConfig::new);
    }
}