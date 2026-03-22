package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class LightningBowConfig {

    public static final String FILE_NAME = "arc_heavens";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;

    // Lightning behavior
    public boolean spawn_lightning_on_entity_hit = true;
    public boolean spawn_lightning_on_block_hit = true;

    // Trail
    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 10;

    public static LightningBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, LightningBowConfig.class, LightningBowConfig::new);
    }

    public static LightningBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, LightningBowConfig.class, LightningBowConfig::new);
    }
}