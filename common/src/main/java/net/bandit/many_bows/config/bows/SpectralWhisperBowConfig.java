package net.bandit.many_bows.config.bows;

import net.bandit.many_bows.config.BowJsonConfigHelper;

public class SpectralWhisperBowConfig {

    public static final String FILE_NAME = "spectral_whisper";

    // Core
    public double base_damage = 7.0D;
    public int max_lifetime_ticks = 140;
    public boolean allow_pickup = true;

    // Block phasing
    public int phase_through_block_layers = 1;
    public double phase_nudge_multiplier = 0.5D;
    public boolean stop_when_out_of_phases = true;
    public boolean discard_when_out_of_phases = false;

    public static SpectralWhisperBowConfig get() {
        return BowJsonConfigHelper.getConfig(FILE_NAME, SpectralWhisperBowConfig.class, SpectralWhisperBowConfig::new);
    }

    public static SpectralWhisperBowConfig reload() {
        return BowJsonConfigHelper.reloadConfig(FILE_NAME, SpectralWhisperBowConfig.class, SpectralWhisperBowConfig::new);
    }
}