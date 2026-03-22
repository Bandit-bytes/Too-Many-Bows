package net.bandit.many_bows.config.bows;

public class RadiantBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean explode_on_block_hit = true;
    public boolean explode_on_entity_hit = true;

    public float visual_explosion_power = 2.0F;

    public double damage_radius = 5.0D;

    public boolean explosion_damage_enabled = true;
    public double base_explosion_damage = 3.0D;
    public boolean use_ranged_damage_attribute_for_explosion_damage = true;
    public String ranged_damage_attribute_namespace = "ranged_weapon";
    public String ranged_damage_attribute_path = "damage";
    public double ranged_damage_attribute_divisor = 2.0D;

    public boolean explosion_damage_scales_with_power_multiplier = true;
    public double inverted_heal_and_harm_damage_multiplier = 2.0D;

    public boolean affect_owner = false;
    public boolean affect_allies = false;

    public boolean discard_after_explosion = true;
    public boolean allow_pickup = false;
}