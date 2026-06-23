package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class NecroFlameBowConfig {

    public static final String FILE_NAME = "necro_flame";

    // Core
    public double base_damage = 5.0D;
    public float bonus_fire_damage = 4.0F;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = true;
    public transient boolean discard_on_entity_hit = true;
    public transient boolean discard_on_block_hit = false;

    // Scaling
    public transient boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_to_direct_damage_divisor = 2.5F;

    // Fire / debuff
    public int fire_ticks = 200;
    public transient boolean remove_regeneration = true;
    public transient boolean remove_instant_health = true;
    public transient boolean apply_cursed_flame = true;
    public int cursed_flame_duration_ticks = 160;
    public int cursed_flame_amplifier = 0;

    // Trail / block impact
    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_count = 5;
    public transient int trail_particle_duration_ticks = 100;

    public transient boolean impact_soul_particles_enabled = true;
    public transient int impact_soul_particle_count = 30;

    public transient boolean impact_flame_particles_enabled = true;
    public transient int impact_flame_particle_count = 30;

    public transient float block_impact_sound_volume = 1.0F;
    public transient float block_impact_sound_pitch = 1.0F;

    public static NecroFlameBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, NecroFlameBowConfig.class, NecroFlameBowConfig::new);
    }

    public static NecroFlameBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, NecroFlameBowConfig.class, NecroFlameBowConfig::new);
    }
}