package net.bandit.many_bows.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class GravewireMarkEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_INITIAL_LIFETIME =
            SynchedEntityData.defineId(GravewireMarkEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_Y_OFFSET =
            SynchedEntityData.defineId(GravewireMarkEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_RISE_AMOUNT =
            SynchedEntityData.defineId(GravewireMarkEntity.class, EntityDataSerializers.FLOAT);

    private UUID targetUUID;
    private int lifetimeRemaining = 40;

    public GravewireMarkEntity(EntityType<? extends GravewireMarkEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public void setTargetUUID(UUID uuid) {
        this.targetUUID = uuid;
    }

    public void setLifetime(int ticks) {
        this.lifetimeRemaining = ticks;
        this.entityData.set(DATA_INITIAL_LIFETIME, ticks);
    }

    public void setYOffset(double offset) {
        this.entityData.set(DATA_Y_OFFSET, (float) offset);
    }

    public void setRiseAmount(double amount) {
        this.entityData.set(DATA_RISE_AMOUNT, (float) amount);
    }

    public int getInitialLifetime() {
        return this.entityData.get(DATA_INITIAL_LIFETIME);
    }

    public float getYOffset() {
        return this.entityData.get(DATA_Y_OFFSET);
    }

    public float getRiseAmount() {
        return this.entityData.get(DATA_RISE_AMOUNT);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (--this.lifetimeRemaining <= 0) {
            this.discard();
            return;
        }

        if (this.targetUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        Entity entity = serverLevel.getEntity(this.targetUUID);
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            this.discard();
            return;
        }

        int initialLifetime = Math.max(1, this.getInitialLifetime());
        float progress = 1.0F - (this.lifetimeRemaining / (float) initialLifetime);
        double rise = this.getRiseAmount() * progress;

        double x = living.getX();
        double y = living.getY() + this.getYOffset() + rise;
        double z = living.getZ();

        this.setPos(x, y, z);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        this.lifetimeRemaining = in.getInt("LifetimeRemaining").orElse(this.lifetimeRemaining);

        this.entityData.set(
                DATA_INITIAL_LIFETIME,
                in.getInt("InitialLifetime").orElse(this.getInitialLifetime())
        );

        this.entityData.set(
                DATA_Y_OFFSET,
                in.getFloatOr("YOffset", this.getYOffset())
        );

        this.entityData.set(
                DATA_RISE_AMOUNT,
                in.getFloatOr("RiseAmount", this.getRiseAmount())
        );
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("LifetimeRemaining", this.lifetimeRemaining);
        out.putInt("InitialLifetime", this.getInitialLifetime());
        out.putFloat("YOffset", this.getYOffset());
        out.putFloat("RiseAmount", this.getRiseAmount());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_INITIAL_LIFETIME, 40);
        builder.define(DATA_Y_OFFSET, 1.35F);
        builder.define(DATA_RISE_AMOUNT, 0.85F);
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
