package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;

public class BowNecklaceItem extends Item {

    public enum Tier {
        FLETCHER(
                "A worn talisman wrapped in waxed cord.",
                "It doesn’t make the bow stronger—only steadier in the hands.",
                "Reduces bow durability loss."
        ),
        DEAD_EYE(
                "A cold pendant that never warms against the skin.",
                "Some say it \"finds\" the gap in armor before you do.",
                "Grants a chance for bow shots to critically strike."
        );

        private final String line1;
        private final String line2;
        private final String effectHint;

        Tier(String line1, String line2, String effectHint) {
            this.line1 = line1;
            this.line2 = line2;
            this.effectHint = effectHint;
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
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.accept(Component.literal(tier.line1).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal(tier.line2).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.empty());

        tooltip.accept(Component.literal(tier.effectHint).withStyle(ChatFormatting.BLUE));
        tooltip.accept(Component.literal("Equip in a necklace slot.").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
