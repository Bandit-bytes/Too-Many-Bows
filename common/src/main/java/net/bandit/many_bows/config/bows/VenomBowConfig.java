package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class VenomBowConfig {

    public static final String FILE_NAME = "verdant_viper";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public int post_hit_linger_ticks = 40;
    public boolean allow_pickup = true;

    // Trail
    public boolean trail_particles_enabled = true;
    public int spore_trail_particle_count = 5;
    public boolean glow_trail_enabled = true;

    // Poison
    public boolean apply_direct_hit_poison = true;
    public int direct_hit_poison_duration_ticks = 200;
    public int direct_hit_poison_amplifier = 1;

    public boolean apply_aoe_poison = true;
    public double poison_burst_radius = 4.0D;
    public int aoe_poison_duration_ticks = 200;
    public int aoe_poison_amplifier = 1;

    // Burst visuals / sound
    public int witch_particle_count = 100;
    public int sculk_soul_particle_count = 30;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.8F;

    public static VenomBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, VenomBowConfig.class, VenomBowConfig::new);
    }

    public static VenomBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, VenomBowConfig.class, VenomBowConfig::new);
    }
}