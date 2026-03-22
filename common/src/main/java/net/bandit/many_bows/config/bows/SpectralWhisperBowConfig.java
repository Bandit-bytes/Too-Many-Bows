package net.bandit.many_bows.config.bows;

public class SpectralWhisperBowConfig {

    public double direct_hit_damage = 7.0D;

    // 0 or lower disables the lifespan timer.
    public int max_lifetime_ticks = 140;

    public int obstacles_to_phase_through = 1;
    public double phase_position_offset_multiplier = 0.5D;

    public boolean stop_when_out_of_phases = true;
    public boolean discard_when_out_of_phases = false;

    public double entity_hit_box_inflation = 1.0D;

    public boolean allow_pickup = true;
}