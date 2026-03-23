package net.bandit.many_bows.command;

import com.mojang.brigadier.CommandDispatcher;
import net.bandit.many_bows.config.BowConfigRegistry;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.config.PlatformCompatReloadRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TmbCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext buildContext,
                                Commands.CommandSelection selection) {
        dispatcher.register(
                Commands.literal("tmb")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadAll(context.getSource()))
                                .then(Commands.literal("all")
                                        .executes(context -> reloadAll(context.getSource())))
                                .then(Commands.literal("bows")
                                        .executes(context -> reloadBows(context.getSource())))
                                .then(Commands.literal("loot")
                                        .executes(context -> reloadLoot(context.getSource())))
                                .then(Commands.literal("accessories")
                                        .executes(context -> reloadCompat(context.getSource()))))
        );
    }

    private static int reloadAll(CommandSourceStack source) {
        int bowCount = BowConfigRegistry.reloadAll();
        ManyBowsConfigHolder.reload();
        int compatCount = PlatformCompatReloadRegistry.reloadAll();

        source.sendSuccess(
                () -> Component.literal(
                        "Reloaded Too Many Bows configs. Bows: " + bowCount +
                                ", loot: 1, accessories: " + compatCount
                ),
                true
        );

        return 1;
    }

    private static int reloadBows(CommandSourceStack source) {
        int bowCount = BowConfigRegistry.reloadAll();

        source.sendSuccess(
                () -> Component.literal("Reloaded Too Many Bows bow configs: " + bowCount),
                true
        );

        return 1;
    }

    private static int reloadLoot(CommandSourceStack source) {
        ManyBowsConfigHolder.reload();

        source.sendSuccess(
                () -> Component.literal("Reloaded Too Many Bows loot config."),
                true
        );

        return 1;
    }

    private static int reloadCompat(CommandSourceStack source) {
        int compatCount = PlatformCompatReloadRegistry.reloadAll();

        source.sendSuccess(
                () -> Component.literal("Reloaded Too Many Bows accessories configs: " + compatCount),
                true
        );

        return 1;
    }
}