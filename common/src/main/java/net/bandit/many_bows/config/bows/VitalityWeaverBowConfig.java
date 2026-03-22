package net.bandit.many_bows.config.bows;

public class VitalityWeaverBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean heal_shooter_on_hit = true;
    public double heal_percent_of_damage_dealt = 0.5D;
    public boolean cap_heal_to_target_current_health = true;

    public boolean run_on_hit_callback = true;

    public boolean heal_sound_enabled = true;
    public float heal_sound_volume = 1.0F;
    public float heal_sound_pitch = 1.0F;

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}