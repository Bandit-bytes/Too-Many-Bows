package net.bandit.many_bows.config.bows;

public class IroncladBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean start_vacuum_on_hit = true;
    public int vacuum_duration_ticks = 80;

    public double vacuum_radius = 5.0D;
    public double vacuum_pull_strength = 0.2D;

    public boolean vacuum_affects_owner = false;
    public boolean vacuum_affects_only_living_entities = true;

    public boolean allow_pickup = true;
}