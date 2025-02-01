package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.RadiantArrow;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RadianceBow extends BowItem {

    private static final int EXPERIENCE_COST = 5; // Experience points required per shot

    public RadianceBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            if (power >= 0.1F && consumeExperience(player)) { // Check if experience is consumed
                // Create and configure the RadiantArrow
                RadiantArrow arrow = new RadiantArrow(level, player, bowStack, new ItemStack(Items.ARROW));

                arrow.setBaseDamage(arrow.getBaseDamage() + 3.0);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                level.addFreshEntity(arrow);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

                // Bow durability loss
                bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }


    private boolean consumeExperience(Player player) {
        int totalExperience = player.totalExperience;
        if (totalExperience >= EXPERIENCE_COST) {
            player.giveExperiencePoints(-EXPERIENCE_COST); // Deduct experience points
            return true;
        }

        // Notify player if insufficient experience
        player.displayClientMessage(Component.translatable("item.many_bows.radiance.no_experience").withStyle(ChatFormatting.RED), true);
        return false;
    }


    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // No arrows required
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            // Detailed information when holding Shift
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.info").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.xp", EXPERIENCE_COST).withStyle(ChatFormatting.RED));
        } else {
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltipComponents.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }

}
