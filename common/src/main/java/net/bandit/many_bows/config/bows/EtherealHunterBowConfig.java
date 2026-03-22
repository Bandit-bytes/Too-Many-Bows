package net.bandit.many_bows.config.bows;

public class EtherealHunterBowConfig {

    // Set to -1.0 to leave the current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean no_gravity = false;

    public boolean start_hit_timer_on_entity_hit = true;
    public boolean start_hit_timer_on_block_hit = true;

    public boolean discard_after_hit_delay = true;
    public int hit_discard_delay_ticks = 40;

    public boolean allow_pickup = false;
}