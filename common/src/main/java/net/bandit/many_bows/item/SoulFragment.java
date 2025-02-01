package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulFragment extends Item {
    public SoulFragment(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.soulfragment.tooltip.info").withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
