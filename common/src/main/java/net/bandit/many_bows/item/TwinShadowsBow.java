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
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TwinShadowsBow extends ModBowItem {

    private static final double DEFAULT_CRIT_MULTIPLIER = 1.5D;

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (!(livingEntity instanceof Player player)) return;

        ItemStack arrowStack = player.getProjectile(itemStack);
        boolean isCreative = player.getAbilities().instabuild;
        boolean hasInfinity = hasInfinity(itemStack, player);
        boolean canFireNoArrows = canFireWithoutArrows(itemStack, player);

        if (arrowStack.isEmpty() && !isCreative && !canFireNoArrows) return;

        int charge = this.getUseDuration(itemStack) - i;
        float power = getPowerForTime(charge);
        if (charge < 20 || power < 1.0F) return;

        ItemStack usedArrowStack = arrowStack;
        if (usedArrowStack.isEmpty() && canFireNoArrows) {
            usedArrowStack = new ItemStack(Items.ARROW);
        }

        if (!level.isClientSide) {
            fireTwinArrows(level, player, hasInfinity, itemStack, usedArrowStack);
            damageBow(itemStack, player, player.getUsedItemHand());
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        boolean isInfinityArrowStack = hasInfinity && usedArrowStack.is(Items.ARROW);

        if (!isCreative && !isInfinityArrowStack && !hasInfinity && !arrowStack.isEmpty()) {
            arrowStack.shrink(2);
            if (arrowStack.isEmpty()) {
                player.getInventory().removeItem(arrowStack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
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
        boolean hasProjectile = !player.getProjectile(itemStack).isEmpty();
        boolean canFireNoArrows = canFireWithoutArrows(itemStack, player);

        if (!player.getAbilities().instabuild && !hasProjectile && !canFireNoArrows) {
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

        AbstractArrow lightArrow = arrowItem.createArrow(level, arrowStack, player);
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
        lightArrow.setBaseDamage(4.0);
        lightArrow.setCustomName(Component.literal(ChatFormatting.WHITE + "Light Arrow"));
        applyEnchantments(lightArrow, bowStack);
        applyBowDamageAttribute(lightArrow, player);
        tryApplyBowCrit(lightArrow, player, DEFAULT_CRIT_MULTIPLIER);
        lightArrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(lightArrow);

        AbstractArrow darkArrow = arrowItem.createArrow(level, arrowStack, player);
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 3.0F, 0.0F, 3.0F, 1.0F);
        darkArrow.setBaseDamage(6.0);
        darkArrow.setCustomName(Component.literal(ChatFormatting.DARK_GRAY + "Dark Arrow"));
        applyEnchantments(darkArrow, bowStack);
        applyBowDamageAttribute(darkArrow, player);
        tryApplyBowCrit(darkArrow, player, DEFAULT_CRIT_MULTIPLIER);
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(darkArrow);

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
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
