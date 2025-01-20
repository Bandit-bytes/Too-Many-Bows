package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TwinShadowsBow extends BowItem {

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 1.0F) {
                boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

                if (hasInfinity || consumeArrows(player, 2)) {
                    spawnTwinArrows(level, player, stack, hasInfinity);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private void spawnTwinArrows(Level level, Player player, ItemStack stack, boolean hasInfinity) {
        Vec3 arrowDirection = player.getLookAngle();

        // Light Arrow
        Arrow lightArrow = createArrow(level, player, stack, arrowDirection, ChatFormatting.WHITE);
        lightArrow.setBaseDamage(6.0); // Base damage for the light arrow
        lightArrow.addTag("light");
        level.addFreshEntity(lightArrow);

        // Dark Arrow
        Arrow darkArrow = createArrow(level, player, stack, arrowDirection.add(0.1, 0, -0.1), ChatFormatting.DARK_GRAY);
        darkArrow.setBaseDamage(8.0); // Higher damage for the dark arrow
        darkArrow.addTag("dark");
        level.addFreshEntity(darkArrow);

        // Apply enchantments to both arrows
        applyEnchantments(lightArrow, stack);
        applyEnchantments(darkArrow, stack);

        // Add particle effects
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.5, player.getZ(), 0, 0.05, 0);
    }

    private Arrow createArrow(Level level, Player player, ItemStack stack, Vec3 direction, ChatFormatting color) {
        Arrow arrow = new Arrow(level, player);
        arrow.shoot(direction.x, direction.y, direction.z, 3.0F, 1.0F);
        arrow.setSecondsOnFire(0); // Prevent default fire unless enchanted
        arrow.setGlowingTag(true); // Make the arrow visually distinct
        arrow.setCustomName(Component.literal(color + "Shadow Arrow"));
        arrow.pickup = player.getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }

    private void applyEnchantments(Arrow arrow, ItemStack stack) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 1.5);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }
    private boolean consumeArrows(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int arrowsRemoved = 0;

        for (ItemStack stack : player.getInventory().items) {
            ItemStack projectile = player.getProjectile(stack);
            if (!projectile.isEmpty() && arrowsRemoved < count) {
                int removeAmount = Math.min(projectile.getCount(), count - arrowsRemoved);
                projectile.shrink(removeAmount);
                arrowsRemoved += removeAmount;
            }
            if (arrowsRemoved >= count) {
                return true;
            }
        }
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.twin_shadows.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
