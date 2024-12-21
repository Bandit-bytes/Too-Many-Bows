package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DuskReaperArrow extends AbstractArrow {
    private static final int SPECTRAL_RADIUS = 5;

    public DuskReaperArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public DuskReaperArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
                applyMarkedForDeath(target); // Mark the target for death
            }

            // Create lingering zone effects at the impact point
            createLingeringZone(this.getX(), this.getY(), this.getZ());
        }

        // Play impact sound
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);

        // Remove the arrow after hitting
        this.discard();
    }

    private void applyMarkedForDeath(LivingEntity target) {
        // Mark the target
        target.setCustomName(Component.literal("Marked for Death"));
        target.setCustomNameVisible(true);

        // Apply initial effects
        target.hurt(this.damageSources().magic(), 8.0F); // Heavy magic damage
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // Slowness
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1)); // Weakness
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // Glow for visibility

        // Add a damage-over-time effect
        target.addEffect(new MobEffectInstance(MobEffects.HARM, 100, 0)); // Constant harm
    }

    private void createLingeringZone(double x, double y, double z) {
        AABB area = new AABB(
                x - SPECTRAL_RADIUS, y - SPECTRAL_RADIUS, z - SPECTRAL_RADIUS,
                x + SPECTRAL_RADIUS, y + SPECTRAL_RADIUS, z + SPECTRAL_RADIUS
        );

        // Damage and slow entities in the zone
        this.level().getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
            if (!(entity instanceof Player && ((Player) entity).isCreative())) { // Ignore creative players
                entity.hurt(this.damageSources().magic(), 2.0F); // Light magic damage
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0)); // Slow effect
            }
        });

        // Add particle effects
        for (int i = 0; i < 20; i++) {
            this.level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.SOUL,
                    x + this.level().random.nextGaussian(),
                    y + this.level().random.nextGaussian(),
                    z + this.level().random.nextGaussian(),
                    0, 0, 0
            );
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        // Prevent the arrow from being picked up
        return ItemStack.EMPTY;
    }
}
