package net.bandit.many_bows.config.bows;

/**
 * Emerald Sage arrows. The XP amount and mob blacklist live in
 * config/too_many_bows.json (emeraldSageXpAmount, emeraldSageCrimsonNexusBlacklist).
 */
public class HunterXpBowConfig {

    // Set to -1.0 to leave the arrow's base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean spawn_xp_on_living_hit = true;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}
