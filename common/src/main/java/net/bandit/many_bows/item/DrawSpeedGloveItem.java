package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrawSpeedGloveItem extends Item {

    public DrawSpeedGloveItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable(
                "item.too_many_bows.draw_speed_glove.lore_1"
        ).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(
                "item.too_many_bows.draw_speed_glove.lore_2"
        ).withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.empty());

        tooltip.add(Component.translatable(
                "item.too_many_bows.draw_speed_glove.effect"
        ).withStyle(ChatFormatting.BLUE));

        tooltip.add(Component.translatable(
                "item.too_many_bows.equip.glove"
        ).withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
