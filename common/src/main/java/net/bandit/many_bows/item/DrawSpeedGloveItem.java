package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;


public class DrawSpeedGloveItem extends Item {

    public DrawSpeedGloveItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {

            tooltip.accept(Component.translatable(
                    "item.too_many_bows.draw_speed_glove.lore_1"
            ).withStyle(ChatFormatting.GRAY));

            tooltip.accept(Component.translatable(
                    "item.too_many_bows.draw_speed_glove.lore_2"
            ).withStyle(ChatFormatting.DARK_GRAY));

            tooltip.accept(Component.empty());

            tooltip.accept(Component.translatable(
                    "item.too_many_bows.draw_speed_glove.effect"
            ).withStyle(ChatFormatting.BLUE));

            tooltip.accept(Component.translatable(
                    "item.too_many_bows.equip.glove"
            ).withStyle(ChatFormatting.DARK_GRAY));

            super.appendHoverText(stack, context, display, tooltip, flag);
        }
    }
        private static boolean isShiftDownSafe() {
            return Minecraft.getInstance().hasShiftDown();
        }
}
