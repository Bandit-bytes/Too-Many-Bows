package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SentinelWrathBowConfig {

    public static final String FILE_NAME = "sentinel_wrath";

    // Core
    public double base_damage = 5.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;
    public transient boolean discard_on_entity_hit = true;
    public transient boolean discard_on_block_hit = true;

    // Raid mob bonus damage
    public transient boolean use_ranged_damage_attribute_scaling_vs_raid_mobs = true;
    public float raid_mob_ranged_damage_multiplier = 1.5F;
    public float raid_mob_fallback_damage = 6.0F;
    public float final_damage_multiplier = 1.0F;

    // Impact
    public transient boolean impact_particles_enabled = true;
    public transient int impact_particle_count = 10;
    public transient float impact_sound_volume = 0.7F;
    public transient float impact_sound_pitch = 1.0F;

    public static SentinelWrathBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SentinelWrathBowConfig.class, SentinelWrathBowConfig::new);
    }

    public static SentinelWrathBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SentinelWrathBowConfig.class, SentinelWrathBowConfig::new);
    }
}