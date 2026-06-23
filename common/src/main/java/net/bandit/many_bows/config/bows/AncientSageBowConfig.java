package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class AncientSageBowConfig {

    public static final String FILE_NAME = "ancient_sage_bow";

    // Core
    public double base_damage = 7.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;

    // Ancient Sage special damage behavior
    public boolean use_ranged_damage_attribute_for_penetration = true;
    public float default_armor_penetration_factor = 0.33F;
    public float ranged_damage_to_penetration_divisor = 14.5F;
    public float min_armor_penetration_factor = 0.0F;
    public float max_armor_penetration_factor = 1.0F;
    public float final_damage_multiplier = 1.0F;

    // Visuals
    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 60;
    public int trail_particles_per_tick = 1;

    public boolean hit_particles_enabled = true;
    public int hit_particle_count = 15;

    public static AncientSageBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, AncientSageBowConfig.class, AncientSageBowConfig::new);
    }

    public static AncientSageBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, AncientSageBowConfig.class, AncientSageBowConfig::new);
    }
}