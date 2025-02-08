package net.bandit.many_bows.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TorchbearerArrow extends AbstractArrow {

    public TorchbearerArrow(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    public TorchbearerArrow(Level level, LivingEntity shooter) {
        super(EntityType.ARROW, shooter, level);
        this.setBaseDamage(5.0);
    }
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (result instanceof EntityHitResult entityHitResult) {
            // Fire damage when hitting an entity
            LivingEntity entity = entityHitResult.getEntity() instanceof LivingEntity ? (LivingEntity) entityHitResult.getEntity() : null;
            if (entity != null) {
                entity.setSecondsOnFire(5); // Set the entity on fire for 5 seconds
            }
        } else if (result instanceof BlockHitResult blockHitResult) {
            // Get the block position and face
            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitFace = blockHitResult.getDirection();
            BlockPos placePos = hitPos.relative(hitFace);

            if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
                if (hitFace == Direction.UP) {
                    // Place a standing torch on top of the block
                    BlockPos torchPos = hitPos.above();
                    BlockState torchState = Blocks.TORCH.defaultBlockState();

                    if (torchState.canSurvive(serverLevel, torchPos)) {
                        serverLevel.setBlock(torchPos, torchState, 3);
                    }
                }
                else if (hitFace != Direction.DOWN) {
                    // Create the wall torch state
                    BlockState wallTorchState = Blocks.WALL_TORCH.defaultBlockState();

                    // Use the hit position for placement
                    BlockPos torchPos = hitPos.relative(hitFace);

                    // Get the state of the block being hit
                    BlockState hitBlockState = serverLevel.getBlockState(hitPos);

                    // Check if the block being hit is sturdy
                    boolean isFaceSturdy = hitBlockState.isFaceSturdy(serverLevel, hitPos, hitFace);

                    if (isFaceSturdy && serverLevel.getBlockState(torchPos).isAir()) {
                        // Correctly set the FACING property to point away from the hit block
                        wallTorchState = wallTorchState.setValue(WallTorchBlock.FACING, hitFace);

                        // Place the wall torch
                        serverLevel.setBlock(torchPos, wallTorchState, 3);
                    }
                }
            }
        }
        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
