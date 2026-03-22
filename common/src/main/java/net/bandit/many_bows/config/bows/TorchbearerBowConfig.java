package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class TorchbearerBowConfig {

    public static final String FILE_NAME = "torchbearer";

    // Core
    public double base_damage = 5.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = false;

    // Entity hit
    public boolean ignite_entities_on_hit = true;
    public int entity_fire_ticks = 120;

    // Torch placement
    public boolean place_torch_on_top_hit = true;
    public boolean place_wall_torch_on_side_hit = true;
    public boolean discard_after_successful_place = true;

    public static TorchbearerBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, TorchbearerBowConfig.class, TorchbearerBowConfig::new);
    }

    public static TorchbearerBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, TorchbearerBowConfig.class, TorchbearerBowConfig::new);
    }
}