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
import net.minecraft.world.phys.EntityHitResult;

public class IcicleJavelin extends AbstractArrow {

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
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity o ? o : null;

            // Deal direct damage
            target.hurt(this.damageSources().arrow(this, owner), 6.0f);

            // Apply slowness effect to simulate freezing
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));

            // Play subtle glass sound
            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.2f);

            // Show small snow particle burst
            for (int i = 0; i < 10; i++) {
                double dx = (random.nextDouble() - 0.5) * 0.5;
                double dy = random.nextDouble() * 0.5;
                double dz = (random.nextDouble() - 0.5) * 0.5;
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        target.getX() + dx, target.getY() + dy, target.getZ() + dz,
                        0, -0.03, 0);
            }

            // Remove arrow
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            createFrostTrail();
        }
    }

    private void createFrostTrail() {
        for (int i = 0; i < 3; i++) {
            double dx = (random.nextDouble() - 0.5) * 0.2;
            double dy = (random.nextDouble() - 0.5) * 0.2;
            double dz = (random.nextDouble() - 0.5) * 0.2;
            level().addParticle(ParticleTypes.SNOWFLAKE,
                    this.getX() + dx, this.getY() + dy, this.getZ() + dz,
                    0, 0, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
