package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.SpectralArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpectralWhisperBow extends BowItem {

    public SpectralWhisperBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int timeCharged) {
        if (shooter instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                boolean infiniteArrows = player.getAbilities().instabuild ||
                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = findArrowInInventory(player);

                if (!arrowStack.isEmpty() || infiniteArrows) {
                    SpectralArrow spectralArrow = new SpectralArrow(level, player, stack);
                    spectralArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);


                    applyEnchantments(stack, spectralArrow);

                    if (!infiniteArrows) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }

                    level.addFreshEntity(spectralArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

                    stack.hurtAndBreak(1, player, (entity) -> entity.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void applyEnchantments(ItemStack stack, SpectralArrow arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.0);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.spectral_whisper").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.too_many_bows.spectral_whisper.tooltip").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.spectral_whisper.details").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
