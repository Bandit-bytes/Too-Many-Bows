package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.SonicBoomProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SonicBoomBow extends BowItem {

    public SonicBoomBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                // Create and fire the sonic boom projectile
                SonicBoomProjectile sonicBoom = new SonicBoomProjectile(level, player);
                sonicBoom.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 5.0F, 0.5F); // Increased velocity and lower inaccuracy
                level.addFreshEntity(sonicBoom);

                // Play the sonic boom sound
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Damage the bow item
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Epic tooltips with a theme
        tooltip.add(Component.translatable("item.too_many_bows.sonic_bow.tooltip").withStyle(ChatFormatting.GOLD)); // General description
        tooltip.add(Component.translatable("item.too_many_bows.sonic_bow.tooltip.ability").withStyle(ChatFormatting.AQUA)); // Describes its ability
        tooltip.add(Component.translatable("item.too_many_bows.sonic_bow.tooltip.legend").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)); // Adds a lore element
    }

}
