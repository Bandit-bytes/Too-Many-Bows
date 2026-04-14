package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SoulhoardBowConfig;
import net.bandit.many_bows.entity.SoulhoardArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.common.SoulLanternCompatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class SoulhoardBow extends ModBowItem {

    private static final String CONFIG_NAME = "soulhoard_bow";
    private static final String SOULS_KEY = "SoulCount";
    private static final String BOW_ID_KEY = "SoulhoardBowId";

    public SoulhoardBow(Properties properties) {
        super(properties);
    }

    private static SoulhoardBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SoulhoardBowConfig.class, SoulhoardBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (!(entity instanceof Player player)) {
            return;
        }

        SoulhoardBowConfig config = config();
        ItemStack foundAmmo = player.getProjectile(bowStack);

        boolean infiniteAmmo = hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild;
        if (foundAmmo.isEmpty() && !infiniteAmmo) {
            return;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;
        float power = getPowerForTime(charge);
        if (power < 0.1F) {
            return;
        }

        ItemStack ammoTemplate = foundAmmo.isEmpty() ? new ItemStack(Items.ARROW) : foundAmmo.copy();
        ammoTemplate.setCount(1);

        int storedSouls = getSoulCount(bowStack);
        boolean lanternBoosted =
                config.soul_lantern_synergy_enabled && SoulLanternCompatHelper.isSoulLanternEquipped(player);

        boolean fullyCharged =
                !config.release_requires_full_charge || power >= config.full_charge_required_power;

        int requiredSoulsToRelease = getMaxSoulCapacity(player);
        boolean empowered = fullyCharged && storedSouls >= requiredSoulsToRelease;

        UUID sourceBowId = getOrCreateBowId(bowStack);

        if (level instanceof ServerLevel serverLevel) {
            ItemStack projectileStack = ammoTemplate.copy();
            SoulhoardArrow arrow = new SoulhoardArrow(serverLevel, player, bowStack, projectileStack);

            applyConfiguredDirectDamage(arrow, player, level);
            arrow.setPowerMultiplier(power);
            arrow.setSourceBowId(sourceBowId);
            arrow.setSoulLanternEmpowered(lanternBoosted);

            if (empowered) {
                arrow.setHoardedSouls(storedSouls);
                clearSouls(bowStack);

                if (config.ignite_seconds_on_empowered_shot > 0) {
                    arrow.igniteForSeconds(config.ignite_seconds_on_empowered_shot);
                }
            }

            applyPowerEnchantment(arrow, bowStack, level);
            applyFlameEnchantment(arrow, bowStack, level);
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, 1.5D);

            if (infiniteAmmo) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            } else {
                arrow.pickup = config.allow_pickup
                        ? AbstractArrow.Pickup.ALLOWED
                        : AbstractArrow.Pickup.DISALLOWED;
            }

            shootProjectile(player, arrow, power * 2.8F, 1.0F);
            serverLevel.addFreshEntity(arrow);

            if (!infiniteAmmo) {
                consumeMatchingAmmo(player, ammoTemplate, 1);
            }
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                empowered ? SoundEvents.WITHER_SHOOT : SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                empowered ? config.empowered_fire_sound_volume : config.normal_fire_sound_volume,
                empowered ? config.empowered_fire_sound_pitch : config.normal_fire_sound_pitch
        );

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            damageBow(bowStack, player, player.getUsedItemHand());
        }
    }

    private void shootProjectile(Player shooter, AbstractArrow arrow, float velocity, float inaccuracy) {
        arrow.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, velocity, inaccuracy);
    }

    private int countMatchingAmmo(Player player, ItemStack template) {
        int total = 0;

        for (ItemStack stack : player.getInventory().offhand) {
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                total += stack.getCount();
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private void consumeMatchingAmmo(Player player, ItemStack template, int amount) {
        int remaining = amount;

        for (ItemStack stack : player.getInventory().offhand) {
            if (remaining <= 0) break;
            if (!ItemStack.isSameItemSameComponents(stack, template)) continue;

            int taken = Math.min(stack.getCount(), remaining);
            stack.shrink(taken);
            remaining -= taken;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!ItemStack.isSameItemSameComponents(stack, template)) continue;

            int taken = Math.min(stack.getCount(), remaining);
            stack.shrink(taken);
            remaining -= taken;
        }

        player.getInventory().setChanged();
    }

    private void applyConfiguredDirectDamage(SoulhoardArrow arrow, Player player, Level level) {
        SoulhoardBowConfig config = config();

        if (!config.use_ranged_damage_attribute_for_direct_hit) {
            arrow.setBaseDamage(config.direct_hit_damage);
            return;
        }

        var registry = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        var rangedDamageAttr = registry.getHolder(
                ResourceLocation.fromNamespaceAndPath(
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

    public static float getPowerForTime(int charge) {
        float f = (float) charge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    private static CompoundTag getCustomTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setCustomTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getSoulCount(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return Mth.clamp(tag.getInt(SOULS_KEY), 0, getAbsoluteMaxSoulCapacity());
    }

    public static void setSoulCount(ItemStack stack, int amount) {
        setSoulCountClamped(stack, amount, getAbsoluteMaxSoulCapacity());
    }

    public static void addSoul(ItemStack stack, int amount, Player player) {
        int cap = getMaxSoulCapacity(player);
        setSoulCountClamped(stack, getSoulCount(stack) + amount, cap);
    }

    public static void clearSouls(ItemStack stack) {
        setSoulCount(stack, 0);
    }

    public static UUID getOrCreateBowId(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);

        if (!tag.hasUUID(BOW_ID_KEY)) {
            UUID uuid = UUID.randomUUID();
            tag.putUUID(BOW_ID_KEY, uuid);
            setCustomTag(stack, tag);
            return uuid;
        }

        return tag.getUUID(BOW_ID_KEY);
    }
    private static int getAbsoluteMaxSoulCapacity() {
        SoulhoardBowConfig config = config();
        return Math.max(config.max_souls, config.soul_lantern_max_souls);
    }

    public static int getMaxSoulCapacity(Player player) {
        SoulhoardBowConfig config = config();

        if (player != null
                && config.soul_lantern_synergy_enabled
                && SoulLanternCompatHelper.isSoulLanternEquipped(player)) {
            return Math.max(config.max_souls, config.soul_lantern_max_souls);
        }

        return Math.max(1, config.max_souls);
    }

    private static void setSoulCountClamped(ItemStack stack, int amount, int cap) {
        CompoundTag tag = getCustomTag(stack);
        tag.putInt(SOULS_KEY, Mth.clamp(amount, 0, Math.max(1, cap)));
        setCustomTag(stack, tag);
    }

    public static UUID getBowIdIfPresent(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        return tag.hasUUID(BOW_ID_KEY) ? tag.getUUID(BOW_ID_KEY) : null;
    }

    public static boolean awardSoulsToMatchingBow(Player player, UUID bowId, int amount) {
        if (bowId == null || amount <= 0) {
            return false;
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (tryAwardToStack(stack, bowId, amount, player)) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (tryAwardToStack(stack, bowId, amount, player)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryAwardToStack(ItemStack stack, UUID bowId, int amount, Player player) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!stack.is(ItemRegistry.SOULHOARD.get())) {
            return false;
        }

        UUID stackId = getBowIdIfPresent(stack);
        if (stackId == null || !stackId.equals(bowId)) {
            return false;
        }

        addSoul(stack, amount, player);
        return true;
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
        return 18;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(bowStack);

        boolean infiniteAmmo = hasInfinityEnchantment(bowStack, level) || player.getAbilities().instabuild;
        if (!infiniteAmmo && ammo.isEmpty()) {
            return InteractionResultHolder.fail(bowStack);
        }

        if (!infiniteAmmo) {
            ItemStack ammoTemplate = ammo.copy();
            ammoTemplate.setCount(1);

            if (countMatchingAmmo(player, ammoTemplate) < 1) {
                return InteractionResultHolder.fail(bowStack);
            }
        }

        getOrCreateBowId(bowStack);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int souls = getSoulCount(stack);

        int maxSouls = Math.max(1, config().max_souls);

        if (config().soul_lantern_synergy_enabled && souls > config().max_souls) {
            maxSouls = Math.max(maxSouls, config().soul_lantern_max_souls);
        }

        tooltipComponents.add(
                Component.translatable("tooltip.too_many_bows.soulhoard.souls", souls, maxSouls)
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF9F43)))
        );

        if (Screen.hasShiftDown()) {
            tooltipComponents.add(
                    Component.translatable("tooltip.too_many_bows.soulhoard.1")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
            );
            tooltipComponents.add(
                    Component.translatable("tooltip.too_many_bows.soulhoard.2")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xC86BFF)))
            );
            tooltipComponents.add(
                    Component.translatable("tooltip.too_many_bows.soulhoard.lantern")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0x79E5FF)))
            );
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}