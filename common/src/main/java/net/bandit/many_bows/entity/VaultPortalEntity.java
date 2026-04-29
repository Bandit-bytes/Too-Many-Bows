package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VaultpiercerBowConfig;
import net.bandit.many_bows.registry.ItemRegistry;
import net.bandit.many_bows.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class VaultPortalEntity extends Entity {

    private static final String CONFIG_NAME = "vaultpiercer";
    private static final int MAX_FIRE_ANIM_TICKS = 5;

    private static final EntityDataAccessor<Integer> COLOR_PHASE =
            SynchedEntityData.defineId(VaultPortalEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID =
            SynchedEntityData.defineId(VaultPortalEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> FIRE_ANIM_TICKS =
            SynchedEntityData.defineId(VaultPortalEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private UUID targetUUID;

    private double offsetX;
    private double offsetY;
    private double offsetZ;

    private int warmupTicks = 10;
    private int lifetimeTicks = 50;
    private boolean trackTarget = true;
    private float powerMultiplier = 1.0F;
    private int portalIndex = 0;

    private boolean hasFired = false;
    private int fireAnimTicks = 0;

    public VaultPortalEntity(EntityType<? extends VaultPortalEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    private static VaultpiercerBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, VaultpiercerBowConfig.class, VaultpiercerBowConfig::new);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;

        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && targetUUID != null) {
            Entity entity = serverLevel.getEntity(targetUUID);
            if (entity != null) {
                this.entityData.set(TARGET_ENTITY_ID, entity.getId());
            }
        }
    }

    public void setOffset(double offsetX, double offsetY, double offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public void setWarmupTicks(int warmupTicks) {
        this.warmupTicks = warmupTicks;
    }

    public void setLifetimeTicks(int lifetimeTicks) {
        this.lifetimeTicks = lifetimeTicks;
    }

    public void setTrackTarget(boolean trackTarget) {
        this.trackTarget = trackTarget;
    }

    public void setPowerMultiplier(float powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    public void setPortalIndex(int portalIndex) {
        this.portalIndex = portalIndex;
        this.entityData.set(COLOR_PHASE, portalIndex);
    }

    public int getWarmupTicks() {
        return this.warmupTicks;
    }

    public int getFireAnimTicks() {
        return this.entityData.get(FIRE_ANIM_TICKS);
    }

    public boolean isFiring() {
        return this.entityData.get(FIRE_ANIM_TICKS) > 0;
    }

    public int getPortalIndex() {
        return this.portalIndex;
    }

    public Entity getClientTarget() {
        int id = this.entityData.get(TARGET_ENTITY_ID);
        if (id < 0) {
            return null;
        }

        return this.level().getEntity(id);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR_PHASE, 0);
        builder.define(TARGET_ENTITY_ID, -1);
        builder.define(FIRE_ANIM_TICKS, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        if (this.ownerUUID != null) {
            out.putString("Owner", this.ownerUUID.toString());
        }

        if (this.targetUUID != null) {
            out.putString("Target", this.targetUUID.toString());
        }

        out.putDouble("OffsetX", this.offsetX);
        out.putDouble("OffsetY", this.offsetY);
        out.putDouble("OffsetZ", this.offsetZ);
        out.putInt("WarmupTicks", this.warmupTicks);
        out.putInt("LifetimeTicks", this.lifetimeTicks);
        out.putBoolean("TrackTarget", this.trackTarget);
        out.putFloat("PowerMultiplier", this.powerMultiplier);
        out.putInt("PortalIndex", this.portalIndex);
        out.putBoolean("HasFired", this.hasFired);
        out.putInt("FireAnimTicks", this.fireAnimTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        String owner = in.getString("Owner").orElse("");
        if (!owner.isBlank()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
                this.ownerUUID = null;
            }
        }

        String target = in.getString("Target").orElse("");
        if (!target.isBlank()) {
            try {
                this.targetUUID = UUID.fromString(target);
            } catch (IllegalArgumentException ignored) {
                this.targetUUID = null;
            }
        }

        this.offsetX = in.getDoubleOr("OffsetX", this.offsetX);
        this.offsetY = in.getDoubleOr("OffsetY", this.offsetY);
        this.offsetZ = in.getDoubleOr("OffsetZ", this.offsetZ);

        this.warmupTicks = in.getInt("WarmupTicks").orElse(this.warmupTicks);
        this.lifetimeTicks = in.getInt("LifetimeTicks").orElse(this.lifetimeTicks);
        this.trackTarget = in.getBooleanOr("TrackTarget", this.trackTarget);
        this.powerMultiplier = in.getFloatOr("PowerMultiplier", this.powerMultiplier);
        this.portalIndex = in.getInt("PortalIndex").orElse(this.portalIndex);
        this.hasFired = in.getBooleanOr("HasFired", this.hasFired);
        this.fireAnimTicks = in.getInt("FireAnimTicks").orElse(this.fireAnimTicks);

        this.entityData.set(COLOR_PHASE, this.portalIndex);
        this.entityData.set(FIRE_ANIM_TICKS, this.fireAnimTicks);
    }

    @Override
    public void tick() {
        super.tick();

        VaultpiercerBowConfig config = config();

        if (this.level().isClientSide()) {
            if (this.warmupTicks > 0) {
                this.warmupTicks--;
            }

            int clientFireTicks = this.entityData.get(FIRE_ANIM_TICKS);
            if (clientFireTicks > 0) {
                this.entityData.set(FIRE_ANIM_TICKS, clientFireTicks - 1);
            }

            if (this.lifetimeTicks > 0) {
                this.lifetimeTicks--;
            }

            tickClientParticles(config);
            return;
        }

        LivingEntity target = resolveTarget(config);

        if (!this.hasFired && this.trackTarget && target != null && target.isAlive()) {
            double bob = Math.sin((this.tickCount + (this.portalIndex * 3)) * 0.25D) * config.portal_vertical_bob;

            this.setPos(
                    target.getX() + this.offsetX,
                    target.getY() + this.offsetY + bob,
                    target.getZ() + this.offsetZ
            );

            this.entityData.set(TARGET_ENTITY_ID, target.getId());
        }

        if (!this.hasFired) {
            if (--this.warmupTicks <= 0) {
                fireFollowUpArrow(config);
                this.hasFired = true;
                this.fireAnimTicks = MAX_FIRE_ANIM_TICKS;
                this.entityData.set(FIRE_ANIM_TICKS, MAX_FIRE_ANIM_TICKS);
                return;
            }

            if (--this.lifetimeTicks <= 0) {
                this.discard();
            }

            return;
        }

        if (this.fireAnimTicks > 0) {
            this.fireAnimTicks--;
            this.entityData.set(FIRE_ANIM_TICKS, this.fireAnimTicks);
        } else {
            this.discard();
        }
    }

    private void tickClientParticles(VaultpiercerBowConfig config) {
        if (!config.portal_particles_enabled) {
            return;
        }

        int phase = this.entityData.get(COLOR_PHASE);

        for (int i = 0; i < Math.max(1, config.portal_particle_count / 2); i++) {
            double angle = ((Math.PI * 2.0D) / Math.max(4, config.portal_particle_count)) * i
                    + (this.tickCount * 0.12D);

            double radius = 0.35D + (phase % 2) * 0.04D;
            double x = this.getX() + Math.cos(angle) * radius;
            double y = this.getY() + (Math.sin((this.tickCount + i) * 0.18D) * 0.04D);
            double z = this.getZ() + Math.sin(angle) * radius;

            this.level().addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.01D, 0.0D);

            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }

        this.level().addParticle(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY(),
                this.getZ(),
                0.0D,
                0.0D,
                0.0D
        );
    }

    private LivingEntity resolveOwner() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.ownerUUID == null) {
            return null;
        }

        Entity entity = serverLevel.getEntity(this.ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private LivingEntity resolveTarget(VaultpiercerBowConfig config) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (this.targetUUID != null) {
            Entity entity = serverLevel.getEntity(this.targetUUID);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                return living;
            }
        }

        if (!config.retarget_if_target_dies) {
            return null;
        }

        LivingEntity owner = resolveOwner();

        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(config.retarget_radius),
                living -> living.isAlive() && living != owner
        );

        return nearby.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private void fireFollowUpArrow(VaultpiercerBowConfig config) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity owner = resolveOwner();
        LivingEntity target = resolveTarget(config);

        if (owner == null || target == null) {
            return;
        }

        VaultpiercerArrow arrow = new VaultpiercerArrow(
                serverLevel,
                owner,
                new ItemStack(ItemRegistry.VAULTPIERCER.get()),
                new ItemStack(Items.ARROW)
        );

        arrow.setPos(this.getX(), this.getY(), this.getZ());
        arrow.setYRot(this.getYRot());
        arrow.setXRot(this.getXRot());

        arrow.setFollowUpProjectile(true);
        arrow.setPortalStrikeEnabled(false);
        arrow.setPowerMultiplier(this.powerMultiplier);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setCritArrow(false);

        double damage = resolveScaledDamage(
                serverLevel,
                owner,
                config.follow_up_damage_base,
                config.use_ranged_damage_attribute_for_follow_up_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.follow_up_damage_attribute_divisor
        );

        if (config.follow_up_damage_scales_with_power_multiplier) {
            damage *= this.powerMultiplier;
        }

        arrow.setBaseDamage(damage);

        if (config.follow_up_homing_enabled) {
            arrow.setHomingTarget(target.getUUID(), config.follow_up_homing_ticks);
        }

        if (config.follow_up_no_gravity) {
            arrow.setNoGravity(true);
        }

        Vec3 aimPos = target.getBoundingBox().getCenter().add(0.0D, 0.15D, 0.0D);
        Vec3 diff = aimPos.subtract(this.position());

        arrow.shoot(
                diff.x,
                diff.y,
                diff.z,
                config.follow_up_projectile_velocity,
                0.0F
        );

        serverLevel.addFreshEntity(arrow);

        if (config.portal_fire_sound_enabled) {
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundRegistry.VAULT_PORTAL_FIRE.get(),
                    SoundSource.PLAYERS,
                    config.portal_fire_sound_volume,
                    config.portal_fire_sound_pitch
            );
        }

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY(),
                this.getZ(),
                10,
                0.08D,
                0.08D,
                0.08D,
                0.01D
        );

        serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(),
                this.getY(),
                this.getZ(),
                8,
                0.04D,
                0.04D,
                0.04D,
                0.0D
        );
    }

    private double resolveScaledDamage(
            ServerLevel level,
            LivingEntity shooter,
            double fallback,
            boolean useAttributeScaling,
            String attributeNamespace,
            String attributePath,
            double divisor
    ) {
        if (!useAttributeScaling || shooter == null) {
            return fallback;
        }

        var registry = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var rangedAttrHolder = registry.get(
                Identifier.fromNamespaceAndPath(attributeNamespace, attributePath)
        ).orElse(null);

        if (rangedAttrHolder == null) {
            return fallback;
        }

        var attrInstance = shooter.getAttribute(rangedAttrHolder);
        if (attrInstance == null || divisor == 0.0D) {
            return fallback;
        }

        return attrInstance.getValue() / divisor;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}