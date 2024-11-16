package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
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
    private int tickCount = 0;

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().sonicBoom(this), 20.0F); // Adjusted damage
            target.knockback(2.0F, Math.sin(this.getYRot() * Math.PI / 180.0F), -Math.cos(this.getYRot() * Math.PI / 180.0F));
            level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.0F);
            this.discard(); // Remove after hitting an entity
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        // Server-side lifetime control
        if (!this.level().isClientSide && --this.lifetime <= 0) {
            this.discard();
        }

        if (this.level().isClientSide) {
            createSonicBoomSpiral();
        }
    }
    // Enhanced particle effect for warden-like sonic boom
    private void createSonicBoomSpiral() {
        int particles = 25;
        double radius = 0.5;
        double spiralExpansionRate = 0.15;

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * (i + tickCount * 0.1);
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = tickCount * 0.05 - (i * 0.01);

            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D);

            radius += spiralExpansionRate;
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
