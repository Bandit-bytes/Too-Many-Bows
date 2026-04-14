package net.bandit.many_bows.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class LightOrbEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_LIGHT_LEVEL =
            SynchedEntityData.defineId(LightOrbEntity.class, EntityDataSerializers.INT);

    private static final double FOLLOW_SPEED = 0.35D;

    private UUID ownerUUID;
    private BlockPos lastLightPos;
    private int lifetime = 0;

    public LightOrbEntity(EntityType<? extends LightOrbEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIGHT_LEVEL, 12);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setLightLevel(int lightLevel) {
        this.entityData.set(DATA_LIGHT_LEVEL, Math.max(1, Math.min(15, lightLevel)));
    }

    public int getLightLevel() {
        return this.entityData.get(DATA_LIGHT_LEVEL);
    }

    public void refreshLifetime() {
        this.lifetime = 10;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (--this.lifetime <= 0) {
            cleanupLight();
            this.discard();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel) || this.ownerUUID == null) {
            cleanupLight();
            this.discard();
            return;
        }

        Player player = serverLevel.getPlayerByUUID(this.ownerUUID);
        if (player == null || !player.isAlive()) {
            cleanupLight();
            this.discard();
            return;
        }

        double targetX = player.getX();
        double targetY = player.getY() + 1.0D;
        double targetZ = player.getZ();

        double dx = targetX - this.getX();
        double dy = targetY - this.getY();
        double dz = targetZ - this.getZ();

        this.move(MoverType.SELF, this.getDeltaMovement().add(dx * FOLLOW_SPEED, dy * FOLLOW_SPEED, dz * FOLLOW_SPEED));

        BlockPos currentPos = this.blockPosition();

        if (this.lastLightPos == null || !this.lastLightPos.equals(currentPos)) {
            removeLightAtLastPos();

            BlockState currentState = this.level().getBlockState(currentPos);
            if (currentState.isAir()) {
                this.level().setBlockAndUpdate(
                        currentPos,
                        Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, this.getLightLevel())
                );
                this.lastLightPos = currentPos;
            }
        }
    }

    private void removeLightAtLastPos() {
        if (this.lastLightPos != null && this.level().getBlockState(this.lastLightPos).is(Blocks.LIGHT)) {
            this.level().setBlockAndUpdate(this.lastLightPos, Blocks.AIR.defaultBlockState());
        }
    }

    private void cleanupLight() {
        removeLightAtLastPos();
        this.lastLightPos = null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("Lifetime")) {
            this.lifetime = tag.getInt("Lifetime");
        }
        if (tag.contains("LightLevel")) {
            this.setLightLevel(tag.getInt("LightLevel"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("Lifetime", this.lifetime);
        tag.putInt("LightLevel", this.getLightLevel());
    }
}