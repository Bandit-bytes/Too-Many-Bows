package net.bandit.many_bows.item;

import net.bandit.many_bows.client.ClientTooltipHelper;
import net.bandit.many_bows.config.bows.CrimsonNexusBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class CrimsonNexusBow extends ModBowItem {

    private final WeakHashMap<Player, Long> activeLifeDrain = new WeakHashMap<>();

    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack,
                              ServerLevel level,
                              Entity entity,
                              @Nullable EquipmentSlot slot) {

        if (!(entity instanceof Player player)) return;
        if (slot != EquipmentSlot.MAINHAND) return;

        Long lastUsedTick = activeLifeDrain.get(player);
        if (lastUsedTick == null) return;

        CrimsonNexusBowConfig config = CrimsonNexusBowConfig.get();
        if (level.getGameTime() - lastUsedTick > config.life_drain_duration_ticks) return;

        int interval = Math.max(1, config.life_drain_interval_ticks);
        if (level.getGameTime() % interval != 0) return;

        float drainDamage = (float) (config.life_drain_damage * getBowDamageMultiplier(player));
        double radius = Math.max(0.0D, config.life_drain_radius);

        AABB area = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        level.getEntities(player, area, e -> e instanceof LivingEntity && e != player)
                .forEach(target -> {
                    if (target instanceof LivingEntity living) {
                        living.hurt(player.damageSources().magic(), drainDamage);
                        player.heal(config.heal_per_target);
                    }
                });
    }

    @Override
    public boolean releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
       try{
           if (!(entity instanceof Player player)) return false;


        int charge = this.getUseDuration(bowStack, entity) - chargeTime;

        float mult = this.manybows$getChargeMultiplier(bowStack, entity);
        int scaledCharge = Math.max(0, (int) (charge * mult));

        float power = getPowerForTime(scaledCharge);
        if (power < 0.1F) return false;

        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

            CrimsonNexusBowConfig config = CrimsonNexusBowConfig.get();
            float healthCost = player.getHealth() > config.health_cost * 2.0F ? config.health_cost : 0.0F;
            if (healthCost <= 0.0F) return false;
            player.hurt(player.damageSources().magic(), healthCost);
        }

        ItemStack fakeAmmo = new ItemStack(Items.ARROW);
        List<ItemStack> projectiles = List.of(fakeAmmo);

        if (level instanceof ServerLevel serverLevel) {
            this.shoot(
                    serverLevel,
                    player,
                    player.getUsedItemHand(),
                    bowStack,
                    projectiles,
                    power * CrimsonNexusBowConfig.get().projectile_velocity,
                    1.0F,
                    power == 1.0F,
                    null
            );

            activeLifeDrain.put(player, level.getGameTime());
            damageBow(bowStack, player, player.getUsedItemHand());
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }finally {
           if (level.isClientSide()) {
               this.manybows$resetPullVisual(bowStack);
           }
       }
    }


    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack ammoStack, boolean crit) {
        AbstractArrow arrow;

        Item ammoItem = ammoStack.getItem();
        if (ammoItem instanceof ArrowItem arrowItem
                && (ammoStack.is(Items.SPECTRAL_ARROW) || ammoStack.is(Items.TIPPED_ARROW))) {
            arrow = arrowItem.createArrow(level, ammoStack, shooter, weaponStack);
        } else {

            arrow = ((ArrowItem) Items.ARROW).createArrow(level, new ItemStack(Items.ARROW), shooter, weaponStack);

            setArrowDamage(arrow, CrimsonNexusBowConfig.get().base_damage);
        }

        if (shooter instanceof Player p && p.getHealth() >= p.getMaxHealth()) {
            arrow.setCritArrow(true);
        } else if (crit) {
            arrow.setCritArrow(true);
        }

        if (level instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, weaponStack, arrow, item -> {});
        }

        if (shooter instanceof Player player) {
            applyBowDamageAttribute(arrow, player);
            tryApplyBowCrit(arrow, player, CrimsonNexusBowConfig.get().crit_bonus_multiplier);
        }

        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        return arrow;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float inaccuracy, float angle,
                                   @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
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
                                Consumer<Component> tooltip,
                                TooltipFlag flag) {

        if (isShiftDownSafe()) {
            tooltip.accept(Component.translatable("item.many_bows.crimson_nexus.tooltip.info")
                    .withStyle(ChatFormatting.DARK_RED));

            tooltip.accept(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", "2.0")
                    .withStyle(ChatFormatting.RED));

            tooltip.accept(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("item.too_many_bows.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean isShiftDownSafe() {
        return ClientTooltipHelper.hasShiftDown();
    }
}
