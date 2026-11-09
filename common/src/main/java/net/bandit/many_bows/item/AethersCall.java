package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AethersCallBowConfig;
import net.bandit.many_bows.entity.AethersCallArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AethersCall extends ModBowItem {

    public AethersCall(Properties properties) {
        super(properties);
    }

    private static AethersCallBowConfig config() {
        return BowJsonConfigHelper.getConfig("aethers_call", AethersCallBowConfig.class, AethersCallBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        boolean hasInfinity = hasInfinity(bowStack, player);
        ItemStack arrowStack = player.getProjectile(bowStack);

        if (arrowStack.isEmpty() && !hasInfinity) return;
        if (arrowStack.isEmpty()) arrowStack = new ItemStack(Items.ARROW);

        float power = getPowerForTime(getUseDuration(bowStack) - timeLeft);
        if (power < 0.1F) return;

        boolean infiniteNormalArrow = hasInfinity && arrowStack.is(Items.ARROW);

        if (!level.isClientSide) {
            AethersCallBowConfig config = config();
            AbstractArrow arrow;

            if (arrowStack.getItem() instanceof ArrowItem arrowItem
                    && (arrowStack.is(Items.SPECTRAL_ARROW) || arrowStack.is(Items.TIPPED_ARROW))) {
                arrow = arrowItem.createArrow(level, arrowStack, player);
            } else {
                arrow = new AethersCallArrow(level, player);
            }

            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    power * config.arrow_velocity, config.arrow_inaccuracy);

            if (power == 1.0F) {
                arrow.setCritArrow(true);
            }

            applyStandardEnchantments(arrow, bowStack);
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);

            arrow.pickup = infiniteNormalArrow || player.getAbilities().instabuild
                    ? AbstractArrow.Pickup.CREATIVE_ONLY
                    : (arrow instanceof AethersCallArrow && !config.allow_pickup
                    ? AbstractArrow.Pickup.DISALLOWED
                    : AbstractArrow.Pickup.ALLOWED);

            damageBow(bowStack, player, player.getUsedItemHand());
            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F + (power * 0.2F));

        if (!infiniteNormalArrow && !player.getAbilities().instabuild) {
            arrowStack.shrink(1);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeItem(arrowStack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.aether_bow.tooltip.extended").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.many_bows.aether_bow.tooltip").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
