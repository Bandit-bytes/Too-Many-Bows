package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

import java.util.ArrayList;
import java.util.List;

public class HunterXpBowConfig {

    public static final String FILE_NAME = "emerald_sage_bow";

    // Core
    public double base_damage = 4.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = true;
    public boolean discard_on_entity_hit = true;

    // XP reward
    public boolean reward_xp_on_hit = false;
    public boolean require_killing_blow = true;
    public int xp_orb_amount = 10;
    public List<String> xp_blacklist = new ArrayList<>(List.of(
            "minecraft:armor_stand"
    ));

    public static HunterXpBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, HunterXpBowConfig.class, HunterXpBowConfig::new);
    }

    public static HunterXpBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, HunterXpBowConfig.class, HunterXpBowConfig::new);
    }
}