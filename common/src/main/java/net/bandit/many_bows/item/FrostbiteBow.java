package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.FrostbiteArrow;
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
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FrostbiteBow extends BowItem {

    private boolean hasPlayedPullSound = false;

    public FrostbiteBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            FrostbiteArrow arrow = new FrostbiteArrow(level, player);

            // 4 = speed (kept constant)
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.0F, 1.0F);

            // Add extra damage to the arrow
            arrow.setBaseDamage(arrow.getBaseDamage() + 4.0);  // Add 4.0 extra damage to the arrow

            level.addFreshEntity(arrow);

            // Add a burst of snow particles around the player when shooting
            createSnowBurstParticles(level, player);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F); // Frostbite-themed shoot sound
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        // Reset the pull sound flag when releasing the bow
        hasPlayedPullSound = false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (level.isClientSide() && livingEntity instanceof Player player) {
            // Create a small snowflake effect while pulling the bow
            if (player.getRandom().nextFloat() < 0.2F) {
                createPullingSnowParticles(level, livingEntity);
            }

            // Play the pull sound only once when pulling starts
            if (!hasPlayedPullSound) {
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.SNOW_BREAK, SoundSource.PLAYERS, 0.5F, 1.0F);
                hasPlayedPullSound = true;
            }
        }

        super.onUseTick(level, livingEntity, stack, count);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.frostbite_bow.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.many_bows.frostbite_bow.tooltip.ability").withStyle(ChatFormatting.DARK_AQUA));
    }

    private void createPullingSnowParticles(Level level, LivingEntity entity) {
        double offsetX = entity.getRandom().nextGaussian() * 0.2D;
        double offsetY = entity.getRandom().nextGaussian() * 0.2D + 0.3D;
        double offsetZ = entity.getRandom().nextGaussian() * 0.2D;
        level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX() + offsetX, entity.getY() + entity.getBbHeight() / 1.5 + offsetY, entity.getZ() + offsetZ, 0.0D, 0.0D, 0.0D);
    }

    private void createSnowBurstParticles(Level level, LivingEntity entity) {
        for (int i = 0; i < 30; i++) { // Larger burst of particles when the arrow is fired
            double offsetX = entity.getRandom().nextGaussian() * 0.6D;
            double offsetY = entity.getRandom().nextGaussian() * 0.6D + 0.5D;
            double offsetZ = entity.getRandom().nextGaussian() * 0.6D;
            level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX() + offsetX, entity.getY() + entity.getBbHeight() / 2.0 + offsetY, entity.getZ() + offsetZ, 0.0D, 0.1D, 0.0D);
        }
    }
}
