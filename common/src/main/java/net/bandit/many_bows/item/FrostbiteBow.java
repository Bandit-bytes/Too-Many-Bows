package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.FrostbiteArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FrostbiteBow extends BowItem {

    private static final int MIN_CHARGE_REQUIRED = 10;
    private boolean hasPlayedPullSound = false;

    public FrostbiteBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : findArrowInInventory(player);

            if (charge >= MIN_CHARGE_REQUIRED && power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                FrostbiteArrow arrow = new FrostbiteArrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 4.0F, 1.0F);
                arrow.setBaseDamage(arrow.getBaseDamage() + 4.0);

                // Prevent pickup if Infinity is enabled
                if (hasInfinity) {
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                level.addFreshEntity(arrow);
                createSnowBurstParticles(level, player);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);

                // Consume arrow if not in Creative mode or with Infinity
                if (!hasInfinity) {
                    arrowStack.shrink(1);
                }
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        hasPlayedPullSound = false;
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (level.isClientSide() && livingEntity instanceof Player player) {
            if (player.getRandom().nextFloat() < 0.2F) {
                createPullingSnowParticles(level, livingEntity);
            }
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
        for (int i = 0; i < 30; i++) {
            double offsetX = entity.getRandom().nextGaussian() * 0.6D;
            double offsetY = entity.getRandom().nextGaussian() * 0.6D + 0.5D;
            double offsetZ = entity.getRandom().nextGaussian() * 0.6D;
            level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX() + offsetX, entity.getY() + entity.getBbHeight() / 2.0 + offsetY, entity.getZ() + offsetZ, 0.0D, 0.1D, 0.0D);
        }
    }
}
