package net.bandit.many_bows.config.bows;

import java.util.ArrayList;
import java.util.List;

public class SentinelWrathBowConfig {

    // Set to -1.0 to leave the arrow's base damage unchanged.
    public double direct_hit_damage_override = 8.0D;

    // Raid mobs take a follow-up hit of base damage * this multiplier.
    public boolean bonus_damage_vs_raid_mobs_enabled = true;
    public double raid_mob_damage_multiplier = 5.0D;

    public List<String> raid_mob_whitelist = new ArrayList<>(List.of(
            "minecraft:pillager",
            "minecraft:vindicator",
            "minecraft:evoker",
            "minecraft:ravager",
            "minecraft:illusioner",
            "minecraft:witch"
    ));

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 0.7F;
    public float impact_sound_pitch = 1.0F;

    public boolean impact_particles_enabled = true;
    public int impact_particle_count = 10;
    public double impact_particle_offset_scale = 0.2D;

    public boolean allow_pickup = true;
}
