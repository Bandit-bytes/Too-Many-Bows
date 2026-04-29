package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VaultpiercerBowConfig;
import net.bandit.many_bows.entity.VaultpiercerArrow;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VaultpiercerBow extends ModBowItem {

    private static final String CONFIG_NAME = "vaultpiercer";

    public VaultpiercerBow(Properties properties) {
        super(properties);
    }

    private static VaultpiercerBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, VaultpiercerBowConfig.class, VaultpiercerBowConfig::new);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        ItemStack arrowStack = player.getProjectile(bowStack);
        if (arrowStack.isEmpty() && !player.getAbilities().instabuild) {
            return false;
        }

        ItemStack ammoStack = arrowStack.isEmpty() ? new ItemStack(Items.ARROW) : arrowStack;
        int charge = this.getUseDuration(bowStack, entity) - chargeTime;
        float power = getPowerForTime(charge);

        if (power < 0.1F) {
            return false;
        }

        List<ItemStack> projectiles = draw(bowStack, ammoStack, player);
        boolean arrowConsumed = false;
        VaultpiercerBowConfig config = config();

        if (!projectiles.isEmpty() && level instanceof ServerLevel serverLevel) {
            for (ItemStack projectileStack : projectiles) {
                AbstractArrow arrow;

                if (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW)) {
                    arrow = ((ArrowItem) projectileStack.getItem()).createArrow(serverLevel, projectileStack, player, bowStack);
                } else {
                    arrow = new VaultpiercerArrow(serverLevel, player, bowStack, projectileStack);
                    if (arrow instanceof VaultpiercerArrow vaultArrow) {
                        applyConfiguredDirectDamage(vaultArrow, player, level);
                        vaultArrow.setPowerMultiplier(power);
                        vaultArrow.setFollowUpProjectile(false);
                        vaultArrow.setPortalStrikeEnabled(config.portal_strike_enabled);
                    }
                }

                applyPowerEnchantment(arrow, bowStack, level);
                applyFlameEnchantment(arrow, bowStack, level);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * config.projectile_velocity, 1.0F);
                applyKnockbackEnchantment(arrow, bowStack, player, level);
                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, 1.5D);
                applyPickupRule(arrow, bowStack, player);

                serverLevel.addFreshEntity(arrow);

                if (!hasInfinityEnchantment(bowStack, level) && !player.getAbilities().instabuild && !arrowConsumed) {
                    projectileStack.shrink(1);
                    arrowConsumed = true;
                }
            }
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                1.0F,
                1.1F
        );

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            damageBow(bowStack, player, player.getUsedItemHand());
        }
        return arrowConsumed;
    }

    private void applyConfiguredDirectDamage(VaultpiercerArrow arrow, Player player, Level level) {
        VaultpiercerBowConfig config = config();

        if (!config.use_ranged_damage_attribute_for_direct_hit) {
            arrow.setBaseDamage(config.direct_hit_damage);
            return;
        }

        var registry = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var rangedDamageAttr = registry.get(
                Identifier.fromNamespaceAndPath(
                        config.ranged_damage_attribute_namespace,
                        config.ranged_damage_attribute_path
                )
        ).orElse(null);

        if (rangedDamageAttr == null || config.direct_damage_attribute_divisor == 0.0D) {
            arrow.setBaseDamage(config.direct_hit_damage);
            return;
        }

        var attrInstance = player.getAttribute(rangedDamageAttr);
        if (attrInstance == null) {
            arrow.setBaseDamage(config.direct_hit_damage);
            return;
        }

        arrow.setBaseDamage(attrInstance.getValue() / config.direct_damage_attribute_divisor);
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
            double resistance = Math.max(0.0, 1.0 - shooter.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 knockbackVec = arrow.getDeltaMovement().normalize().scale(punchLevel * 0.6 * resistance);
            arrow.push(knockbackVec.x, 0.1, knockbackVec.z);
        }
    }

    private void applyPowerEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> power = getEnchantmentHolder(level, Enchantments.POWER);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, bow);
        if (powerLevel > 0) {
            double extraDamage = 0.5D * powerLevel + 1.0D;
            arrow.setBaseDamage(((AbstractArrowAccessor) arrow).manybows$getBaseDamage() + extraDamage);
        }
    }

    private void applyPickupRule(AbstractArrow arrow, ItemStack bow, Player player) {
        boolean configAllowsPickup = true;
        if (arrow instanceof VaultpiercerArrow vaultArrow) {
            configAllowsPickup = vaultArrow.isPickupAllowedByConfig();
        }

        if (!configAllowsPickup) {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            return;
        }

        if (hasInfinityEnchantment(bow, player.level()) || player.getAbilities().instabuild) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        }
    }

    private boolean hasInfinityEnchantment(ItemStack bow, Level level) {
        Holder<Enchantment> infinity = getEnchantmentHolder(level, Enchantments.INFINITY);
        return EnchantmentHelper.getItemEnchantmentLevel(infinity, bow) > 0;
    }

    private Holder<Enchantment> getEnchantmentHolder(Level level, ResourceKey<Enchantment> enchantmentKey) {
        return level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(enchantmentKey);
    }

    public static float getPowerForTime(int charge) {
        float f = (float) charge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
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
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();

        if (!player.hasInfiniteMaterials() && !hasArrows) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                Item.TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(
                    Component.translatable("item.many_bows.vaultpiercer.tooltip")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
            );
            tooltip.accept(
                    Component.translatable("item.many_bows.vaultpiercer.tooltip.ability")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xF3C86B)))
            );
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}