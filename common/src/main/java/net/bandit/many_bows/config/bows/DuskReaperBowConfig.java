package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class DuskReaperBowConfig {

    public static final String FILE_NAME = "dusk_reaper";

    // Core
    public double base_damage = 8.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;
    public transient boolean discard_on_hit = true;

    // Damage scaling
    public transient boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_multiplier = 2.0F;
    public float final_damage_multiplier = 1.0F;

    // Debuffs
    public transient boolean apply_slowness = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 1;

    public transient boolean apply_weakness = true;
    public int weakness_duration_ticks = 60;
    public int weakness_amplifier = 1;

    public transient boolean apply_glowing = true;
    public transient int glowing_duration_ticks = 200;
    public transient int glowing_amplifier = 0;

    // Mark
    public transient boolean apply_marked_for_death_tag = true;
    public transient String marked_for_death_tag = "manybows:marked_for_death";

    // Impact
    public transient boolean impact_soul_particles_enabled = true;
    public transient int impact_soul_particle_count = 20;
    public transient float impact_sound_volume = 1.0F;
    public transient float impact_sound_pitch = 0.5F;

    public static DuskReaperBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, DuskReaperBowConfig.class, DuskReaperBowConfig::new);
    }

    public static DuskReaperBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, DuskReaperBowConfig.class, DuskReaperBowConfig::new);
    }
}