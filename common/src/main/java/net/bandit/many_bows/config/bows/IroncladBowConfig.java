package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class IroncladBowConfig {

    public static final String FILE_NAME = "ironclad";

    // Core
    public double base_damage = 6.0D;
    public int max_lifetime_ticks = 200;
    public boolean allow_pickup = true;

    // Vacuum
    public boolean activate_vacuum_on_block_hit = true;
    public boolean activate_vacuum_on_entity_hit = false;
    public int vacuum_duration_ticks = 80;
    public double vacuum_radius = 5.0D;
    public double pull_strength = 0.2D;
    public boolean affect_owner = false;
    public boolean discard_when_vacuum_ends = true;

    public static IroncladBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, IroncladBowConfig.class, IroncladBowConfig::new);
    }

    public static IroncladBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, IroncladBowConfig.class, IroncladBowConfig::new);
    }
}