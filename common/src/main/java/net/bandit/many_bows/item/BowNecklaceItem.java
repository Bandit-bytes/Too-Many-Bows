package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BowNecklaceItem extends Item {

    public enum Tier {
        FLETCHER("fletcher"),
        DEAD_EYE("dead_eye");

        private final String name;

        Tier(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Tier tier;

    public BowNecklaceItem(Tier tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.bow_necklace." + tier.getName() + ".line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.many_bows.bow_necklace." + tier.getName() + ".line2")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());

        tooltip.add(Component.translatable("item.many_bows.bow_necklace." + tier.getName() + ".effect")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.many_bows.bow_necklace.equip_hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}