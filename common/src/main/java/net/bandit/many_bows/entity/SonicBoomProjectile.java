package net.bandit.many_bows.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SonicBoomProjectile extends AbstractArrow {

    private int lifetime = 60;
    private int pulseInterval = 5;
    private int tickCount = 0;

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter) {
        super(EntityType.ARROW, shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().sonicBoom(this), 20.0F); // High damage
            target.knockback(2.0F, Math.sin(this.getYRot() * Math.PI / 180.0F), -Math.cos(this.getYRot() * Math.PI / 180.0F));
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.0F);
            this.discard(); // Remove after hitting an entity
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        // Check lifetime expiration to despawn the projectile
        if (!this.level().isClientSide && --this.lifetime <= 0) {
            this.discard();
        }

        // Create particle effect pulse every pulseInterval ticks
        if (this.level().isClientSide && tickCount % pulseInterval == 0) {
            createSonicBoomPulse();
        }
    }

    // Creates a particle ring expanding outward to simulate the sonic boom effect
    private void createSonicBoomPulse() {
        int particles = 15; // Number of particles per ring
        double radius = 0.5 + (tickCount / 5.0); // Expanding radius over time

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * i / particles;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);

            // Generate particles in a ring around the arrow
            this.level().addParticle(ParticleTypes.SONIC_BOOM,
                    this.getX() + offsetX,
                    this.getY(),
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
