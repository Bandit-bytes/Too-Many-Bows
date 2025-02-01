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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack, entity) - timeCharged;
            float power = getPowerForTime(charge);

            // Retrieve arrow stack
            ItemStack arrowStack = player.getProjectile(stack);

            if (power >= 0.1F && !arrowStack.isEmpty()) {
                fireTwinArrows(level, player, stack, arrowStack);

                // Damage the bow
                stack.hurtAndBreak(1, player,(EquipmentSlot.MAINHAND));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private void fireTwinArrows(Level level, Player player, ItemStack bowStack, ItemStack arrowStack) {
        Vec3 arrowDirection = player.getLookAngle();

        ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);

        // Fire the first (Light) arrow
        AbstractArrow lightArrow = arrowItem.createArrow(level, bowStack, player, arrowStack);
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, 1.0F);
        lightArrow.setBaseDamage(6.0);
        lightArrow.addTag("light");
        lightArrow.setCustomName(Component.literal(ChatFormatting.WHITE + "Light Arrow"));
        lightArrow.pickup = AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(lightArrow);

        // Fire the second (Dark) arrow
        AbstractArrow darkArrow = arrowItem.createArrow(level, bowStack, player, arrowStack);
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 5.0F, 0.0F, 3.5F, 1.0F);
        darkArrow.setBaseDamage(8.0);
        darkArrow.addTag("dark");
        darkArrow.setCustomName(Component.literal(ChatFormatting.DARK_GRAY + "Dark Arrow"));
        darkArrow.pickup = AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(darkArrow);

        // Play shooting sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        // Consume only 1 arrow from inventory
        if (!player.getAbilities().instabuild) {
            arrowStack.shrink(1);
        }
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
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
