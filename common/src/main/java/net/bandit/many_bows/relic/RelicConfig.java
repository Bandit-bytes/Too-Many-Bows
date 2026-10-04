package net.bandit.many_bows.relic;
import net.bandit.many_bows.config.BowJsonConfigHelper;

public final class RelicConfig {
    public float eventideDamage = 25, lanceDamage = 50, finalMaxHealthFraction = 0.18f;
    public float worldeaterDamage = 25, dragonDiveDamage = 50, dragonfireDamage = 26;
    public float godsplitterDamage = 25, headshotMultiplier = 8;
    public int abilityCooldownTicks = 80, maxDominance = 10;
    public int starfallDelayTicks = 40, dragonfireDurationTicks = 100;
    public float fragmentChance = 0.25f, bluntedEdgeChance = 0.18f;
    public boolean affectPlayers = false, protectPets = true;

    public static RelicConfig get() {
        return BowJsonConfigHelper.getConfig("forbidden_three", RelicConfig.class, RelicConfig::new);
    }
}
