package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.HunterXPArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
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
                ItemStack arrowStack = findArrowInInventory(player);

                if (!arrowStack.isEmpty()) {
                    // Shoot HunterXPArrow instead of a normal Arrow
                    HunterXPArrow hunterXPArrow = new HunterXPArrow(level, player);
                    hunterXPArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                    // Apply enchantment effects
                    int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    if (powerLevel > 0) {
                        hunterXPArrow.setBaseDamage(hunterXPArrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
                    }

                    int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (punchLevel > 0) {
                        hunterXPArrow.setKnockback(punchLevel);
                    }

                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        hunterXPArrow.setSecondsOnFire(100);
                    }

                    hunterXPArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    hunterXPArrow.setCritArrow(true);
                    level.addFreshEntity(hunterXPArrow);

                    // Play sound and consume arrow
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    arrowStack.shrink(1);
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.emerald_sage_bow").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.too_many_bows.emerald_sage_bow.tooltip").withStyle(ChatFormatting.DARK_GREEN));
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
