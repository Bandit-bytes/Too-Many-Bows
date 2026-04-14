package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SoulhoardBowConfig;
import net.bandit.many_bows.item.SoulhoardBow;
import net.bandit.many_bows.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class HoardedSkullEntity extends Entity {

    private static final String CONFIG_NAME = "soulhoard_bow";

    private static final EntityDataAccessor<Integer> DATA_HOVER_DELAY =
            SynchedEntityData.defineId(HoardedSkullEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(HoardedSkullEntity.class, EntityDataSerializers.FLOAT);

    private UUID ownerUUID;
    private UUID preferredTargetUUID;
    private UUID sourceBowId;

    private int lifetimeRemaining = 30;
    private float damage = 2.0F;
    private double seekRadius = 10.0D;
    private double speed = 0.55D;
    private double steering = 0.25D;
    private int burnSeconds = 2;
    private int hitsRemaining = 3;

    public HoardedSkullEntity(EntityType<? extends HoardedSkullEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    private static SoulhoardBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SoulhoardBowConfig.class, SoulhoardBowConfig::new);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void setPreferredTargetUUID(UUID preferredTargetUUID) {
        this.preferredTargetUUID = preferredTargetUUID;
    }

    public void setSourceBowId(UUID sourceBowId) {
        this.sourceBowId = sourceBowId;
    }

    public void setLifetime(int ticks) {
        this.lifetimeRemaining = Math.max(1, ticks);
    }

    public void setHitsRemaining(int hitsRemaining) {
        this.hitsRemaining = Math.max(1, hitsRemaining);
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0.0F, damage);
    }

    public void setSeekRadius(double seekRadius) {
        this.seekRadius = Math.max(0.0D, seekRadius);
    }

    public void setSpeed(double speed) {
        this.speed = Math.max(0.01D, speed);
    }

    public void setSteering(double steering) {
        this.steering = Math.max(0.01D, Math.min(1.0D, steering));
    }

    public void setBurnSeconds(int burnSeconds) {
        this.burnSeconds = Math.max(0, burnSeconds);
    }

    public void setHoverDelay(int hoverDelay) {
        this.entityData.set(DATA_HOVER_DELAY, Math.max(0, hoverDelay));
    }

    public int getHoverDelay() {
        return this.entityData.get(DATA_HOVER_DELAY);
    }

    public void setRenderScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.1F, scale));
    }

    public float getRenderScale() {
        return this.entityData.get(DATA_SCALE);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        if (--this.lifetimeRemaining <= 0) {
            this.discard();
            return;
        }

        int hoverDelay = this.getHoverDelay();
        if (hoverDelay > 0) {
            this.entityData.set(DATA_HOVER_DELAY, hoverDelay - 1);

            Vec3 drift = this.getDeltaMovement().scale(0.72D);
            double swayX = (this.random.nextDouble() - 0.5D) * 0.02D;
            double swayZ = (this.random.nextDouble() - 0.5D) * 0.02D;
            Vec3 hoverMotion = drift.add(swayX, 0.010D, swayZ);

            this.setDeltaMovement(hoverMotion);
            this.move(MoverType.SELF, hoverMotion);

            if (hoverDelay == 1) {
                LivingEntity launchTarget = getResolvedTarget();
                if (launchTarget == null) {
                    launchTarget = findNearestTarget();
                }
                if (launchTarget != null) {
                    burstToward(launchTarget);
                }
            }
            return;
        }

        LivingEntity target = getResolvedTarget();
        if (target == null) {
            target = findNearestTarget();
        }

        if (target != null) {
            steerToward(target);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.96D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        tryHitTarget();
    }

    private void spawnClientParticles() {
        Vec3 motion = this.getDeltaMovement();
        double speedNow = motion.length();

        if (speedNow > 0.01D) {
            double nx = -motion.x / speedNow;
            double ny = -motion.y / speedNow;
            double nz = -motion.z / speedNow;

            for (int i = 0; i < 2; i++) {
                double back = 0.10D * i;
                double px = this.getX() + nx * back + (this.random.nextDouble() - 0.5D) * 0.025D;
                double py = this.getY() + 0.03D + ny * back + (this.random.nextDouble() - 0.5D) * 0.025D;
                double pz = this.getZ() + nz * back + (this.random.nextDouble() - 0.5D) * 0.025D;

                level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        px,
                        py,
                        pz,
                        nx * 0.01D,
                        ny * 0.01D,
                        nz * 0.01D
                );
            }
        } else if (this.tickCount % 2 == 0) {
            level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(),
                    this.getY() + 0.04D,
                    this.getZ(),
                    0.0D,
                    0.005D,
                    0.0D
            );
        }

        if (this.random.nextInt(3) == 0) {
            level().addParticle(
                    ParticleTypes.SOUL,
                    this.getX(),
                    this.getY() + 0.02D,
                    this.getZ(),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private LivingEntity getResolvedTarget() {
        if (this.preferredTargetUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(this.preferredTargetUUID);
        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }

        return null;
    }

    private LivingEntity getOwnerEntity() {
        if (this.ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(this.ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private LivingEntity findNearestTarget() {
        AABB searchBox = this.getBoundingBox().inflate(this.seekRadius);
        LivingEntity owner = getOwnerEntity();

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.isAlive() && entity != owner
        );

        return candidates.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                .orElse(null);
    }

    private Vec3 getAimPoint(LivingEntity target) {
        return target.position().add(0.0D, Math.min(0.95D, target.getBbHeight() * 0.60D), 0.0D);
    }

    private void burstToward(LivingEntity target) {
        Vec3 burst = getAimPoint(target)
                .subtract(this.position())
                .normalize()
                .scale(this.speed * 1.45D);

        this.setDeltaMovement(burst);
    }

    private void steerToward(LivingEntity target) {
        Vec3 current = this.getDeltaMovement();
        Vec3 toTarget = getAimPoint(target).subtract(this.position());

        if (toTarget.lengthSqr() < 1.0E-6D) {
            return;
        }

        double distance = toTarget.length();
        double speedScale = distance < 1.4D ? 1.55D : distance < 2.8D ? 1.25D : 1.0D;
        double maxSpeed = this.speed * speedScale;

        Vec3 desired = toTarget.normalize().scale(maxSpeed);

        if (current.lengthSqr() < 1.0E-4D) {
            current = desired;
        }

        double steerStrength = this.steering;
        if (distance < 2.0D) {
            steerStrength = Math.min(0.90D, steerStrength + 0.30D);
        } else if (distance < 4.0D) {
            steerStrength = Math.min(0.75D, steerStrength + 0.15D);
        }

        Vec3 next = current.scale(1.0D - steerStrength).add(desired.scale(steerStrength));

        if (next.lengthSqr() > (maxSpeed * maxSpeed)) {
            next = next.normalize().scale(maxSpeed);
        }

        this.setDeltaMovement(next);
    }

    private void tryHitTarget() {
        LivingEntity owner = getOwnerEntity();

        List<LivingEntity> hits = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(0.40D),
                entity -> entity.isAlive() && entity != owner
        );

        if (hits.isEmpty()) {
            return;
        }

        LivingEntity target = hits.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this)))
                .orElse(null);

        if (target == null) {
            return;
        }

        boolean wasAliveBefore = target.isAlive();

        target.hurt(this.damageSources().magic(), this.damage);

        if (this.burnSeconds > 0) {
            target.igniteForSeconds(this.burnSeconds);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.55D,
                    target.getZ(),
                    10,
                    0.12D,
                    0.14D,
                    0.12D,
                    0.012D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.55D,
                    target.getZ(),
                    6,
                    0.10D,
                    0.12D,
                    0.10D,
                    0.006D
            );
        }

        boolean killed = wasAliveBefore && !target.isAlive();

        if (killed && owner instanceof Player player && this.sourceBowId != null) {
            SoulhoardBowConfig config = config();

            int soulsToAward = Math.max(1, config.souls_gained_per_kill + config.skull_bonus_souls_on_kill);

            boolean awarded = SoulhoardBow.awardSoulsToMatchingBow(
                    player,
                    this.sourceBowId,
                    soulsToAward
            );

            if (awarded && config.play_harvest_sound) {
                this.level().playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundRegistry.SOULHOARD_HARVEST.get(),
                        SoundSource.PLAYERS,
                        config.harvest_sound_volume,
                        config.harvest_sound_pitch
                );
            }
        }

        this.hitsRemaining--;

        if (this.hitsRemaining <= 0) {
            this.discard();
            return;
        }

        SoulhoardBowConfig config = config();

        if (config.skull_retarget_after_hit) {
            this.preferredTargetUUID = null;

            LivingEntity nextTarget = findNearestTarget();
            if (nextTarget != null) {
                double speedMultiplier = killed
                        ? config.skull_kill_speed_multiplier
                        : config.skull_post_hit_speed_multiplier;

                Vec3 burst = getAimPoint(nextTarget)
                        .subtract(this.position())
                        .normalize()
                        .scale(this.speed * speedMultiplier);

                this.setDeltaMovement(burst);
                this.preferredTargetUUID = nextTarget.getUUID();
                return;
            }
        }

        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }

        if (tag.hasUUID("PreferredTarget")) {
            this.preferredTargetUUID = tag.getUUID("PreferredTarget");
        }

        if (tag.hasUUID("SourceBowId")) {
            this.sourceBowId = tag.getUUID("SourceBowId");
        }

        this.lifetimeRemaining = tag.getInt("LifetimeRemaining");
        this.damage = tag.getFloat("Damage");
        this.seekRadius = tag.getDouble("SeekRadius");
        this.speed = tag.getDouble("Speed");
        this.steering = tag.getDouble("Steering");
        this.burnSeconds = tag.getInt("BurnSeconds");
        this.entityData.set(DATA_HOVER_DELAY, tag.getInt("HoverDelay"));
        this.entityData.set(DATA_SCALE, tag.getFloat("RenderScale"));
        this.hitsRemaining = Math.max(1, tag.getInt("HitsRemaining"));


    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }

        if (this.preferredTargetUUID != null) {
            tag.putUUID("PreferredTarget", this.preferredTargetUUID);
        }

        if (this.sourceBowId != null) {
            tag.putUUID("SourceBowId", this.sourceBowId);
        }

        tag.putInt("LifetimeRemaining", this.lifetimeRemaining);
        tag.putFloat("Damage", this.damage);
        tag.putDouble("SeekRadius", this.seekRadius);
        tag.putDouble("Speed", this.speed);
        tag.putDouble("Steering", this.steering);
        tag.putInt("BurnSeconds", this.burnSeconds);
        tag.putInt("HoverDelay", this.getHoverDelay());
        tag.putFloat("RenderScale", this.getRenderScale());
        tag.putInt("HitsRemaining", this.hitsRemaining);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HOVER_DELAY, 0);
        builder.define(DATA_SCALE, 0.7F);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}