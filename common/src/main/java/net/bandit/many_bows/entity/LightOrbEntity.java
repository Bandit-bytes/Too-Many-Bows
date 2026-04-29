package net.bandit.many_bows.entity;

import net.bandit.many_bows.item.TorchbearerBow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LightOrbEntity extends Entity {

    private static final double FOLLOW_DISTANCE = 1.5D;
    private static final int DEFAULT_LIFETIME = 40;

    private BlockPos lastLightPos = null;

    private UUID ownerUUID;
    private int lightLevel = 15;
    private int lifetime = DEFAULT_LIFETIME;

    public LightOrbEntity(EntityType<? extends LightOrbEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvisible(true);
        this.setNoGravity(true);
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = Mth.clamp(lightLevel, 0, 15);
    }

    public void refreshLifetime() {
        this.lifetime = DEFAULT_LIFETIME;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced data needed. This entity is server-controlled and invisible.
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            discardAndCleanup();
            return;
        }

        Player owner = getOwnerPlayer(serverLevel);

        if (owner == null) {
            // Fallback for old Torchbearer behavior if this orb was spawned without an owner UUID.
            owner = this.level().getNearestPlayer(this, 5.0D);

            if (owner == null || !isPlayerHoldingTorchBow(owner)) {
                discardAndCleanup();
                return;
            }
        }

        if (--this.lifetime <= 0) {
            discardAndCleanup();
            return;
        }

        followOwner(owner);
        updateLightBlock();
    }

    private Player getOwnerPlayer(ServerLevel serverLevel) {
        if (this.ownerUUID == null) {
            return null;
        }

        Entity entity = serverLevel.getEntity(this.ownerUUID);
        return entity instanceof Player player ? player : null;
    }

    private void followOwner(Player player) {
        double targetX = player.getX();
        double targetY = player.getY() + FOLLOW_DISTANCE;
        double targetZ = player.getZ();

        double dx = targetX - this.getX();
        double dy = targetY - this.getY();
        double dz = targetZ - this.getZ();

        Vec3 motion = this.getDeltaMovement()
                .add(dx * 0.20D, dy * 0.20D, dz * 0.20D)
                .scale(0.85D);

        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
    }

    private void updateLightBlock() {
        if (this.lightLevel <= 0) {
            cleanupLightBlock();
            return;
        }

        BlockPos currentPos = this.blockPosition();

        if (this.lastLightPos != null && !this.lastLightPos.equals(currentPos)) {
            cleanupLightBlock();
        }

        BlockState currentState = this.level().getBlockState(currentPos);

        if (currentState.isAir() || currentState.is(Blocks.LIGHT)) {
            BlockState lightState = Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, Mth.clamp(this.lightLevel, 0, 15));

            this.level().setBlockAndUpdate(currentPos, lightState);
            this.lastLightPos = currentPos;
        }
    }

    private void cleanupLightBlock() {
        if (this.lastLightPos == null) {
            return;
        }

        if (this.level().getBlockState(this.lastLightPos).is(Blocks.LIGHT)) {
            this.level().setBlockAndUpdate(this.lastLightPos, Blocks.AIR.defaultBlockState());
        }

        this.lastLightPos = null;
    }

    private void discardAndCleanup() {
        cleanupLightBlock();
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        String owner = in.getString("OwnerUUID").orElse("");

        if (!owner.isBlank()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
                this.ownerUUID = null;
            }
        }

        this.lightLevel = Mth.clamp(in.getInt("LightLevel").orElse(this.lightLevel), 0, 15);
        this.lifetime = in.getInt("Lifetime").orElse(this.lifetime);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        if (this.ownerUUID != null) {
            out.putString("OwnerUUID", this.ownerUUID.toString());
        }

        out.putInt("LightLevel", this.lightLevel);
        out.putInt("Lifetime", this.lifetime);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    private boolean isPlayerHoldingTorchBow(Player player) {
        return player.getMainHandItem().getItem() instanceof TorchbearerBow
                || player.getOffhandItem().getItem() instanceof TorchbearerBow;
    }
}