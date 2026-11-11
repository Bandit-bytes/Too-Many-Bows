package net.bandit.many_bows.relic;
import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.MigratableConfig;

public final class RelicConfig implements MigratableConfig {
    /** Bump when shipped balance defaults change; see {@link #migrate()}. */
    public static final int CURRENT_VERSION = 3;

    // Absent from pre-versioned files, so it keeps this initializer and migration runs.
    public int configVersion = 0;

    public float eventideDamage = 40, lanceDamage = 70, finalMaxHealthFraction = 0.25f, finalJudgmentCap = 200;
    public float worldeaterDamage = 40, dragonDiveDamage = 75, dragonfireDamage = 35;
    public float godsplitterDamage = 50, headshotMultiplier = 8, executeHealthFraction = 0.25f;
    public int abilityCooldownTicks = 60, maxDominance = 8;
    public int starfallDelayTicks = 40, dragonfireDurationTicks = 100;
    // Gear scaling: arrow damage gained per point of ranged_weapon:damage from gear (Ranged Weapon API).
    public boolean useRangedWeaponAttribute = true;
    public float rangedDamageScaling = 1.5f;
    // Lances, dragon dive and dragonfire grow by the same percentage gear adds to the arrow.
    public boolean scaleAbilitiesWithGear = true;
    public float fragmentChance = 0.25f, bluntedEdgeChance = 0.18f;
    public boolean affectPlayers = false, protectPets = true;

    public static RelicConfig defaults() {
        RelicConfig config = new RelicConfig();
        config.configVersion = CURRENT_VERSION;
        return config;
    }

    public static RelicConfig get() {
        return BowJsonConfigHelper.getConfig("forbidden_three", RelicConfig.class, RelicConfig::defaults);
    }

    /**
     * Moves values still at an old shipped default to the new default.
     * Values a pack/server has customised are left alone.
     */
    @Override
    public boolean migrate() {
        if (configVersion >= CURRENT_VERSION) return false;
        RelicConfig d = defaults();

        if (configVersion < 2) {
            // v2: relic bows buffed to sit clearly above every regular bow.
            if (eventideDamage == 25) eventideDamage = d.eventideDamage;
            if (lanceDamage == 50) lanceDamage = d.lanceDamage;
            if (finalMaxHealthFraction == 0.18f) finalMaxHealthFraction = d.finalMaxHealthFraction;
            if (worldeaterDamage == 25) worldeaterDamage = d.worldeaterDamage;
            if (dragonDiveDamage == 50) dragonDiveDamage = d.dragonDiveDamage;
            if (dragonfireDamage == 26) dragonfireDamage = d.dragonfireDamage;
            if (godsplitterDamage == 25) godsplitterDamage = d.godsplitterDamage;
            if (abilityCooldownTicks == 80) abilityCooldownTicks = d.abilityCooldownTicks;
            if (maxDominance == 10) maxDominance = d.maxDominance;
            // Guard the new fields against invalid hand-edited values.
            if (finalJudgmentCap <= 0) finalJudgmentCap = d.finalJudgmentCap;
            if (executeHealthFraction <= 0) executeHealthFraction = d.executeHealthFraction;
        }

        // v3: only adds gear-scaling fields (filled from initializers); rewrite so they appear in the file.

        configVersion = CURRENT_VERSION;
        return true;
    }
}
