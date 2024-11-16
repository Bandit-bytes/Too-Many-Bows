package net.bandit.many_bows.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.EntityHitResult;

public class SentinelArrow extends Arrow {
    private static final float DAMAGE_MULTIPLIER = 4.0f;

    public SentinelArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public SentinelArrow(Level level, LivingEntity owner) {
        super(level, owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof LivingEntity target) {
            if (isRaidMob(target)) {
                float extraDamage = (float) this.getBaseDamage() * (DAMAGE_MULTIPLIER - 1);

                // Use the new method to get a damage source for arrows
                Entity owner = this.getOwner();
                DamageSource damageSource = owner != null
                        ? this.level().damageSources().arrow(this, owner)
                        : this.level().damageSources().arrow(this, null);

                target.hurt(damageSource, extraDamage);

                // Play a special sound when hitting raid mobs
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Add particles for visual effect
                this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
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
