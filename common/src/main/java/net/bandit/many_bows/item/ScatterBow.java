package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class ScatterBow extends BowItem {

    private static final int MAX_ARROWS = 8; // Max arrows fired at once

    public ScatterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            List<ItemStack> projectiles = draw(bowStack, player.getProjectile(bowStack), player);
            boolean hasInfinity = hasInfinityEnchantment(bowStack, level);

            if (!projectiles.isEmpty() || hasInfinity || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                int arrowsAvailable = player.getAbilities().instabuild ? MAX_ARROWS : Math.min(MAX_ARROWS, getArrowCount(player));

                if (power >= 0.1F && arrowsAvailable > 0) {
                    for (int i = 0; i < arrowsAvailable; i++) {
                        Arrow arrow = EntityType.ARROW.create(level);
                        if (arrow != null) {
                            arrow.setOwner(player);
                            arrow.setPos(player.getX(), player.getEyeY(), player.getZ());
                            arrow.setBaseDamage(1.0);

                            applyPowerEnchantment(arrow, bowStack, level);
                            applyKnockbackEnchantment(arrow, bowStack, player, level);
                            applyFlameEnchantment(arrow, bowStack, level);

                            arrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;

                            float yawOffset = (level.getRandom().nextFloat() - 0.5F) * 20F;
                            float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * 10F;
                            arrow.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset, 0.0F, power * 3.0F, 1.0F);

                            level.addFreshEntity(arrow);
                        }
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!hasInfinity && !player.getAbilities().instabuild) {
                        bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                        removeArrowsFromInventory(player, arrowsAvailable);
                    }
                }
            }
        }
    }
    private int getArrowCount(Player player) {
        int arrowCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            ItemStack projectile = player.getProjectile(stack);
            if (!projectile.isEmpty()) {
                arrowCount += projectile.getCount();
            }
        }
        return arrowCount;
    }
    private void removeArrowsFromInventory(Player player, int count) {
        int removed = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                int toRemove = Math.min(stack.getCount(), count - removed);
                stack.shrink(toRemove);
                removed += toRemove;
                if (removed >= count) {
                    break;
                }
            }
        }
    }

    private void applyFlameEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> flame = getEnchantmentHolder(level, Enchantments.FLAME);
        int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(flame, bow);
        if (flameLevel > 0) {
            arrow.igniteForSeconds(5);
        }
    }

    private void applyKnockbackEnchantment(AbstractArrow arrow, ItemStack bow, LivingEntity shooter, Level level) {
        Holder<Enchantment> punch = getEnchantmentHolder(level, Enchantments.PUNCH);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(punch, bow);
        if (punchLevel > 0) {
            double resistance = Math.max(0.0, 1.0 - shooter.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
            net.minecraft.world.phys.Vec3 knockbackVec = arrow.getDeltaMovement().normalize().scale(punchLevel * 0.6 * resistance);
            arrow.push(knockbackVec.x, 0.1, knockbackVec.z);
        }
    }

    private void applyPowerEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> power = getEnchantmentHolder(level, Enchantments.POWER);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, bow);
        if (powerLevel > 0) {
            double extraDamage = 0.5 * powerLevel + 1.0;
            arrow.setBaseDamage(arrow.getBaseDamage() + extraDamage);
        }
    }

    private boolean hasInfinityEnchantment(ItemStack bow, Level level) {
        Holder<Enchantment> infinity = getEnchantmentHolder(level, Enchantments.INFINITY);
        return EnchantmentHelper.getItemEnchantmentLevel(infinity, bow) > 0;
    }

    private Holder<Enchantment> getEnchantmentHolder(Level level, ResourceKey<Enchantment> enchantmentKey) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(enchantmentKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.scatter_bow.tooltip").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.scatter_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
