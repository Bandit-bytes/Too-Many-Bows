package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class IcicleJavelin extends AbstractArrow {
    private boolean exploded = false;

    public IcicleJavelin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public IcicleJavelin(Level level, LivingEntity shooter) {
        super(EntityRegistry.ICICLE_JAVELIN.get(), shooter, level);
        this.setBaseDamage(5.0);
        this.setPierceLevel((byte) 3);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && !exploded) {
            if (result.getEntity() instanceof LivingEntity target) {
                target.hurt(damageSources().arrow(this, this.getOwner()), 6.0f);
                frostExplosion();
                exploded = true;
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.inGround && !exploded) {
            frostExplosion();
            exploded = true;
            this.discard();
        }

        if (level().isClientSide()) {
            createFrostTrail();
        }
    }

    private void frostExplosion() {
        try {
            if (!level().isClientSide()) {
                LivingEntity owner = (this.getOwner() instanceof LivingEntity o) ? o : null;
                AABB area = new AABB(this.blockPosition()).inflate(3.0);
                List<LivingEntity> affected = level().getEntitiesOfClass(LivingEntity.class, area, e ->
                        (owner == null || !e.is(owner)) && e.isAlive() && !e.isRemoved());

                int limit = Math.min(affected.size(), 4);
                for (int i = 0; i < limit; i++) {
                    LivingEntity entity = affected.get(i);
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                }
            }

            level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f + random.nextFloat() * 0.4f);

            if (level().isClientSide()) {
                for (int i = 0; i < 30; i++) {
                    double dx = (random.nextDouble() - 0.5) * 1.5;
                    double dy = random.nextDouble() * 1.0;
                    double dz = (random.nextDouble() - 0.5) * 1.5;
                    level().addParticle(ParticleTypes.SNOWFLAKE,
                            this.getX() + dx, this.getY() + dy, this.getZ() + dz,
                            0, -0.03, 0);
                }
            }

        } catch (Exception e) {
            System.err.println("[IcicleJavelin] Frost explosion failed: " + e);
        }
    }



    private void createFrostTrail() {
        for (int i = 0; i < 3; i++) {
            double dx = (random.nextDouble() - 0.5) * 0.2;
            double dy = (random.nextDouble() - 0.5) * 0.2;
            double dz = (random.nextDouble() - 0.5) * 0.2;
            level().addParticle(ParticleTypes.SNOWFLAKE, this.getX() + dx, this.getY() + dy, this.getZ() + dz, 0, 0, 0);
        }
    }


    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
