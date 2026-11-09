package net.bandit.many_bows.config.bows;

public class WindBowConfig {

    public double direct_hit_damage = 3.0D;

    // Extra magic damage equal to the arrow's base damage on a direct hit.
    public boolean bonus_magic_damage_on_direct_hit = true;

    public boolean levitation_enabled = true;
    public int levitation_duration_ticks = 20;
    public int levitation_amplifier = 1;

    public boolean ignore_owner = true;

    public boolean trail_particles_enabled = true;
    public double trail_particle_speed_y = 0.1D;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}
