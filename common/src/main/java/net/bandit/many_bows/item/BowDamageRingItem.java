package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;

public class BowDamageRingItem extends Item {

    public enum Tier {
        SHARPSHOT(0.15D),
        STORMBOUND(0.30D);

        private final double bonus;

        Tier(double bonus) {
            this.bonus = bonus;
        }

        public double bonus() {
            return bonus;
        }
    }

    private final Tier tier;

    public BowDamageRingItem(Tier tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    public double getBonus() {
        return tier.bonus();
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        switch (tier) {
            case SHARPSHOT -> {
                tooltip.accept(Component.literal("A quiet vow, etched in silver.")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.accept(Component.literal("The bowstring remembers who holds it steady.")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            case STORMBOUND -> {
                tooltip.accept(Component.literal("A signet stolen from the stormcourt.")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.accept(Component.literal("When the wind turns, so does your fate.")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltip.accept(Component.empty());

        int percent = (int) Math.round(getBonus() * 100.0);
        tooltip.accept(Component.literal("Increases bow damage by +" + percent + "%")
                .withStyle(ChatFormatting.BLUE));

        tooltip.accept(Component.literal("Equip in a ring slot.")
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
