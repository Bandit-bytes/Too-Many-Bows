package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class VitalityWeaverBowConfig {

    public static final String FILE_NAME = "vitality_weaver";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = true;

    // Lifesteal
    public float lifesteal_percent = 0.5F;
    public boolean play_heal_sound = true;
    public float heal_sound_volume = 1.0F;
    public float heal_sound_pitch = 1.0F;

    public static VitalityWeaverBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, VitalityWeaverBowConfig.class, VitalityWeaverBowConfig::new);
    }

    public static VitalityWeaverBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, VitalityWeaverBowConfig.class, VitalityWeaverBowConfig::new);
    }
}