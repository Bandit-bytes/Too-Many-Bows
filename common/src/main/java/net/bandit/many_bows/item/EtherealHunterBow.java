package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class EtherealHunterBow extends BowItem {

    private static final int HUNGER_COST = 2; // Hunger points required per shot

    public EtherealHunterBow(Properties properties) {
        super(properties);
    }

    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            if (power >= 0.1F && consumeHunger(player)) {
                // Play custom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.5F);

                // Determine arrow type
                ItemStack arrowStack = player.getProjectile(bowStack);
                ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem ?
                        (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;

                AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player, bowStack);

                // Fire the arrow
                arrow.setBaseDamage(arrow.getBaseDamage() + 3.0);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                // Special effect when at full health
                if (player.getHealth() == player.getMaxHealth()) {
                    arrow.setCritArrow(true);
                }

                // Add arrow to the world
                level.addFreshEntity(arrow);

                // Bow durability loss
                bowStack.hurtAndBreak(1, player,(EquipmentSlot.MAINHAND));

                // Update player stats
                player.awardStat(Stats.ITEM_USED.get(this));
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

    private boolean consumeHunger(Player player) {
        int currentHunger = player.getFoodData().getFoodLevel();
        if (currentHunger >= HUNGER_COST) {
            player.getFoodData().setFoodLevel(currentHunger - HUNGER_COST); // Deduct hunger points
            return true;
        }

        // Notify player if insufficient hunger
        player.displayClientMessage(Component.translatable("item.many_bows.ethereal_hunter.no_hunger").withStyle(ChatFormatting.RED), true);
        return false;
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
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // No arrows required
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.info").withStyle(ChatFormatting.DARK_PURPLE));
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.hunger", HUNGER_COST).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.many_bows.ethereal_hunter.tooltip.legend").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
