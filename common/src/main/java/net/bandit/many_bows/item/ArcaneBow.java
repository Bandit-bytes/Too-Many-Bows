package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class ArcaneBow extends BowItem {

    public ArcaneBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        super.releaseUsing(stack, level, entity, timeCharged);

        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHROOMLIGHT_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (charge >= 20) {
                fireExtraArrow(level, player);
            }
        }
    }
    private void fireExtraArrow(Level level, Player player) {
        AbstractArrow extraArrow = new AbstractArrow(EntityType.ARROW, player, level) {
            @Override
            protected ItemStack getPickupItem() {
                return new ItemStack(Items.ARROW);
            }
        };
        extraArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.0F, 1.0F);
        extraArrow.setBaseDamage(extraArrow.getBaseDamage() + 5.0); // Increase damage by 5.0
        level.addFreshEntity(extraArrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        super.onUseTick(level, entity, stack, timeLeft);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge == 10) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHROOMLIGHT_STEP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip.ability")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF00FF)))); // Fancy Color
    }

}
