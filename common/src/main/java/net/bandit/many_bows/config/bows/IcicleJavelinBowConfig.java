package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class IcicleJavelinBowConfig {

    public static final String FILE_NAME = "cyroheart_bow";

    // Core
    public double base_damage = 8.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;

    // Damage scaling
    public transient boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_multiplier = 1.5F;
    public float final_damage_multiplier = 1.0F;

    // Freeze effect
    public transient boolean apply_direct_hit_slowness = true;
    public int direct_hit_slowness_duration_ticks = 60;
    public int direct_hit_slowness_amplifier = 4;

    public boolean freeze_block_on_entity_hit = true;
    public boolean freeze_block_when_landed = true;

    // Particles / sound
    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_count = 5;

    public transient boolean impact_particles_enabled = true;
    public transient int impact_particle_count = 20;

    public transient float impact_sound_volume = 1.0F;
    public transient float impact_sound_pitch = 1.0F;

    public static IcicleJavelinBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, IcicleJavelinBowConfig.class, IcicleJavelinBowConfig::new);
    }

    public static IcicleJavelinBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, IcicleJavelinBowConfig.class, IcicleJavelinBowConfig::new);
    }
}