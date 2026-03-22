package net.bandit.many_bows.config.bows;

public class WebstringBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean apply_slowness_on_hit = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 1;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}