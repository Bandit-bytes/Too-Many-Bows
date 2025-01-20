package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.IcicleJavelin;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IcicleJavelinBow extends BowItem {
    private static final int MIN_CHARGE_REQUIRED = 10;
    private static final double BASE_DAMAGE = 3.0;
    private boolean hasPlayedPullSound = false;

    public IcicleJavelinBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check if player has Infinity or is in Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            // Check if the player has any arrows in their inventory (required if not Creative or no Infinity)
            ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

            // Check if the bow is fully charged and the player can shoot
            if (charge >= MIN_CHARGE_REQUIRED && power >= 0.1F && (hasInfinity || !arrowStack.isEmpty())) {
                // Create and shoot the Icicle Javelin
                IcicleJavelin javelin = new IcicleJavelin(level, player);
                javelin.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.5F, 1.0F);

                // Apply enchantment
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (powerLevel > 0) {
                    javelin.setBaseDamage(BASE_DAMAGE + powerLevel * 0.5);
                }
                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punchLevel > 0) {
                    javelin.setKnockback(punchLevel);
                }
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                    javelin.setSecondsOnFire(100);
                }

                // Set the pickup status based on Infinity or Creative mode
                javelin.pickup = hasInfinity ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;

                // Add the projectile to the world
                level.addFreshEntity(javelin);
                createIceParticles(level, player);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Consume the arrow and damage the bow if not in Creative mode or without Infinity
                if (!hasInfinity) {
                    arrowStack.shrink(1);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
        hasPlayedPullSound = false;
    }
    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.many_bows.icicle_javelin_bow.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.many_bows.icicle_javelin_bow.tooltip.ability").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.many_bows.icicle_javelin_bow.tooltip.freeze").withStyle(ChatFormatting.BLUE));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }

    private void createIceParticles(Level level, LivingEntity entity) {
        for (int i = 0; i < 20; i++) {
            double offsetX = entity.getRandom().nextGaussian() * 0.3D;
            double offsetY = entity.getRandom().nextGaussian() * 0.3D;
            double offsetZ = entity.getRandom().nextGaussian() * 0.3D;
            level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX() + offsetX, entity.getY() + entity.getBbHeight() / 1.5 + offsetY, entity.getZ() + offsetZ, 0, 0, 0);
        }
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
