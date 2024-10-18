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

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter) {
        super(EntityType.ARROW, shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().sonicBoom(this), 20.0F); // Increased damage
            target.knockback(2.0F, Math.sin(this.getYRot() * Math.PI / 180.0F), -Math.cos(this.getYRot() * Math.PI / 180.0F)); // Stronger knockback
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 20; ++i) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 0.1D;
                double offsetY = (this.random.nextDouble() - 0.5D) * 0.1D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 0.1D;
                this.level().addParticle(ParticleTypes.SONIC_BOOM,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0.0D, 0.0D, 0.0D);
            }
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
