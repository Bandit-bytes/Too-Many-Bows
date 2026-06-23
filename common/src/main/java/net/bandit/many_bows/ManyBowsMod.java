package net.bandit.many_bows;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.bandit.many_bows.command.TmbCommands;
import net.bandit.many_bows.config.BowConfigRegistry;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.config.PlatformCompatReloadRegistry;
import net.bandit.many_bows.loot.ModLootModifiers;
import net.bandit.many_bows.registry.*;

public final class ManyBowsMod {
    public static final String MOD_ID = "too_many_bows";

    private ManyBowsMod() {
    }

    public static void init() {
        ItemRegistry.register();
        TabRegistry.init();
        EntityRegistry.register();
        EffectRegistry.register();
        AttributesRegistry.register();
        SoundRegistry.register();

        ModLootModifiers.registerLootModifiers();

        // Preserve the original configuration layout: loot, every bow, and platform accessories.
        ManyBowsConfigHolder.reload();
        BowConfigRegistry.preloadAll();
        PlatformCompatReloadRegistry.preloadAll();

        CommandRegistrationEvent.EVENT.register(TmbCommands::register);
    }
}
