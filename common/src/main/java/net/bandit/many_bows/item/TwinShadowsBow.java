package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TwinShadowsBow extends BowItem {

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

            if (power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                // Call the method
                fireTwinArrows(level, player, hasInfinity, stack);

                // Damage the bow
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }

}
    private void fireTwinArrows(Level level, Player player, boolean hasInfinity, ItemStack bowStack) {
        Vec3 arrowDirection = player.getLookAngle();

        // Retrieve the arrow stack once
        ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(bowStack);

        if (!arrowStack.isEmpty() || hasInfinity) {
            // Fire the first (Light) arrow
            ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
            AbstractArrow lightArrow = arrowItem.createArrow(level, arrowStack, player);
            lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.0F, 1.0F);
            lightArrow.setBaseDamage(6.0);
            lightArrow.addTag("light");
            lightArrow.setCustomName(Component.literal(ChatFormatting.WHITE + "Light Arrow"));
            applyEnchantments(lightArrow, bowStack);
            lightArrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
            level.addFreshEntity(lightArrow);

            // Fire the second (Dark) arrow
            AbstractArrow darkArrow = arrowItem.createArrow(level, arrowStack, player);
            darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 5.0F, 0.0F, 4.0F, 1.0F); // Slight yaw offset
            darkArrow.setBaseDamage(8.0);
            darkArrow.addTag("dark");
            darkArrow.setCustomName(Component.literal(ChatFormatting.DARK_GRAY + "Dark Arrow"));
            applyEnchantments(darkArrow, bowStack);
            darkArrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
            level.addFreshEntity(darkArrow);

            // Play shooting sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!hasInfinity && !arrowStack.isEmpty()) {
                arrowStack.shrink(1);
                if (arrowStack.isEmpty()) {
                    player.getInventory().removeItem(arrowStack);
                }
            }

        }
    }

    private void applyEnchantments(AbstractArrow arrow, ItemStack stack) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 1.5);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
