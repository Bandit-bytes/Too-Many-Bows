package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.config.ManyBowsConfigHolder;
import net.bandit.many_bows.config.bows.CrimsonNexusBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class CrimsonNexusBow extends ModBowItem {

    private final WeakHashMap<Player, Long> lastShotTime = new WeakHashMap<>();

    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }

    private static CrimsonNexusBowConfig config() {
        return BowJsonConfigHelper.getConfig("crimson_nexus", CrimsonNexusBowConfig.class, CrimsonNexusBowConfig::new);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        int charge = this.getUseDuration(stack) - timeCharged;
        float power = getPowerForTime(charge, entity);

        if (power < 0.1F) return;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

        CrimsonNexusBowConfig config = config();
        float healthCost = player.getHealth() <= config.minimum_health_to_pay_cost ? 0.0F : config.health_cost;
        if (healthCost > 0.0F) {
            player.hurt(player.damageSources().magic(), healthCost);
        }

        ItemStack arrowStack = player.getProjectile(stack);
        ArrowItem arrowItem;

        if (arrowStack.isEmpty() || !(arrowStack.getItem() instanceof ArrowItem)) {
            arrowItem = (ArrowItem) Items.ARROW;
            arrowStack = new ItemStack(Items.ARROW);
        } else {
            arrowItem = (ArrowItem) arrowStack.getItem();
        }

        AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        if (config.use_ranged_damage_attribute || config.base_damage >= 0.0D) {
            double fallback = config.base_damage >= 0.0D ? config.base_damage : arrow.getBaseDamage();
            arrow.setBaseDamage(resolveConfiguredDamage(player, fallback,
                    config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, 0.0D));
        }

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);
        applyStandardEnchantments(arrow, stack, 1.5D);

        applyBowDamageAttribute(arrow, player);
        tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

        if (player.getHealth() == player.getMaxHealth()) {
            arrow.setCritArrow(true);
            arrow.setSecondsOnFire(config.full_health_fire_seconds);
        }

        level.addFreshEntity(arrow);
        lastShotTime.put(player, level.getGameTime());

        damageBow(stack, player, player.getUsedItemHand());
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getAbilities().instabuild || player.getProjectile(stack).isEmpty() || this.getAllSupportedProjectiles().test(player.getProjectile(stack))) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player)) return;
        if (!selected) return;
        if (level.isClientSide) return;

        CrimsonNexusBowConfig config = config();
        if (!config.life_drain_enabled) return;

        if (config.life_drain_window_ticks >= 0) {
            Long lastShot = lastShotTime.get(player);
            if (lastShot == null || level.getGameTime() - lastShot > config.life_drain_window_ticks) return;
        }

        if (level.getGameTime() % Math.max(1, config.life_drain_interval_ticks) != 0) return;
        if (player.getHealth() >= player.getMaxHealth()) return;

        double h = config.life_drain_horizontal_radius;
        double v = config.life_drain_vertical_radius;
        AABB area = new AABB(player.getX() - h, player.getY() - v, player.getZ() - h,
                player.getX() + h, player.getY() + v, player.getZ() + h);

        List<LivingEntity> nearby = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(config.life_drain_ignores_players && e instanceof Player)));

        BowLootConfig lootConfig = ManyBowsConfigHolder.getConfig();

        nearby.removeIf(target -> {
            String id = target.getType().builtInRegistryHolder().key().location().toString();
            return lootConfig.emeraldSageCrimsonNexusBlacklist.contains(id);
        });

        if (nearby.isEmpty() || config.life_drain_max_targets <= 0) return;

        float damage = (float) resolveConfiguredDamage(player, config.life_drain_base_damage,
                config.use_ranged_damage_attribute, config.life_drain_ranged_damage_divisor, 0.0D);

        Collections.shuffle(nearby, new java.util.Random(level.random.nextLong()));
        for (LivingEntity target : nearby.subList(0, Math.min(config.life_drain_max_targets, nearby.size()))) {
            target.hurt(player.damageSources().magic(), damage);
            player.heal(config.life_drain_heal_per_target);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.3F, 1.5F);

        if (config.life_drain_damages_bow) {
            damageBow(stack, player, player.getUsedItemHand());
        }
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
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> s.getItem() instanceof ArrowItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.info").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", String.valueOf(config().health_cost)).withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }
}
