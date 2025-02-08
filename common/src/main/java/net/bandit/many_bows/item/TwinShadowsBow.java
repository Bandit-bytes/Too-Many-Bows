package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import java.util.function.Predicate;

public class TwinShadowsBow extends BowItem {

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (livingEntity instanceof Player player) {
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack) > 0;
            ItemStack arrowStack = player.getProjectile(itemStack);

            // Ensure an arrow exists, or default to a normal arrow
            if (!arrowStack.isEmpty() || hasInfinity) {
                if (arrowStack.isEmpty()) {
                    arrowStack = new ItemStack(Items.ARROW);
                }

                int charge = this.getUseDuration(itemStack) - i;
                float power = getPowerForTime(charge);
                if (charge < 20 || power < 1.0F) {
                    return; // Prevent early firing
                }

                boolean isCreativeArrow = hasInfinity && arrowStack.is(Items.ARROW);

                if (!level.isClientSide) {
                    fireTwinArrows(level, player, hasInfinity, itemStack, arrowStack);
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                        1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

                if (!isCreativeArrow && !hasInfinity && !player.getAbilities().instabuild) {
                    arrowStack.shrink(2);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public static float getPowerForTime(int charge) {
        if (charge < 20) {
            return 0.0F;
        }

        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }


    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }
    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl = !player.getProjectile(itemStack).isEmpty();
        if (!player.getAbilities().instabuild && !bl) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
        }
    }

    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    public int getDefaultProjectileRange() {
        return 15;
    }

    private void fireTwinArrows(Level level, Player player, boolean hasInfinity, ItemStack bowStack, ItemStack arrowStack) {
        ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);

        // Fire Light Arrow
        AbstractArrow lightArrow = arrowItem.createArrow(level, arrowStack, player);
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
        lightArrow.setBaseDamage(4.0);
        lightArrow.setCustomName(Component.literal(ChatFormatting.WHITE + "Light Arrow"));
        applyEnchantments(lightArrow, bowStack);
        lightArrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(lightArrow);

        // Fire Dark Arrow
        AbstractArrow darkArrow = arrowItem.createArrow(level, arrowStack, player);
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 3.0F, 0.0F, 3.0F, 1.0F);
        darkArrow.setBaseDamage(6.0);
        darkArrow.setCustomName(Component.literal(ChatFormatting.DARK_GRAY + "Dark Arrow"));
        applyEnchantments(darkArrow, bowStack);
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED; // Dark Arrow is not pickable
        level.addFreshEntity(darkArrow);

        // Play sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
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
