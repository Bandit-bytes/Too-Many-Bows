package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.BeaconBeamArrow;
import net.bandit.many_bows.registry.ItemRegistry;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


import java.util.List;
import java.util.function.Predicate;

public class BeaconBeamBow extends ModBowItem {

    public BeaconBeamBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) return;

        ItemStack ammo = player.getProjectile(bowStack);
        boolean hasInfinity = hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild;

        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            return;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;
        float power = getPowerForTime(charge);

        if (power < 0.1F) return;
        if (level instanceof ServerLevel serverLevel) {
            BeaconBeamArrow arrow = new BeaconBeamArrow(serverLevel, player, bowStack, ammo);

            applyPowerEnchantment(arrow, bowStack, level);
            applyKnockbackEnchantment(arrow, bowStack, player, level);
            applyFlameEnchantment(arrow, bowStack, level);

            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);
            arrow.pickup = hasInfinity ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.DISALLOWED;
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
            serverLevel.addFreshEntity(arrow);
            if (!hasInfinity && !player.getAbilities().instabuild) {
                ammo.shrink(1);
            }
            if (!player.getAbilities().instabuild) {
                damageBow(bowStack, player, InteractionHand.MAIN_HAND);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS,
                1.0F, 1.1F + (power * 0.25F));

        player.awardStat(Stats.ITEM_USED.get(this));
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

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
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
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bowStack);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.too_many_bows.beacon_beam_bow.tooltip").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.too_many_bows.beacon_beam_bow.tooltip.ability").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.too_many_bows.beacon_beam_bow.tooltip.legend").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}