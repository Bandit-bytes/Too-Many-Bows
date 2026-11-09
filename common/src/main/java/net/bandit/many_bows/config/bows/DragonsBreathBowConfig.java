package net.bandit.many_bows.config.bows;

public class DragonsBreathBowConfig {

    public double direct_hit_damage = 5.0D;

    public int max_lifetime_ticks = 100;

    public boolean trail_particles_enabled = true;
    public int trail_particle_lifespan_ticks = 40;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 1.0F;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 20;
    public double impact_particle_spread = 1.5D;

    public boolean splash_damage_enabled = true;
    public double splash_radius = 2.0D;
    public double splash_damage_base = 4.0D;
    public boolean use_ranged_damage_attribute_for_splash_damage = false;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double splash_damage_attribute_divisor = 1.5D;
    public boolean splash_damage_affects_owner = false;

    // Lingering dragon's breath cloud that applies Instant Damage.
    public boolean damage_cloud_enabled = true;
    public int cloud_duration_ticks = 100;
    public int cloud_wait_time_ticks = 10;
    public float cloud_radius = 3.0F;
    public float cloud_radius_per_tick = -0.05F;
    public int cloud_instant_damage_amplifier = 1;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}
