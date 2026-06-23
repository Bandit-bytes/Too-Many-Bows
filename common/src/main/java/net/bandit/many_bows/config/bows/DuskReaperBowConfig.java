package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class DuskReaperBowConfig {

    public static final String FILE_NAME = "dusk_reaper";

    // Core
    public double base_damage = 8.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;
    public boolean discard_on_hit = true;

    // Damage scaling
    public boolean use_ranged_damage_attribute_scaling = true;
    public float ranged_damage_multiplier = 2.0F;
    public float final_damage_multiplier = 1.0F;

    // Debuffs
    public boolean apply_slowness = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 1;

    public boolean apply_weakness = true;
    public int weakness_duration_ticks = 60;
    public int weakness_amplifier = 1;

    public boolean apply_glowing = true;
    public int glowing_duration_ticks = 200;
    public int glowing_amplifier = 0;

    // Mark
    public boolean apply_marked_for_death_tag = true;
    public String marked_for_death_tag = "manybows:marked_for_death";

    // Impact
    public boolean impact_soul_particles_enabled = true;
    public int impact_soul_particle_count = 20;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.5F;

    public static DuskReaperBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, DuskReaperBowConfig.class, DuskReaperBowConfig::new);
    }

    public static DuskReaperBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, DuskReaperBowConfig.class, DuskReaperBowConfig::new);
    }
}