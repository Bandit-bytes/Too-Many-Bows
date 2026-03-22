package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.TorchbearerBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    private static final String CONFIG_NAME = "torchbearer";

    public TorchbearerArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public TorchbearerArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TORCHBEARER_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static TorchbearerBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, TorchbearerBowConfig.class, TorchbearerBowConfig::new);
    }

    private void applyConfiguredValues() {
        TorchbearerBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        TorchbearerBowConfig config = config();
        boolean actionSucceeded = false;

        if (result instanceof EntityHitResult entityHitResult) {
            LivingEntity entity = entityHitResult.getEntity() instanceof LivingEntity living ? living : null;
            if (entity != null && config.ignite_entities_on_hit) {
                entity.setRemainingFireTicks(config.entity_fire_ticks);
                actionSucceeded = true;
            }
        } else if (result instanceof BlockHitResult blockHitResult) {
            if (!level().isClientSide && level() instanceof ServerLevel serverLevel && config.place_torch_on_block_hit) {
                BlockPos hitPos = blockHitResult.getBlockPos();
                Direction hitFace = blockHitResult.getDirection();

                if (hitFace == Direction.UP && config.place_standing_torch_on_top_hit) {
                    BlockPos torchPos = hitPos.above();
                    BlockState torchState = Blocks.TORCH.defaultBlockState();

                    if (torchState.canSurvive(serverLevel, torchPos)) {
                        serverLevel.setBlock(torchPos, torchState, 3);
                        actionSucceeded = true;
                    }
                } else if (hitFace != Direction.DOWN && config.place_wall_torch_on_side_hit) {
                    BlockPos torchPos = hitPos.relative(hitFace);
                    BlockState hitBlockState = serverLevel.getBlockState(hitPos);
                    boolean isFaceSturdy = hitBlockState.isFaceSturdy(serverLevel, hitPos, hitFace);
                    boolean canPlaceInTarget = !config.require_air_for_wall_torch || serverLevel.getBlockState(torchPos).isAir();

                    if (isFaceSturdy && canPlaceInTarget) {
                        BlockState wallTorchState = Blocks.WALL_TORCH.defaultBlockState()
                                .setValue(WallTorchBlock.FACING, hitFace);

                        serverLevel.setBlock(torchPos, wallTorchState, 3);
                        actionSucceeded = true;
                    }
                }
            }
        }

        if ((config.discard_only_when_action_succeeds && actionSucceeded)
                || (!config.discard_only_when_action_succeeds && result.getType() != HitResult.Type.MISS)) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}