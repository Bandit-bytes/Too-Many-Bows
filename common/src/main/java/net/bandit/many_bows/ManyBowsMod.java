package net.bandit.many_bows;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.bandit.many_bows.command.TmbCommands;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.loot.ModLootModifiers;
import net.bandit.many_bows.registry.*;

public final class ManyBowsMod {
    public static final String MOD_ID = "too_many_bows";

    private ManyBowsMod() {
    }

    public static void init() {
        // Load the one public-facing config before anything consumes it.
        ManyBowsConfigHolder.reload();

        ItemRegistry.register();
        TabRegistry.init();
        EntityRegistry.register();
        EffectRegistry.register();
        AttributesRegistry.register();
        SoundRegistry.register();

        ModLootModifiers.registerLootModifiers();
        CommandRegistrationEvent.EVENT.register(TmbCommands::register);

    }
}
