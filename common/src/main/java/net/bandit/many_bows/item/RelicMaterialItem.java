package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RelicMaterialItem extends Item {

    private final String description;

    public RelicMaterialItem(Properties properties, String description) {
        super(properties);
        this.description = description;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> lines,
            TooltipFlag flag
    ) {
        lines.add(
                Component.translatable(description)
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}