package net.bandit.many_bows.item;


import net.bandit.many_bows.entity.SentinelArrow;
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

public class SentinelsWrathBow extends BowItem {
    private static final float DAMAGE_MULTIPLIER = 2.0f;

    public SentinelsWrathBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Ensure the bow is properly charged
            if (power >= 0.1F) {
                boolean creative = player.getAbilities().instabuild;
                boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = findArrowInInventory(player);

                // Check if the player has arrows or is in creative mode or has Infinity enchantment
                if (!arrowStack.isEmpty() || creative || hasInfinity) {
                    SentinelArrow sentinelArrow = new SentinelArrow(level, player);
                    sentinelArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                    // Apply enchantments to the arrow
                    applyEnchantments(stack, sentinelArrow);

                    // Extra damage against raid mobs
                    if (sentinelArrow.getOwner() instanceof LivingEntity owner && isRaidMob(owner)) {
                        sentinelArrow.setBaseDamage(sentinelArrow.getBaseDamage() * DAMAGE_MULTIPLIER);
                    }

                    // Only consume an arrow if the player is not in creative mode and doesn't have Infinity enchantment
                    if (!creative && !hasInfinity) {
                        arrowStack.shrink(1);
                    }

                    // Spawn the arrow and play the sound
                    level.addFreshEntity(sentinelArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Damage the bow on use
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private void applyEnchantments(ItemStack stack, SentinelArrow sentinelArrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            sentinelArrow.setBaseDamage(sentinelArrow.getBaseDamage() + (powerLevel * 0.5) + 1.0);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            sentinelArrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            sentinelArrow.setSecondsOnFire(100);
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

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.monster.Pillager ||
                entity instanceof net.minecraft.world.entity.monster.Vindicator ||
                entity instanceof net.minecraft.world.entity.monster.Evoker ||
                entity instanceof net.minecraft.world.entity.monster.Ravager ||
                entity instanceof net.minecraft.world.entity.monster.Illusioner;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Default tooltip that always shows
        tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.tooltip").withStyle(ChatFormatting.GOLD));

        // Check if the player is holding the Shift key
        if (Screen.hasShiftDown()) {
            // List of mobs the bow is strong against
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.shift_title").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob1").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob2").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob3").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob4").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob5").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.mob6").withStyle(ChatFormatting.GREEN));
        } else {
            // Message prompting the player to hold Shift
            tooltip.add(Component.translatable("item.too_many_bows.sentinels_wrath.hold_shift").withStyle(ChatFormatting.GRAY));
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
}
