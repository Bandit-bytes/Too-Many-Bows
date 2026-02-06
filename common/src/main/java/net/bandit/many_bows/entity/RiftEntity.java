package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RiftEntity extends Entity {
    private int lifeTime = 60;
    private static final double PULL_RADIUS = 5.0;
    private static final double EXPLOSION_RADIUS = 4.0;
    private static final double PULL_STRENGTH = 0.1;

    private LivingEntity owner;

    public RiftEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noPhysics = true;
    }
    public RiftEntity(Level world, double x, double y, double z) {
        this(EntityRegistry.RIFT_ENTITY.get(), world);
        this.setPos(x, y, z);
    }

    public RiftEntity(Level world, LivingEntity owner, BlockPos pos) {
        this(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        this.owner = owner;
    }

@Override
public void tick() {
    super.tick();

    if (this.lifeTime-- <= 0) {
        if (!this.level().isClientSide()) {
            this.explode();
        }
        this.discard();
    } else {
        if (!this.level().isClientSide()) {
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

        if (this.level().isClientSide()) {
            double corePulse = Math.sin(this.lifeTime * 0.1) * 0.5 + 1.5;
            for (int i = 0; i < 30; i++) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
                        this.getX() + this.random.nextGaussian() * corePulse,
                        this.getY() + this.random.nextGaussian() * corePulse,
                        this.getZ() + this.random.nextGaussian() * corePulse,
                        0.0, 0.0, 0.0);
            }

            for (int i = 0; i < 20; i++) {
                double spawnRadius = 8.0;
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

            for (int layer = 0; layer < 3; layer++) {
                double height = this.getY() + layer * 0.3;
                double radius = 2.5 + layer * 0.3;
                for (int i = 0; i < 40; i++) {
                    double angle = (this.lifeTime * 0.1 + i) % (2 * Math.PI);
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    this.level().addParticle(ParticleTypes.END_ROD,
                            this.getX() + offsetX,
                            height,
                            this.getZ() + offsetZ,
                            0.0, 0.0, 0.0);
                }
            }
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

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        this.lifeTime = in.getInt("LifeTime").orElse(60);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("LifeTime", this.lifeTime);
    }

    private void explode() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

        float baseDamage = 8.0f;

        if (owner instanceof Player player) {
            var dmgInst = player.getAttribute(net.bandit.many_bows.registry.AttributesRegistry.BOW_DAMAGE);
            if (dmgInst != null) baseDamage *= (float) dmgInst.getValue();

            var critInst = player.getAttribute(net.bandit.many_bows.registry.AttributesRegistry.BOW_CRIT_CHANCE);
            if (critInst != null && player.getRandom().nextDouble() < critInst.getValue()) {
                baseDamage *= 1.5F;
            }
        }

        List<LivingEntity> entities = level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(getX() - EXPLOSION_RADIUS, getY() - EXPLOSION_RADIUS, getZ() - EXPLOSION_RADIUS,
                        getX() + EXPLOSION_RADIUS, getY() + EXPLOSION_RADIUS, getZ() + EXPLOSION_RADIUS),
                e -> e.isAlive() && !(e instanceof Player p && p.getAbilities().instabuild)
        );

        var source = (owner != null)
                ? level().damageSources().mobAttack(owner)
                : level().damageSources().generic();

        for (LivingEntity target : entities) {
            target.hurt(source, baseDamage);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
