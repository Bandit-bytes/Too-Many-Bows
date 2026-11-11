package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.TwinShadowsBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
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
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            List<ItemStack> projectiles = draw(bowStack, player.getProjectile(bowStack), player);

            if (!projectiles.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge, entity);

                if (power >= 0.1F) {
                    if (level instanceof ServerLevel serverLevel) {
                        fireTwinArrows(serverLevel, player, bowStack, projectiles, power);
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    private void fireTwinArrows(ServerLevel serverLevel, Player player, ItemStack bowStack, List<ItemStack> projectileStacks, float power) {
        if (projectileStacks.isEmpty()) return;

        ItemStack projectileStack = projectileStacks.get(0);
        ArrowItem arrowItem = (ArrowItem) (projectileStack.getItem() instanceof ArrowItem ? projectileStack.getItem() : Items.ARROW);

        TwinShadowsBowConfig config = config();

        //Light Arrow
        AbstractArrow lightArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        lightArrow.setBaseDamage(resolveConfiguredDamage(player, config.light_arrow_base_damage,
                config.use_ranged_damage_attribute, config.light_arrow_ranged_damage_divisor, 0.0D));
        applyBowDamageAttribute(lightArrow, player);
        tryApplyBowCrit(lightArrow, player, config.crit_damage_multiplier);
        lightArrow.setCustomName(Component.translatable("entity.too_many_bows.twin_shadows.light_arrow").withStyle(ChatFormatting.WHITE));
        lightArrow.addTag("light");
        lightArrow.pickup = AbstractArrow.Pickup.ALLOWED;
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);
        serverLevel.addFreshEntity(lightArrow);

        //Dark Arrow
        AbstractArrow darkArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        darkArrow.setBaseDamage(resolveConfiguredDamage(player, config.dark_arrow_base_damage,
                config.use_ranged_damage_attribute, config.dark_arrow_ranged_damage_divisor, 0.0D));
        applyBowDamageAttribute(darkArrow, player);
        tryApplyBowCrit(darkArrow, player, config.crit_damage_multiplier);
        darkArrow.setCustomName(Component.translatable("entity.too_many_bows.twin_shadows.dark_arrow").withStyle(ChatFormatting.DARK_GRAY));
        darkArrow.addTag("dark");
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + config.dark_arrow_yaw_offset_degrees, 0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);
        serverLevel.addFreshEntity(darkArrow);

        if (!player.getAbilities().instabuild) {
            projectileStack.shrink(1);
            if (projectileStack.isEmpty()) {
                projectileStacks.remove(0);
            }
        }
    }


    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!hasArrows) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip").withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
