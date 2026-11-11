package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.TwinShadowsBowConfig;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TwinShadowsBow extends ModBowItem {

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }

    private static TwinShadowsBowConfig config() {
        return BowJsonConfigHelper.getConfig("twin_shadows", TwinShadowsBowConfig.class, TwinShadowsBowConfig::new);
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
        float power = getPowerForTime(charge, livingEntity);
        if (power < 1.0F) return;

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

    /** Twin Shadows only fires at full draw. */
    @Override
    public float getPowerForTime(int charge, LivingEntity entity) {
        return isFullyDrawn(charge, entity) ? super.getPowerForTime(charge, entity) : 0.0F;
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
        TwinShadowsBowConfig config = config();
        ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);

        AbstractArrow lightArrow = arrowItem.createArrow(level, arrowStack, player);
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, config.arrow_velocity, config.arrow_inaccuracy);
        lightArrow.setBaseDamage(resolveConfiguredDamage(player, config.light_arrow_base_damage,
                config.use_ranged_damage_attribute, config.light_arrow_ranged_damage_divisor, 0.0D));
        lightArrow.setCustomName(Component.translatable("entity.too_many_bows.twin_shadows.light_arrow").withStyle(ChatFormatting.WHITE));
        applyStandardEnchantments(lightArrow, bowStack, 1.5D);
        applyBowDamageAttribute(lightArrow, player);
        tryApplyBowCrit(lightArrow, player, config.crit_damage_multiplier);
        lightArrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(lightArrow);

        AbstractArrow darkArrow = arrowItem.createArrow(level, arrowStack, player);
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + config.dark_arrow_yaw_offset_degrees, 0.0F,
                config.arrow_velocity, config.arrow_inaccuracy);
        darkArrow.setBaseDamage(resolveConfiguredDamage(player, config.dark_arrow_base_damage,
                config.use_ranged_damage_attribute, config.dark_arrow_ranged_damage_divisor, 0.0D));
        darkArrow.setCustomName(Component.translatable("entity.too_many_bows.twin_shadows.dark_arrow").withStyle(ChatFormatting.DARK_GRAY));
        applyStandardEnchantments(darkArrow, bowStack, 1.5D);
        applyBowDamageAttribute(darkArrow, player);
        tryApplyBowCrit(darkArrow, player, config.crit_damage_multiplier);
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(darkArrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
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
