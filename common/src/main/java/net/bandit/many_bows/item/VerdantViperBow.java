package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.VenomArrow;
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

public class VerdantViperBow extends BowItem {

    public VerdantViperBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                VenomArrow venomArrow = new VenomArrow(level, player);
                venomArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                venomArrow.setBaseDamage(venomArrow.getBaseDamage() * 1.5);

                level.addFreshEntity(venomArrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip.ability").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.too_many_bows.verdant_viper_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
    }
}
