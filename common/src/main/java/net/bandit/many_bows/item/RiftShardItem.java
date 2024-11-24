package net.bandit.many_bows.item;



import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class RiftShardItem extends Item {

    public RiftShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Add custom tooltip lines
        tooltip.add(Component.translatable("item.many_bows.rift_shard.tooltip").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.many_bows.rift_shard.tooltip.use").withStyle(ChatFormatting.GRAY));
    }
}
