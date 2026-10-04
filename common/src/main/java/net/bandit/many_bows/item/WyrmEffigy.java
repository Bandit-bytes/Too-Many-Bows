package net.bandit.many_bows.item;

import net.bandit.many_bows.relic.RelicData;
import net.bandit.many_bows.relic.RelicQuests;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class WyrmEffigy extends Item {

    public WyrmEffigy(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> lines,
            TooltipFlag flag
    ) {
        lines.accept(
                Component.literal(
                        "Touch the dragon egg, then bind at the pedestal."
                ).withStyle(ChatFormatting.GRAY)
        );

        lines.accept(
                Component.literal(
                        "Hunt the perched rematch dragon below 20% health."
                ).withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(context.getPlayer() instanceof ServerPlayer player)
                || context.getLevel().dimension() != Level.END) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();

        if (context.getLevel().getBlockState(pos).is(Blocks.DRAGON_EGG)) {
            RelicData.edit(
                    context.getItemInHand(),
                    data -> data.putBoolean("EggWitness", true)
            );

            player.sendSystemMessage(
                    Component.literal(
                            "The effigy remembers the First Tyrant. "
                                    + "The egg is unharmed."
                    )
            );

            return InteractionResult.SUCCESS;
        }

        if (!RelicData.read(context.getItemInHand())
                .getBoolean("EggWitness")) {
            player.sendSystemMessage(
                    Component.literal(
                            "Touch the dragon egg with this effigy first."
                    )
            );

            return InteractionResult.FAIL;
        }

        if (!context.getLevel().getBlockState(pos).is(Blocks.DRAGON_HEAD)
                || !context.getLevel().getBlockState(pos.below())
                .is(Blocks.RESPAWN_ANCHOR)) {
            return InteractionResult.PASS;
        }

        var kill = player.level().getServer().getAdvancements().get(
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "end/kill_dragon"
                )
        );

        if (kill == null
                || !player.getAdvancements().getOrStartProgress(kill).isDone()) {
            player.sendSystemMessage(
                    Component.literal(
                            "First defeat the dragon. Then bind your rematch."
                    )
            );

            return InteractionResult.FAIL;
        }

        RelicData.edit(
                context.getItemInHand(),
                data -> data.putBoolean("HuntBound", true)
        );

        RelicQuests.award(player, "tyrant_without_crown");

        player.sendSystemMessage(
                Component.literal(
                        "Hunt bound. Respawn the dragon with End Crystals; "
                                + "strike it perched below 20% health "
                                + "with a vanilla bow."
                )
        );

        return InteractionResult.SUCCESS;
    }
}