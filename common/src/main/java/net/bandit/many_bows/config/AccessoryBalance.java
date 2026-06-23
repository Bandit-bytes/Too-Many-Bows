package net.bandit.many_bows.config;

/**
 * Built-in accessory balance values. These are intentionally not exposed as
 * separate config files; the public config stays focused on server-wide bow
 * and loot management.
 */
public final class AccessoryBalance {
    public static final double DEAD_EYES_CRIT_BONUS = 0.08D;
    public static final double DRAW_SPEED_GLOVE_BONUS = 0.75D;
    public static final double SHARPSHOT_RING_BONUS = 0.15D;
    public static final double STORMBOUND_SIGNET_BONUS = 0.30D;
    public static final double SOUL_LANTERN_DAMAGE_BONUS = 6.0D;
    public static final double CURSED_LANTERN_DAMAGE_BONUS = 6.0D;
    public static final int LANTERN_LIGHT_LEVEL = 12;

    private AccessoryBalance() {
    }
}
