package net.bandit.many_bows.config;

import net.bandit.many_bows.config.bows.*;

import java.util.List;

public final class BowConfigRegistry {

    private static final List<ConfigEntry<?>> ENTRIES = List.of(
            new ConfigEntry<>("aethers_call", AethersCallBowConfig.class, AethersCallBowConfig::new),
            new ConfigEntry<>("ancient_sage_bow", AncientSageBowConfig.class, AncientSageBowConfig::new),
            new ConfigEntry<>("arcane_bow", ArcaneBowConfig.class, ArcaneBowConfig::new),
            new ConfigEntry<>("astral_bound", AstralBoundBowConfig.class, AstralBoundBowConfig::new),
            new ConfigEntry<>("auroras_grace", AurorasGraceBowConfig.class, AurorasGraceBowConfig::new),
            new ConfigEntry<>("beacon_beam_bow", BeaconBeamBowConfig.class, BeaconBeamBowConfig::new),
            new ConfigEntry<>("necro_flame", NecroFlameBowConfig.class, NecroFlameBowConfig::new),
            new ConfigEntry<>("dragons_breath", DragonsBreathBowConfig.class, DragonsBreathBowConfig::new),
            new ConfigEntry<>("dusk_reaper", DuskReaperBowConfig.class, DuskReaperBowConfig::new),
            new ConfigEntry<>("ethereal_hunter", EtherealHunterBowConfig.class, EtherealHunterBowConfig::new),
            new ConfigEntry<>("flame_bow", FlameBowConfig.class, FlameBowConfig::new),
            new ConfigEntry<>("frostbite", FrostbiteBowConfig.class, FrostbiteBowConfig::new),
            new ConfigEntry<>("hunter_bow", HunterBowConfig.class, HunterBowConfig::new),
            new ConfigEntry<>("emerald_sage_bow", HunterXpBowConfig.class, HunterXpBowConfig::new),
            new ConfigEntry<>("cyroheart_bow", IcicleJavelinBowConfig.class, IcicleJavelinBowConfig::new),
            new ConfigEntry<>("ironclad", IroncladBowConfig.class, IroncladBowConfig::new),
            new ConfigEntry<>("arc_heavens", LightningBowConfig.class, LightningBowConfig::new),
            new ConfigEntry<>("radiant", RadiantBowConfig.class, RadiantBowConfig::new),
            new ConfigEntry<>("sentinel_wrath", SentinelWrathBowConfig.class, SentinelWrathBowConfig::new),
            new ConfigEntry<>("shulker_blast", ShulkerBlastBowConfig.class, ShulkerBlastBowConfig::new),
            new ConfigEntry<>("solar_bow", SolarBowConfig.class, SolarBowConfig::new),
            new ConfigEntry<>("dark_bow", SonicBoomBowConfig.class, SonicBoomBowConfig::new),
            new ConfigEntry<>("spectral_whisper", SpectralWhisperBowConfig.class, SpectralWhisperBowConfig::new),
            new ConfigEntry<>("tidal_bow", TidalBowConfig.class, TidalBowConfig::new),
            new ConfigEntry<>("torchbearer", TorchbearerBowConfig.class, TorchbearerBowConfig::new),
            new ConfigEntry<>("verdant_viper", VenomBowConfig.class, VenomBowConfig::new),
            new ConfigEntry<>("vitality_weaver", VitalityWeaverBowConfig.class, VitalityWeaverBowConfig::new),
            new ConfigEntry<>("webstring", WebstringBowConfig.class, WebstringBowConfig::new),
            new ConfigEntry<>("wind_bow", WindBowConfig.class, WindBowConfig::new)
    );

    private BowConfigRegistry() {
    }

    public static void preloadAll() {
        for (ConfigEntry<?> entry : ENTRIES) {
            preload(entry);
        }
    }

    public static int reloadAll() {
        BowJsonConfigHelper.clearCache();

        for (ConfigEntry<?> entry : ENTRIES) {
            reload(entry);
        }

        return ENTRIES.size();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void preload(ConfigEntry entry) {
        BowJsonConfigHelper.getConfig(entry.fileName(), entry.configClass(), entry.defaultSupplier());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void reload(ConfigEntry entry) {
        BowJsonConfigHelper.reloadConfig(entry.fileName(), entry.configClass(), entry.defaultSupplier());
    }
}