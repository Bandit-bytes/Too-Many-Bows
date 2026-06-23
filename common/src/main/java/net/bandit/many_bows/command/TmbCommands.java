package net.bandit.many_bows.command;

import com.mojang.brigadier.CommandDispatcher;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class TmbCommands {

    private TmbCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext buildContext,
                                Commands.CommandSelection selection) {
        dispatcher.register(
                Commands.literal("tmb")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("reload")
                                .executes(context -> reload(context.getSource())))
        );
    }

    private static int reload(CommandSourceStack source) {
        ManyBowsConfigHolder.reload();

        source.sendSuccess(
                () -> Component.literal("Reloaded Too Many Bows config."),
                true
        );
        return 1;
    }
}
