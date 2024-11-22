package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.ShulkerBlastProjectile;
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

public class ShulkerBlastBow extends BowItem {
    private static final float DAMAGE_MULTIPLIER = 2.0f;

    public ShulkerBlastBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                boolean creative = player.getAbilities().instabuild;
                boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = findArrowInInventory(player);

                if (!arrowStack.isEmpty() || creative || hasInfinity) {
                    ShulkerBlastProjectile projectile = new ShulkerBlastProjectile(level, player);
                    projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                    // Apply enchantment
                    applyEnchantments(stack, projectile);

                    level.addFreshEntity(projectile);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Damage the bow and consume an arrow
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    if (!creative && !hasInfinity) {
                        arrowStack.shrink(1);
                    }
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    private void applyEnchantments(ItemStack stack, ShulkerBlastProjectile projectile) {
        // Base damage multiplier applied
        float baseDamage = 5.0f * DAMAGE_MULTIPLIER; // Adjust the base damage here as needed

        // Apply Power enchantment
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            baseDamage += (powerLevel * 0.5f) + 1.0f;
        }

        projectile.setBaseDamage(baseDamage);

        // Apply Punch enchantment for knockback
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            projectile.setKnockback(punchLevel);
        }

        // Apply Flame enchantment to set the projectile on fire
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            projectile.setSecondsOnFire(100);
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.tooltip").withStyle(ChatFormatting.GOLD));

        // Show extra details if Shift is held
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.shift_title").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.effect1"));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }
}
