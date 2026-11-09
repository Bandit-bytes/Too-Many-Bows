package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.DemonsGraspBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DemonsGrasp extends ModBowItem {

    public DemonsGrasp(Properties properties) {
        super(properties);
    }

    private static DemonsGraspBowConfig config() {
        return BowJsonConfigHelper.getConfig("demons_grasp", DemonsGraspBowConfig.class, DemonsGraspBowConfig::new);
    }

    @Override
    protected void customizeArrow(AbstractArrow arrow, Player player, ItemStack bowStack, float drawPower) {
        arrow.setBaseDamage(config().base_damage);
    }

    @Override
    protected float getArrowVelocity() {
        return config().arrow_velocity;
    }

    @Override
    protected float getArrowInaccuracy() {
        return config().arrow_inaccuracy;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.demon_bow.tooltip.extended").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.many_bows.demon_bow.tooltip").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
