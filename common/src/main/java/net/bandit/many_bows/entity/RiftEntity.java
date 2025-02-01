package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RiftEntity extends Entity {
    private int lifeTime = 60; // Lifetime in ticks (3 seconds)
    private static final double PULL_RADIUS = 5.0; // Radius for pulling entities
    private static final double EXPLOSION_RADIUS = 4.0; // Radius of the explosion
    private static final double PULL_STRENGTH = 0.1; // Strength of the pull effect

    public RiftEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noPhysics = true; // Makes the entity non-collidable
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public RiftEntity(Level world, double x, double y, double z) {
        this(EntityRegistry.RIFT_ENTITY.get(), world);
        this.setPos(x, y, z);
    }

    public RiftEntity(Level world, BlockPos pos) {
        this(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5); // Center the rift in the block
    }

@Override
public void tick() {
    super.tick();

    if (this.lifeTime-- <= 0) {
        if (!this.level().isClientSide) {
            this.explode(); // Trigger the explosion
        }
        this.discard(); // Remove the entity
    } else {
        if (!this.level().isClientSide) {
            // Pull entities in
            List<Entity> entities = this.level().getEntities(this, new AABB(this.getX() - PULL_RADIUS, this.getY() - PULL_RADIUS, this.getZ() - PULL_RADIUS,
                    this.getX() + PULL_RADIUS, this.getY() + PULL_RADIUS, this.getZ() + PULL_RADIUS));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity living && !(entity instanceof Player && ((Player) entity).getAbilities().instabuild)) {
                    double dx = this.getX() - living.getX();
                    double dy = this.getY() - living.getY();
                    double dz = this.getZ() - living.getZ();
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance > 0.1) {
                        double strength = PULL_STRENGTH / distance;
                        living.push(dx * strength, dy * strength, dz * strength);
                    }
                }
            }
        }

        if (this.level().isClientSide) {
            // Core Pulsating Effect
            double corePulse = Math.sin(this.lifeTime * 0.1) * 0.5 + 1.5; // Dynamic core radius
            for (int i = 0; i < 30; i++) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                        this.getX() + this.random.nextGaussian() * corePulse,
                        this.getY() + this.random.nextGaussian() * corePulse,
                        this.getZ() + this.random.nextGaussian() * corePulse,
                        0.0, 0.0, 0.0);
            }

            // Enchanting Letters Being Pulled In
            for (int i = 0; i < 20; i++) {
                double spawnRadius = 8.0; // Letters spawn far away
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double offsetX = Math.cos(angle) * spawnRadius;
                double offsetZ = Math.sin(angle) * spawnRadius;
                double offsetY = (this.random.nextDouble() - 0.5) * 4.0;

                double velocityX = (this.getX() - (this.getX() + offsetX)) * 0.2;
                double velocityY = (this.getY() - (this.getY() + offsetY)) * 0.2;
                double velocityZ = (this.getZ() - (this.getZ() + offsetZ)) * 0.2;

                this.level().addParticle(ParticleTypes.ENCHANT,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        velocityX, velocityY, velocityZ);
            }

            // Swirling Rings
            for (int layer = 0; layer < 3; layer++) {
                double height = this.getY() + layer * 0.3;
                double radius = 2.5 + layer * 0.3; // Adjust ring radius
                for (int i = 0; i < 40; i++) {
                    double angle = (this.lifeTime * 0.1 + i) % (2 * Math.PI);
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    this.level().addParticle(ParticleTypes.END_ROD,
                            this.getX() + offsetX,
                            height,
                            this.getZ() + offsetZ,
                            0.0, 0.0, 0.0); // Static swirling particles
                }
            }

            // Occasional Time Explosion
            if (this.lifeTime % 20 == 0) {
                this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0, 0.0, 0.0);
            }
        }
    }
}

    private void explode() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), (float) EXPLOSION_RADIUS, Level.ExplosionInteraction.NONE);

    }


    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.lifeTime = compound.getInt("LifeTime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifeTime", this.lifeTime);
    }

}
