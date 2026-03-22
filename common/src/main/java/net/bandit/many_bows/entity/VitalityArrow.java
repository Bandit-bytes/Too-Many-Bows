package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.VitalityWeaverBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class VitalityArrow extends AbstractArrow {

    private Consumer<LivingEntity> onHitCallback;
    private int lifetime = 0;

    public VitalityArrow(EntityType<? extends VitalityArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public VitalityArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VITALITY_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private VitalityWeaverBowConfig config() {
        return VitalityWeaverBowConfig.get();
    }

    private void applyConfigValues() {
        VitalityWeaverBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setOnHitCallback(Consumer<LivingEntity> callback) {
        this.onHitCallback = callback;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > config().max_lifetime_ticks) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        LivingEntity target = result.getEntity() instanceof LivingEntity le ? le : null;
        if (target == null) {
            super.onHitEntity(result);
            return;
        }

        if (level().isClientSide()) {
            super.onHitEntity(result);
            return;
        }

        float before = target.getHealth();

        super.onHitEntity(result);

        float after = target.getHealth();
        float damageDealt = before - after;

        if (damageDealt > 0.0F && this.getOwner() instanceof LivingEntity shooter) {
            if (onHitCallback != null) {
                onHitCallback.accept(target);
            }

            float healAmount = damageDealt * config().lifesteal_percent;
            if (healAmount > 0.0F) {
                shooter.heal(healAmount);
            }

            if (config().play_heal_sound) {
                level().playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        config().heal_sound_volume,
                        config().heal_sound_pitch
                );
            }
        }

        this.discard();
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}