package net.bandit.many_bows.registry;
import net.minecraft.resources.Identifier;
public final class RelicTooltipStyles {
    private RelicTooltipStyles() {}
    public static Identifier style(String path) {
        String design = switch (path) {
            case "eventide", "worldeater", "godsplitter" -> "mythic_animated";
            case "heart_of_the_wyrm", "wyrm_effigy" -> "mythic";
            case "vaultpiercer", "gravewire_bow", "soulhoard", "dusk_reaper", "crimson_nexus", "dragons_breath" -> "legendary_animated";
            case "blunted_edge", "dormant_celestial_bow", "ancient_sage_bow", "arc_heavens", "sentinels_wrath" -> "legendary";
            case "celestial_limb", "celestial_string", "celestial_grip", "cursed_stone", "soul_fragment", "rift_shard", "power_crystal" -> "collectible";
            case "wind_glove", "sharpshot_ring", "stormbound_signet", "fletchers_talisman", "dead_eyes_pendant" -> "cosmetic";
            case "hunter_bow", "ethereal_hunter", "verdant_viper", "verdant_vigor", "vitality_weaver", "soul_lantern" -> "pet";
            case "arcane_bow", "astral_bound", "auroras_grace", "spectral_whisper", "necro_flame_bow", "demons_grasp", "cursed_lantern", "twin_shadows" -> "epic";
            case "frostbite", "cyroheart_bow", "tidal_bow", "wind_bow", "dark_bow", "shulker_blast", "beacon_beam_bow" -> "rare";
            default -> "common";
        };
        return Identifier.fromNamespaceAndPath("too_many_bows", design);
    }
}
