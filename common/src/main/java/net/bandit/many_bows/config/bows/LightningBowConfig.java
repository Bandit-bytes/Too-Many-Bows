package net.bandit.many_bows.config.bows;

public class LightningBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean summon_lightning_on_entity_hit = true;
    public boolean summon_lightning_on_in_ground = true;
    public boolean set_lightning_cause_from_owner = true;

    public boolean discard_after_lightning = true;

    public boolean trail_particles_enabled = true;
    public int trail_particle_count = 10;
    public double trail_particle_offset_xz = 0.5D;
    public double trail_particle_offset_y = 0.5D;
    public double trail_particle_velocity_y = 0.1D;

    public boolean allow_pickup = true;
}