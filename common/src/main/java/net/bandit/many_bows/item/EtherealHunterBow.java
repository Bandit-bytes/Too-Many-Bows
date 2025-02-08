package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class EtherealHunterBow extends BowItem {

    private static final int HUNGER_COST = 2; // Hunger points required per shot

    public EtherealHunterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F && consumeHunger(player)) { // Check if hunger is consumed
                // Play custom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.5F);

                // Explicitly create and fire the vanilla arrow
                Arrow arrow = new Arrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                arrow.setCritArrow(charge >= 20);

                // Apply enchantments to the arrow
                applyEnchantments(stack, arrow);

                level.addFreshEntity(arrow);

                // Damage the bow after firing
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    private boolean consumeHunger(Player player) {
        int currentHunger = player.getFoodData().getFoodLevel();
        if (currentHunger >= HUNGER_COST) {
            player.getFoodData().setFoodLevel(currentHunger - HUNGER_COST); // Deduct hunger points
            return true;
        }

        // Notify player if insufficient hunger
        player.displayClientMessage(Component.translatable("item.many_bows.ethereal_hunter.no_hunger").withStyle(ChatFormatting.RED), true);
        return false;
    }

    private void applyEnchantments(ItemStack stack, net.minecraft.world.entity.projectile.Arrow arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // No arrows required
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            // Detailed information when holding Shift
            tooltip.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.info").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.hunger", HUNGER_COST).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.legend").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        } else {
            // Base message prompting to hold Shift
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
