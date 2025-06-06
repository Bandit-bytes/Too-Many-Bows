package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.FrostbiteArrow;
import net.bandit.many_bows.entity.RadiantArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RadianceBow extends BowItem {

    private static final int EXPERIENCE_COST = 5; // XP required per shot

    public RadianceBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge);

            boolean hasInfinity = hasInfinityEnchantment(bowStack, level);
            boolean hasXP = consumeExperience(player) || hasInfinity || player.getAbilities().instabuild;

            if (power >= 0.1F && hasXP) {
                RadiantArrow arrow = new RadiantArrow(level, player, bowStack, new ItemStack(Items.ARROW));
                if (arrow instanceof RadiantArrow radiantArrow) {
                    Holder<Attribute> rangedDamageAttr = level.registryAccess()
                            .registryOrThrow(Registries.ATTRIBUTE)
                            .getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage"))
                            .orElse(null);

                    if (rangedDamageAttr != null) {
                        AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                        if (attrInstance != null) {
                            float damage = (float) attrInstance.getValue();
                            radiantArrow.setBaseDamage(damage / 2);
                        }
                    }
                    radiantArrow.setPowerMultiplier(power);
                }
                // ✅ Apply Enchantments
                applyPowerEnchantment(arrow, bowStack, level);
                applyKnockbackEnchantment(arrow, bowStack, player, level);
                applyFlameEnchantment(arrow, bowStack, level);

                // ✅ Arrow Behavior (Infinity means it doesn't despawn)
                arrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;

                // ✅ Fire the arrow
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                level.addFreshEntity(arrow);

                // ✅ Play XP sound effect
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);

                // ✅ Durability loss
                bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private boolean consumeExperience(Player player) {
        int totalExperience = player.totalExperience;
        if (totalExperience >= EXPERIENCE_COST) {
            player.giveExperiencePoints(-EXPERIENCE_COST);
            return true;
        }
        player.displayClientMessage(Component.translatable("item.many_bows.radiance.no_experience")
                .withStyle(ChatFormatting.RED), true);
        return false;
    }

    // 🔥 Enchantment Handling 🔥
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
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // No arrows required
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.info").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.xp", EXPERIENCE_COST).withStyle(ChatFormatting.RED));
        } else {
            tooltipComponents.add(Component.translatable("item.many_bows.radiance.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
