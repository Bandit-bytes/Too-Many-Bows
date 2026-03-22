package net.bandit.many_bows.config.bows;

public class TorchbearerBowConfig {

    public double direct_hit_damage = 5.0D;

    public boolean ignite_entities_on_hit = true;
    public int entity_fire_ticks = 120;

    public boolean place_torch_on_block_hit = true;
    public boolean place_standing_torch_on_top_hit = true;
    public boolean place_wall_torch_on_side_hit = true;

    public boolean require_air_for_wall_torch = true;
    public boolean discard_only_when_action_succeeds = true;

    public boolean allow_pickup = true;
}