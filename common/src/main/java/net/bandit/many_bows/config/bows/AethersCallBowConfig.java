package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class AethersCallBowConfig {

    public static final String FILE_NAME = "aethers_call";

    public double base_damage = 6.0D;
    public int max_lifetime_ticks = 80;
    public boolean allow_pickup = false;
    public boolean discard_on_impact = true;

    public boolean trail_particles_enabled = true;
    public int trail_particles_per_tick = 1;

    public double burst_radius = 4.0D;
    public boolean burst_particles_enabled = true;
    public int burst_particle_count = 20;
    public float burst_sound_volume = 1.0F;
    public float burst_sound_pitch = 1.5F;

    public boolean owner_slow_falling_enabled = true;
    public int owner_slow_falling_duration_ticks = 100;
    public int owner_slow_falling_amplifier = 0;

    public boolean target_levitation_enabled = true;
    public int target_levitation_duration_ticks = 40;
    public int target_levitation_amplifier = 0;

    public static AethersCallBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, AethersCallBowConfig.class, AethersCallBowConfig::new);
    }

    public static AethersCallBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, AethersCallBowConfig.class, AethersCallBowConfig::new);
    }
}