package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;


public class DrawSpeedGloveItem extends Item {

    public DrawSpeedGloveItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                Consumer<Component> tooltip,
                                TooltipFlag flag) {

        super.appendHoverText(stack, context, display, tooltip, flag);

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.too_many_bows.draw_speed_glove.lore_1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.too_many_bows.draw_speed_glove.lore_2")
                    .withStyle(ChatFormatting.DARK_GRAY));

            tooltip.accept(Component.empty());

            tooltip.accept(Component.translatable("item.too_many_bows.draw_speed_glove.effect")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.translatable("item.too_many_bows.equip.glove")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.literal("Glove Trinket")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.screen != null && mc.hasShiftDown();
    }

}
