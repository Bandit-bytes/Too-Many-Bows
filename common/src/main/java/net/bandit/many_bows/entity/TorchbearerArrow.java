package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
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

    public TorchbearerArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }


    public TorchbearerArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TORCHBEARER_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(5);
    }
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        boolean shouldDiscard = false;

        if (result instanceof EntityHitResult entityHitResult) {
            LivingEntity entity = entityHitResult.getEntity() instanceof LivingEntity ? (LivingEntity) entityHitResult.getEntity() : null;
            if (entity != null) {
                entity.setRemainingFireTicks(120);
                shouldDiscard = true;
            }
        } else if (result instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitFace = blockHitResult.getDirection();
            BlockPos placePos = hitPos.relative(hitFace);

            if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                if (hitFace == Direction.UP) {
                    BlockPos torchPos = hitPos.above();
                    BlockState torchState = Blocks.TORCH.defaultBlockState();

                    if (torchState.canSurvive(serverLevel, torchPos)) {
                        serverLevel.setBlock(torchPos, torchState, 3);
                        shouldDiscard = true;
                    }
                }
                else if (hitFace != Direction.DOWN) {
                    BlockState wallTorchState = Blocks.WALL_TORCH.defaultBlockState();
                    BlockPos torchPos = hitPos.relative(hitFace);
                    BlockState hitBlockState = serverLevel.getBlockState(hitPos);

                    boolean isFaceSturdy = hitBlockState.isFaceSturdy(serverLevel, hitPos, hitFace);

                    if (isFaceSturdy && serverLevel.getBlockState(torchPos).isAir()) {
                        wallTorchState = wallTorchState.setValue(WallTorchBlock.FACING, hitFace);

                        serverLevel.setBlock(torchPos, wallTorchState, 3);
                        shouldDiscard = true;
                    }
                }
            }
        }

        if (shouldDiscard) {
            this.discard();
        }
    }


    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }

}
