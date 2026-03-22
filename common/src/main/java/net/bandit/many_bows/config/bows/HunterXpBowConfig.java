package net.bandit.many_bows.config.bows;

import java.util.ArrayList;
import java.util.List;

public class HunterXpBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean spawn_xp_on_living_hit = true;
    public int xp_amount = 10;

    public List<String> xp_blacklist = new ArrayList<>(List.of(
            "minecraft:armor_stand",
            "minecraft:wandering_trader"
    ));

    public boolean discard_after_entity_hit = true;
    public boolean allow_pickup = true;
}