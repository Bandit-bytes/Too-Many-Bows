package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class BeaconBeamBowConfig {

    public static final String FILE_NAME = "beacon_beam_bow";

    // Core
    public double base_damage = 7.0D;
    public transient int max_lifetime_ticks = 200;
    public transient boolean allow_pickup = false;
    public transient boolean discard_on_entity_hit = true;
    public transient boolean discard_on_block_hit = true;

    // Link damage
    public double link_radius = 6.0D;
    public int max_links = 4;
    public float link_damage_multiplier = 0.45F;
    public float minimum_link_damage = 1.0F;

    // Trail
    public transient boolean trail_particles_enabled = true;
    public transient int trail_particle_sample_count = 6;
    public transient float trail_particle_spread = 0.01F;

    // Sounds
    public transient float entity_hit_sound_volume = 1.0F;
    public transient float entity_hit_sound_pitch = 1.35F;
    public transient float block_hit_sound_volume = 0.6F;
    public transient float block_hit_sound_pitch = 1.2F;

    public static BeaconBeamBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, BeaconBeamBowConfig.class, BeaconBeamBowConfig::new);
    }

    public static BeaconBeamBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, BeaconBeamBowConfig.class, BeaconBeamBowConfig::new);
    }
}