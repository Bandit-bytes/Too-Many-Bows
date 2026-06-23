package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class ShulkerBlastBowConfig {

    public static final String FILE_NAME = "shulker_blast";

    // Core
    public double base_damage = 6.0D;
    public transient int max_lifetime_ticks = 100;
    public transient boolean allow_pickup = false;
    public transient boolean use_gravity = false;

    // Homing
    public double homing_range = 20.0D;
    public float speed = 0.7F;
    public double homing_factor = 0.2D;
    public transient boolean ignore_tamed_animals = true;

    // Damage scaling
    public transient boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_fallback = 6.0F;
    public float final_damage_multiplier = 1.0F;

    // Levitation
    public transient boolean apply_levitation = true;
    public int levitation_duration_ticks = 40;
    public int levitation_amplifier = 1;

    // Visuals / sound
    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_count = 1;
    public transient boolean impact_particles_enabled = true;
    public transient int impact_particle_count = 10;
    public transient float impact_sound_volume = 1.0F;
    public transient float impact_sound_pitch = 1.0F;

    public static ShulkerBlastBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, ShulkerBlastBowConfig.class, ShulkerBlastBowConfig::new);
    }

    public static ShulkerBlastBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, ShulkerBlastBowConfig.class, ShulkerBlastBowConfig::new);
    }
}