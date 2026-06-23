package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class AstralBoundBowConfig {

    public static final String FILE_NAME = "astral_bound";

    // Core
    public double base_damage = 7.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;
    public transient boolean discard_on_entity_hit = true;

    // Ricochet behavior
    public transient boolean ricochet_on_block_hit = true;
    public int max_ricochets = 6;
    public double ricochet_velocity_multiplier = 0.7D;
    public transient double min_velocity_sqr_before_discard = 0.01D;

    // Sound
    public transient float ricochet_sound_volume = 1.0F;
    public transient float ricochet_sound_pitch = 1.0F;

    public static AstralBoundBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, AstralBoundBowConfig.class, AstralBoundBowConfig::new);
    }

    public static AstralBoundBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, AstralBoundBowConfig.class, AstralBoundBowConfig::new);
    }
}