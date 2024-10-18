package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class SolarBow extends BowItem {

    public SolarBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        super.releaseUsing(stack, level, entity, timeCharged);

        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;

            // Play custom sound on release
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4F, 0.3F);

            // Fire a flaming arrow if fully charged
            if (charge >= 20) {
                fireFlamingArrow(level, player);
            }
        }
    }

    private void fireFlamingArrow(Level level, Player player) {
        if (!level.isClientSide()) {
            // Create an arrow entity
            Arrow arrow = new Arrow(level, player);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            // Set the arrow on fire
            arrow.setSecondsOnFire(100); // Fire for 5 seconds

            // Increase velocity (from 1.5F to 3.0F for faster speed)
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);

            // Increase base damage of the arrow (set higher base damage)
            arrow.setBaseDamage(arrow.getBaseDamage() + 4.0); // Increase damage by 4.0

            // Spawn the flaming arrow in the world
            level.addFreshEntity(arrow);

            // Play a sound for the flaming arrow firing
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        super.onUseTick(level, entity, stack, timeLeft);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge == 10) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.0F);
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.solar_bow.tooltip").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.many_bows.solar_bow.tooltip.ability")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF4500)))); // Bright Flaming Orange
    }

}
