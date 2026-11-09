package net.bandit.many_bows.config.bows;

import java.util.ArrayList;
import java.util.List;

public class HunterBowConfig {

    // Set to -1.0 to leave current base damage unchanged.
    public double direct_hit_damage_override = -1.0D;

    public boolean require_player_owner = true;
    public boolean require_target_dead_or_dying = true;
    public boolean use_cooked_drops_if_target_on_fire = true;

    public List<String> passive_mob_whitelist = new ArrayList<>(List.of(
            "minecraft:cow",
            "minecraft:pig",
            "minecraft:sheep",
            "minecraft:chicken",
            "minecraft:rabbit"
    ));

    public int cow_beef_min = 1;
    public int cow_beef_max = 3;
    public int cow_leather_min = 0;
    public int cow_leather_max = 1;

    public int pig_pork_min = 1;
    public int pig_pork_max = 3;

    public int sheep_mutton_min = 1;
    public int sheep_mutton_max = 3;
    public boolean sheep_drop_wool_if_unsheared = true;

    public int chicken_meat_min = 1;
    public int chicken_meat_max = 1;
    public int chicken_feather_min = 0;
    public int chicken_feather_max = 2;

    public int rabbit_meat_min = 1;
    public int rabbit_meat_max = 2;
    public int rabbit_hide_min = 0;
    public int rabbit_hide_max = 1;
    public double rabbit_foot_chance = 0.10D;

    public boolean allow_pickup = true;
}