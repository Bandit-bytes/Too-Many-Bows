package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ScatterBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ScatterBow extends ModBowItem {

    public ScatterBow(Properties properties) {
        super(properties);
    }

    private static ScatterBowConfig config() {
        return BowJsonConfigHelper.getConfig("scatter_bow", ScatterBowConfig.class, ScatterBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) return;

        boolean hasInfinity = hasInfinityEnchantment(bowStack, level);
        int charge = this.getUseDuration(bowStack, entity) - chargeTime;
        float power = getPowerForTime(charge, entity);
        if (power < 0.1F) return;

        ScatterBowConfig config = config();
        int arrowsToFire = hasInfinity || player.getAbilities().instabuild
                ? config.max_arrows
                : Math.min(config.max_arrows, countAvailableArrows(player));
        if (arrowsToFire <= 0) return;

        int arrowsConsumed = 0;

        for (int i = 0; i < arrowsToFire; i++) {
            ItemStack arrowStack = player.getProjectile(bowStack);
            if (arrowStack.isEmpty() && !hasInfinity && !player.getAbilities().instabuild) break;

            Arrow arrow = EntityType.ARROW.create(level);
            if (arrow == null) continue;

            arrow.setOwner(player);
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

            // Set base damage
            arrow.setBaseDamage(resolveConfiguredDamage(player, config.base_damage,
                    config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, 0.0D));

            applyPowerEnchantment(arrow, bowStack, level);
            applyKnockbackEnchantment(arrow, bowStack, player, level);
            applyFlameEnchantment(arrow, bowStack, level);
                applyBowDamageAttribute(arrow, player);
tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

            arrow.pickup = (hasInfinity || player.getAbilities().instabuild)
                    ? AbstractArrow.Pickup.CREATIVE_ONLY
                    : AbstractArrow.Pickup.ALLOWED;

            float yawOffset = (level.getRandom().nextFloat() - 0.5F) * config.yaw_spread_degrees;
            float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * config.pitch_spread_degrees;

            arrow.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset,
                    0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);
            level.addFreshEntity(arrow);

            if (!player.getAbilities().instabuild && !hasInfinity && !arrowStack.isEmpty()) {
                arrowStack.shrink(1);
                arrowsConsumed++;
            }
        }

        if (arrowsConsumed > 0 || hasInfinity) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        damageBow(bowStack, player, InteractionHand.MAIN_HAND);
    }


    private int countAvailableArrows(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ARROW_ONLY.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void consumeArrows(Player player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!ARROW_ONLY.test(stack)) continue;
            int toRemove = Math.min(remaining, stack.getCount());
            stack.shrink(toRemove);
            remaining -= toRemove;
            if (remaining <= 0) break;
        }
    }

    private void applyFlameEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> flame = getEnchantmentHolder(level, Enchantments.FLAME);
        int flameLevel = EnchantmentHelper.getItemEnchantmentLevel(flame, bow);
        if (flameLevel > 0) arrow.igniteForSeconds(5);
    }

    private void applyKnockbackEnchantment(AbstractArrow arrow, ItemStack bow, LivingEntity shooter, Level level) {
        Holder<Enchantment> punch = getEnchantmentHolder(level, Enchantments.PUNCH);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(punch, bow);
        if (punchLevel > 0) {
            double resistance = Math.max(0.0, 1.0 - shooter.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            var knockbackVec = arrow.getDeltaMovement().normalize().scale(punchLevel * 0.6 * resistance);
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
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
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
