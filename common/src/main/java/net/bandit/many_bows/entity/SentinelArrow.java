package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SentinelArrow extends AbstractArrow {
    private static final float DAMAGE_MULTIPLIER = 7.0f;

    public SentinelArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SentinelArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SENTINEL_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (isRaidMob(target)) {
                float baseDamage = (float) this.getBaseDamage();
                float extraDamage = baseDamage * (DAMAGE_MULTIPLIER - 1);


                Entity owner = this.getOwner();
                DamageSource damageSource = owner instanceof LivingEntity
                        ? this.level().damageSources().arrow(this, owner)
                        : this.level().damageSources().arrow(this, null);

                target.hurt(damageSource, extraDamage);


                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7F, 1.0F);


                for (int i = 0; i < 10; i++) {
                    double xOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    double yOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    double zOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Pillager ||
                entity instanceof Vindicator ||
                entity instanceof Evoker ||
                entity instanceof Ravager ||
                entity instanceof Illusioner ||
                entity instanceof Witch;
    }
}
