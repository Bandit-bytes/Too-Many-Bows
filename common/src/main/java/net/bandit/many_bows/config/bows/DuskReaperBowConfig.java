package net.bandit.many_bows.config.bows;

public class DuskReaperBowConfig {

    public double direct_hit_damage = 8.0D;
    public boolean apply_direct_arrow_damage = true;

    public boolean bonus_magic_damage_enabled = true;
    public double bonus_magic_damage_base = 8.0D;
    public boolean use_ranged_damage_attribute_for_bonus_magic_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_multiplier = 2.0D;
    public boolean bonus_magic_damage_scales_with_power_multiplier = true;

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

    public boolean apply_marked_tag = true;
    public String marked_tag = "manybows:marked_for_death";
    public int marked_tag_duration_ticks = 60;

    public boolean floating_label_enabled = true;
    public String floating_label_text = "Marked for Death";
    public int floating_label_duration_ticks = 60;
    public double floating_label_y_offset = 0.25D;
    public boolean floating_label_use_marker = true;

    public boolean impact_sound_enabled = true;
    public float impact_sound_volume = 1.0F;
    public float impact_sound_pitch = 0.5F;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = false;
}