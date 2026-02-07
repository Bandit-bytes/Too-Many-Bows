package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;


public class RepairCrystalItem extends Item {
    public RepairCrystalItem(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.many_bows.repair_crystal.tooltip").withStyle(ChatFormatting.GREEN));
            tooltip.accept(Component.translatable("item.many_bows.repair_crystal.tooltip.use").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
        private static boolean isShiftDownSafe() {
            return Minecraft.getInstance().hasShiftDown();
        }
}
