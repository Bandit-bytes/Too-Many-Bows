package net.bandit.many_bows.config.bows;

public class ArcaneBowConfig {

    // 1.0 = fully drawn (20 ticks).
    public float minimum_pull_power = 1.0F;

    public int arrow_count = 3;
    public float spread_angle_degrees = 5.0F;

    public boolean use_ranged_damage_attribute_for_damage = false;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_divisor = 2.5D;

    public double direct_hit_damage_override = -1.0D;
    public double direct_hit_damage_multiplier = 1.0D;
    public double direct_hit_damage_bonus = 4.0D;

    public boolean apply_bow_damage_attribute = true;
    public boolean apply_bow_crit = true;
    public double bow_crit_multiplier = 1.5D;

    public float velocity_multiplier = 4.0F;
    public float inaccuracy = 1.0F;

    public boolean consume_one_arrow_per_shot = true;
    public boolean damage_bow_on_release = true;

    public boolean shoot_sound_enabled = true;
    public float shoot_sound_volume = 1.0F;
    public float shoot_sound_pitch = 1.0F;

    // allowed, creative_only, disallowed
    public String center_arrow_pickup = "allowed";
    public String side_arrow_pickup = "creative_only";
}