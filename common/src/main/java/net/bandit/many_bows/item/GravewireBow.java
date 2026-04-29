package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.GravewireBowConfig;
import net.bandit.many_bows.entity.GravewireArrow;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class GravewireBow extends ModBowItem {

    private static final String CONFIG_NAME = "gravewire_bow";

    public GravewireBow(Properties properties) {
        super(properties);
    }

    private static GravewireBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, GravewireBowConfig.class, GravewireBowConfig::new);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        GravewireBowConfig config = config();
        ItemStack foundAmmo = player.getProjectile(bowStack);

        boolean infiniteAmmo = hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild;
        if (foundAmmo.isEmpty() && !infiniteAmmo) {
            return infiniteAmmo;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;
        float power = getPowerForTime(charge);
        if (power < 0.1F) {
            return infiniteAmmo;
        }

        ItemStack ammoTemplate = foundAmmo.isEmpty() ? new ItemStack(Items.ARROW) : foundAmmo.copy();
        ammoTemplate.setCount(1);

        int arrowsPerShot = Math.max(1, config.arrows_per_shot);

        if (!infiniteAmmo && config.require_full_ammo_for_volley) {
            int available = countMatchingAmmo(player, ammoTemplate);
            if (available < arrowsPerShot) {
                return infiniteAmmo;
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < arrowsPerShot; i++) {
                ItemStack projectileStack = ammoTemplate.copy();
                AbstractArrow arrow = new GravewireArrow(serverLevel, player, bowStack, projectileStack);

                if (arrow instanceof GravewireArrow gravewireArrow) {
                    applyConfiguredDirectDamage(gravewireArrow, player, level);
                    gravewireArrow.setPowerMultiplier(power);
                    gravewireArrow.setIgnoreTargetHurtFrames(config.ignore_hurt_iframes);
                }

                applyPowerEnchantment(arrow, bowStack, level);
                applyFlameEnchantment(arrow, bowStack, level);
                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, 1.5D);

                if (infiniteAmmo) {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                } else {
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                }

                float spread = config.arrow_spread_degrees;
                float angle = arrowsPerShot == 1
                        ? 0.0F
                        : ((float) i - ((arrowsPerShot - 1) / 2.0F)) * spread;

                shootProjectile(player, arrow, power * 2.8F, 1.0F, angle);
                serverLevel.addFreshEntity(arrow);
            }

            if (!infiniteAmmo) {
                consumeMatchingAmmo(player, ammoTemplate, arrowsPerShot);
            }
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundRegistry.GRAVEWIRE_FIRE.get(),
                SoundSource.PLAYERS,
                1.0F,
                0.8F
        );

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            damageBow(bowStack, player, player.getUsedItemHand());
        }
        return infiniteAmmo;
    }

    private void shootProjectile(Player shooter, AbstractArrow arrow, float velocity, float inaccuracy, float angle) {
        arrow.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    private int countMatchingAmmo(Player player, ItemStack template) {
        int total = 0;

        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (ItemStack.isSameItemSameComponents(stack, template)) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private void consumeMatchingAmmo(Player player, ItemStack template, int amount) {
        int remaining = amount;

        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (remaining <= 0) break;

            ItemStack stack = inventory.getItem(i);
            if (!ItemStack.isSameItemSameComponents(stack, template)) continue;

            int taken = Math.min(stack.getCount(), remaining);
            stack.shrink(taken);
            remaining -= taken;

            if (stack.isEmpty()) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        inventory.setChanged();
    }

    private void applyConfiguredDirectDamage(GravewireArrow arrow, Player player, Level level) {
        GravewireBowConfig config = config();

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

    private void applyPowerEnchantment(AbstractArrow arrow, ItemStack bow, Level level) {
        Holder<Enchantment> power = getEnchantmentHolder(level, Enchantments.POWER);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, bow);
        if (powerLevel > 0) {
            double extraDamage = 0.5D * powerLevel + 1.0D;
            arrow.setBaseDamage(((AbstractArrowAccessor) arrow).manybows$getBaseDamage() + extraDamage);
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
        ItemStack ammo = player.getProjectile(bowStack);

        boolean infiniteAmmo = hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild;
        if (!infiniteAmmo) {
            if (ammo.isEmpty()) {
                return InteractionResult.FAIL;
            }

            ItemStack ammoTemplate = ammo.copy();
            ammoTemplate.setCount(1);

            if (countMatchingAmmo(player, ammoTemplate) < Math.max(1, config().arrows_per_shot)) {
                return InteractionResult.FAIL;
            }
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(
                    Component.translatable("item.many_bows.gravewire_bow.tooltip")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
            );
            tooltip.accept(
                    Component.translatable("item.many_bows.gravewire_bow.tooltip.ability")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xA7FF00)))
            );
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}