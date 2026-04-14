package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SoulLanternItem extends Item {

    public SoulLanternItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(
                    Component.translatable("item.too_many_bows.soul_lantern_tooltip")
                            .withStyle(ChatFormatting.GREEN)
            );
            tooltipComponents.add(
                    Component.translatable("item.too_many_bows.soul_lantern_tooltip.soulhoard")
                            .withStyle(ChatFormatting.AQUA)
            );
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}