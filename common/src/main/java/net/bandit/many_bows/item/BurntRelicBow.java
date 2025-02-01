package net.bandit.many_bows.item;

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
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class BurntRelicBow extends BowItem {

    public BurntRelicBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            ItemStack arrowStack = player.getProjectile(bowStack);
            boolean hasArrows = !arrowStack.isEmpty();
            boolean isCreative = player.getAbilities().instabuild;

            if (hasArrows || isCreative) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                if (power >= 0.1F) {
                    if (level instanceof ServerLevel serverLevel) {
                        AbstractArrow arrow;
                        if (arrowStack.is(Items.SPECTRAL_ARROW) || arrowStack.is(Items.TIPPED_ARROW)) {
                            arrow = ((ArrowItem) arrowStack.getItem()).createArrow(serverLevel, arrowStack, player, bowStack);
                        } else {
                            arrow = ((ArrowItem) Items.ARROW).createArrow(serverLevel, new ItemStack(Items.ARROW), player, bowStack);
                        }

                        arrow.setBaseDamage(arrow.getBaseDamage() + 3.0);
                        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.5F, 1.0F);
                        serverLevel.addFreshEntity(arrow);
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!isCreative) {
                        arrowStack.shrink(0);
                    }
                    if (!isCreative) {
                        bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true;
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
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!player.hasInfiniteMaterials() && !hasArrows) {
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
            tooltipComponents.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip1").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            tooltipComponents.add(Component.translatable("item.too_many_bows.burnt_relic.tooltip2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
