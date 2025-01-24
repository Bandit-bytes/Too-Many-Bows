package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.HunterXPArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmeraldSageBow extends BowItem {

    public EmeraldSageBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                ItemStack arrowStack = player.getProjectile(stack);
                boolean infiniteArrows = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

                if (!arrowStack.isEmpty() || infiniteArrows) {
                    // Create HunterXPArrow or use a compatible arrow
                    AbstractArrow hunterXPArrow = createArrow(level, player, arrowStack, infiniteArrows);

                    hunterXPArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                    applyEnchantments(stack, hunterXPArrow);

                    level.addFreshEntity(hunterXPArrow);

                    // Play sound
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Consume arrow if not infinite
                    if (!infiniteArrows && !arrowStack.isEmpty()) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }

                    // Damage the bow
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }

    private AbstractArrow createArrow(Level level, Player player, ItemStack arrowStack, boolean infiniteArrows) {
        AbstractArrow hunterXPArrow;

        if (arrowStack.getItem() instanceof ArrowItem arrowItem) {
            hunterXPArrow = arrowItem.createArrow(level, arrowStack, player);
        } else {
            hunterXPArrow = new HunterXPArrow(level, player);
        }

        if (infiniteArrows) {
            hunterXPArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }

        return hunterXPArrow;
    }

    private void applyEnchantments(ItemStack stack, AbstractArrow arrow) {
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

        arrow.setCritArrow(true); // Always critical hit
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.emerald_sage_bow").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.emerald_sage_bow.tooltip").withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
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
}
