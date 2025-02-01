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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ArcaneBow extends BowItem {

    public ArcaneBow(Properties properties) {
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
                    if (level instanceof ServerLevel serverLevel) {
                        fireTripleArrows(serverLevel, player, bowStack, arrowStack, power);
                    }

                    // Play sound and apply durability loss
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
    }

    private void fireTripleArrows(ServerLevel serverLevel, Player player, ItemStack bowStack, ItemStack projectileStack, float power) {
        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();
        float spreadAngle = 5.0F; // Small spread between arrows

        for (int i = -1; i <= 1; i++) { // Fires 3 arrows (-1, 0, 1)
            AbstractArrow arrow;

            // Middle arrow = Player's arrow choice
            if (i == 0) {
                if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                    arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);
                } else {
                    arrow = ((ArrowItem) Items.ARROW).createArrow(serverLevel, projectileStack, player, bowStack);
                }
            } else {
                // Side arrows are always normal arrows
                arrow = ((ArrowItem) Items.ARROW).createArrow(serverLevel, new ItemStack(Items.ARROW), player, bowStack);
            }

            // Adjust the shooting spread
            float spreadOffset = i * spreadAngle;
            arrow.shootFromRotation(player, basePitch, baseYaw + spreadOffset, 0.0F, power * 2.5F, 1.0F);

            // Handle pickup behavior
            if (player.getAbilities().instabuild || i != 0) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY; // Only main arrow is recoverable
            } else {
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            }

            serverLevel.addFreshEntity(arrow);
        }

        // Consume only 1 arrow
        if (!player.getAbilities().instabuild) {
            projectileStack.shrink(1);
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
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }

}
