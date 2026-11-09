package net.bandit.many_bows.config.bows;

public class DuskReaperBowConfig {

    public double direct_hit_damage = 8.0D;
    // Normal arrow hit (scales with arrow speed, like a vanilla arrow).
    public boolean apply_direct_arrow_damage = true;

    public boolean bonus_magic_damage_enabled = true;
    // Set to -1.0 to use the arrow's base damage (after Power, crits and accessories).
    public double bonus_magic_damage_base = -1.0D;
    public boolean use_ranged_damage_attribute_for_bonus_magic_damage = false;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_multiplier = 2.0D;
    public boolean bonus_magic_damage_scales_with_power_multiplier = false;

    public boolean ignore_owner = true;
    public boolean discard_if_owner_hit = true;

    public boolean apply_slowness = true;
    public int slowness_duration_ticks = 60;
    public int slowness_amplifier = 1;

    public boolean apply_weakness = true;
    public int weakness_duration_ticks = 60;
    public int weakness_amplifier = 1;

    public boolean apply_glowing = true;
    public int glowing_duration_ticks = 200;
    public int glowing_amplifier = 0;

    // Instant Damage applied every tick for the duration ("constant damage" in the tooltip).
    public boolean apply_harm_effect = true;
    public int harm_duration_ticks = 100;
    public int harm_amplifier = 0;

    public boolean apply_marked_tag = true;
    public String marked_tag = "manybows:marked_for_death";
    public int marked_tag_duration_ticks = 200;

    public boolean mark_sound_enabled = true;
    public float mark_sound_volume = 0.8F;
    public boolean mark_particles_enabled = true;

    public boolean floating_label_enabled = true;
    // A translation key, or plain text to display as written.
    public String floating_label_text = "item.many_bows.dusk_reaper.marked_label";
    public int floating_label_duration_ticks = 200;
    public double floating_label_y_offset = 0.25D;
    public boolean floating_label_use_marker = true;

    public boolean death_explosion_enabled = true;
    public double death_explosion_radius = 4.0D;
    public double death_explosion_damage = 8.0D;
    public boolean death_explosion_affects_owner = false;
    public boolean death_explosion_affects_allies = false;
    public boolean death_explosion_affects_tamed_animals = false;
    public boolean death_explosion_particles_enabled = true;
    public boolean death_explosion_sound_enabled = true;
    public float death_explosion_sound_volume = 1.0F;
    public float death_explosion_sound_pitch = 0.7F;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.5F;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}
