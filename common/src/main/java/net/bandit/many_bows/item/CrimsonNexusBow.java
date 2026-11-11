package net.bandit.many_bows.item;

import net.bandit.many_bows.config.BowJsonConfigHelper;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class CrimsonNexusBow extends ModBowItem {
    private final WeakHashMap<Player, Long> activeLifeDrain = new WeakHashMap<>();
    public CrimsonNexusBow(Properties properties) {
        super(properties);
    }
    private static CrimsonNexusBowConfig config() {
        return BowJsonConfigHelper.getConfig("crimson_nexus", CrimsonNexusBowConfig.class, CrimsonNexusBowConfig::new);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || !selected || level.isClientSide) return;

        CrimsonNexusBowConfig config = config();
        Long lastUsedTime = activeLifeDrain.get(player);
        if (lastUsedTime == null || level.getGameTime() - lastUsedTime > config.life_drain_window_ticks) return;

        if (level.getGameTime() % Math.max(1, config.life_drain_interval_ticks) == 0) {
            float damage = (float) resolveConfiguredDamage(player, config.life_drain_base_damage,
                    config.use_ranged_damage_attribute, config.life_drain_ranged_damage_divisor, 0.0D);

            double radius = config.life_drain_radius;
            AABB area = new AABB(player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                    player.getX() + radius, player.getY() + radius, player.getZ() + radius);

            level.getEntities(player, area, e -> e instanceof LivingEntity && e != player)
                    .forEach(target -> {
                        if (target instanceof LivingEntity livingEntity) {
                            livingEntity.hurt(player.damageSources().magic(), damage);
                            player.heal(config.life_drain_heal_per_target);
                        }
                    });
        }
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(bowStack, entity) - chargeTime;
            float power = getPowerForTime(charge, entity);

            if (power >= 0.1F) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.5F);

                CrimsonNexusBowConfig config = config();
                float healthCost = player.getHealth() > config.minimum_health_to_pay_cost ? config.health_cost : 0.0F;
                player.hurt(player.damageSources().magic(), healthCost);

                ItemStack arrowStack = player.getProjectile(bowStack);
                ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem ?
                        (ArrowItem) arrowStack.getItem() : (ArrowItem) Items.ARROW;

                AbstractArrow arrow = arrowItem.createArrow(level, arrowStack, player, bowStack);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                arrow.setBaseDamage(resolveConfiguredDamage(player, config.base_damage,
                        config.use_ranged_damage_attribute, config.ranged_damage_attribute_divisor, 0.0D));
                applyBowDamageAttribute(arrow, player);
                tryApplyBowCrit(arrow, player, config.crit_damage_multiplier);

                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * config.arrow_velocity, config.arrow_inaccuracy);
                activeLifeDrain.put(player, level.getGameTime());

                if (player.getHealth() == player.getMaxHealth()) {
                    arrow.setCritArrow(true);
                }

                level.addFreshEntity(arrow);
                damageBow(bowStack, player, InteractionHand.MAIN_HAND);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, inaccuracy);
    }
//Fixed

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
        return 1;
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
            // Detailed information when holding Shift
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.info").withStyle(ChatFormatting.DARK_RED));
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.health_cost", String.valueOf(config().health_cost)).withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.translatable("item.many_bows.crimson_nexus.tooltip.legend").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            // Base message prompting to hold Shift
            tooltipComponents.add(Component.translatable("item.too_many_bows.shulker_blast_bow.hold_shift"));
        }
    }
}
