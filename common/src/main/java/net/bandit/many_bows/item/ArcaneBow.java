package net.bandit.many_bows.item;

import net.bandit.many_bows.client.ClientTooltipHelper;
import net.bandit.many_bows.config.bows.ArcaneBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneBow extends ModBowItem {

    public ArcaneBow(Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        try{
            if (!(entity instanceof Player player)) return false;

        ItemStack ammoInInv = player.getProjectile(bowStack);
        if (ammoInInv.isEmpty() && !player.hasInfiniteMaterials()) {
            return false;
        }

        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        ArcaneBowConfig config = ArcaneBowConfig.get();
        float power = getPowerForTime(scaledCharge, config);
        if (power < config.minimum_power_to_fire) return false;

        List<ItemStack> projectiles = ProjectileWeaponItem.draw(bowStack, ammoInInv, player);
        if (projectiles.isEmpty()) return false;

        if (level instanceof ServerLevel serverLevel) {
            ItemStack usedAmmo = projectiles.get(0);

            fireConfiguredVolley(serverLevel, player, bowStack, usedAmmo, power, config);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F);

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }finally {
        if (level.isClientSide()) {
            this.manybows$resetPullVisual(bowStack);
        }
    }
}

    private void fireConfiguredVolley(ServerLevel level, Player player, ItemStack bowStack, ItemStack ammoStack, float power, ArcaneBowConfig config) {
        int count = Math.max(1, config.projectile_count);
        for (int i = 0; i < count; i++) {
            float offset = (i - (count - 1) / 2.0F) * config.spread_angle_degrees;
            boolean center = Math.abs(offset) < 0.001F;

            Projectile proj = this.createProjectile(level, player, bowStack, ammoStack, power == 1.0F);
            if (!(proj instanceof AbstractArrow arrow)) continue;

            EnchantmentHelper.onProjectileSpawned(level, bowStack, arrow, (item) -> {});


            setArrowDamage(arrow, config.base_damage);
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, config.crit_bonus_multiplier);

            arrow.pickup = center && config.center_arrow_pickup_allowed
                    ? AbstractArrow.Pickup.ALLOWED
                    : (config.side_arrows_creative_only ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED);


            float yaw = player.getYRot() + offset;
            arrow.shootFromRotation(player, player.getXRot(), yaw, 0.0F,
                    power * config.projectile_velocity_multiplier, config.projectile_inaccuracy);

            level.addFreshEntity(arrow);
        }

        if (config.damage_bow_when_fired && !player.hasInfiniteMaterials()) {
            damageBow(bowStack, player, player.getUsedItemHand());
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {
            arrow = ((ArrowItem) Items.ARROW).createArrow(level, new ItemStack(Items.ARROW), shooter, weaponStack);
        }

        if (crit) arrow.setCritArrow(true);
        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        return getPowerForTime(pCharge, ArcaneBowConfig.get());
    }

    private static float getPowerForTime(int pCharge, ArcaneBowConfig config) {
        float divisor = Math.max(1.0F, config.charge_divisor);
        float f = (float) pCharge / divisor;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, config.max_power);
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

        if (ClientTooltipHelper.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
