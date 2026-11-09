package net.bandit.many_bows.config.bows;

/** Vitality arrows, fired by Vitality Weaver (and by Verdant Vigor, which sets its own damage). */
public class VitalityWeaverBowConfig {

    // Vitality Weaver arrow base damage. Set to -1.0 to leave the arrow's base damage unchanged.
    public double direct_hit_damage_override = 8.0D;

    public boolean heal_shooter_on_hit = true;
    // Heal = arrow base damage * this value.
    public double heal_percent_of_damage_dealt = 0.5D;
    public boolean cap_heal_to_target_current_health = false;

    public boolean run_on_hit_callback = true;

    public boolean heal_sound_enabled = true;
    public float heal_sound_volume = 0.8F;
    public float heal_sound_pitch = 1.4F;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}
