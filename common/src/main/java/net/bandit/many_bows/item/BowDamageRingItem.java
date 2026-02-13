package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BowDamageRingItem extends Item {

    public enum Tier {
        SHARPSHOT("sharpshot", 0.15D),
        STORMBOUND("stormbound", 0.30D);

        private final String name;
        private final double bonus;

        Tier(String name, double bonus) {
            this.name = name;
            this.bonus = bonus;
        }

        public String getName() {
            return name;
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.bow_damage_ring." + tier.getName() + ".line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.many_bows.bow_damage_ring." + tier.getName() + ".line2")
                .withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.empty());

        int percent = (int) Math.round(getBonus() * 100.0);
        tooltip.add(Component.translatable("item.many_bows.bow_damage_ring.effect", percent)
                .withStyle(ChatFormatting.BLUE));

        tooltip.add(Component.translatable("item.many_bows.bow_damage_ring.equip_hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}