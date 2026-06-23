package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SonicBoomBowConfig {

    public static final String FILE_NAME = "dark_bow";

    // Core
    public double base_damage = 20.0D;
    public int max_lifetime_ticks = 60;
    public boolean allow_pickup = false;
    public boolean use_gravity = false;

    // Damage scaling
    public boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_multiplier = 1.5F;
    public float flat_bonus_damage = 12.0F;
    public float final_damage_multiplier = 1.0F;

    // Knockback
    public float knockback_strength = 2.0F;

    // Beam visuals
    public boolean beam_particles_enabled = true;
    public double beam_length = 2.6D;
    public double beam_step = 0.35D;

    // Spiral visuals
    public boolean spiral_particles_enabled = true;
    public int spiral_particle_count = 18;
    public double spiral_start_radius = 0.25D;
    public double spiral_expansion_rate = 0.06D;

    // Sound
    public float impact_sound_volume = 0.5F;
    public float impact_sound_pitch = 1.0F;

    public static SonicBoomBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SonicBoomBowConfig.class, SonicBoomBowConfig::new);
    }

    public static SonicBoomBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SonicBoomBowConfig.class, SonicBoomBowConfig::new);
    }
}