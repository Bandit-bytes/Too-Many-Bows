package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.AncientSageArrow;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.function.Predicate;


public class AncientSageBow extends BowItem {

    public AncientSageBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            ItemStack arrowStack = player.getProjectile(bowStack);

            if (!arrowStack.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                if (power >= 0.1F) {
                    List<ItemStack> projectiles = draw(bowStack, arrowStack, player);
                    boolean arrowConsumed = false;



                    if (!projectiles.isEmpty() && level instanceof ServerLevel serverLevel) {
                        for (ItemStack projectileStack : projectiles) {
                            AbstractArrow arrow;

                            // If using Tipped or Spectral Arrows, create them properly
                            if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                                arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);

                            } else {
                                // Otherwise, spawn the custom arrow
                                arrow = new AncientSageArrow(serverLevel, player, bowStack, projectileStack);
                            }

                            // Handle pickup behavior
                            if (player.getAbilities().instabuild ||  !projectileStack.is(Items.SPECTRAL_ARROW) && !projectileStack.is(Items.TIPPED_ARROW)) {
                                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY; // Don't consume arrows
                            } else {
                                arrow.pickup = AbstractArrow.Pickup.ALLOWED; // Normal pickup
                            }

                            // Fire the arrow
                            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
                            serverLevel.addFreshEntity(arrow);

                            // Consume the arrow only once per shot
                            if ( !player.getAbilities().instabuild && !arrowConsumed) {
                                projectileStack.shrink(1);
                                arrowConsumed = true;
                            }
                        }
                    }

                    // Play sound and apply durability loss
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        bowStack.hurtAndBreak(1, player,(EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
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
            tooltipComponents.add(Component.translatable("item.many_bows.ancient_sage_bow.tooltip").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.ancient_sage_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
            tooltipComponents.add(Component.translatable("item.many_bows.ancient_sage_bow.tooltip.legend").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
