package net.bandit.many_bows.config.bows;

import java.util.ArrayList;
import java.util.List;

public class SentinelWrathBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean bonus_damage_vs_raid_mobs_enabled = true;
    public double bonus_damage_base = 6.0D;
    public boolean use_ranged_damage_attribute_for_bonus_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_multiplier = 1.5D;
    public boolean bonus_damage_scales_with_power_multiplier = true;

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