package net.bandit.many_bows.item;

import net.bandit.many_bows.client.ClientTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class CursedLanternItem extends Item {

    public CursedLanternItem(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (ClientTooltipHelper.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.too_many_bows.cursed_lantern_tooltip").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}