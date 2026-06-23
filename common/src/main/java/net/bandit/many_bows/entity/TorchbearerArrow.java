package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.TorchbearerBowConfig;
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
import org.jetbrains.annotations.NotNull;

public class TorchbearerArrow extends AbstractArrow {

    private int lifetime = 0;

    public TorchbearerArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public TorchbearerArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TORCHBEARER_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private TorchbearerBowConfig config() {
        return TorchbearerBowConfig.get();
    }

    private void applyConfigValues() {
        TorchbearerBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > config().max_lifetime_ticks) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        boolean shouldDiscard = false;
        TorchbearerBowConfig config = config();

        if (result instanceof EntityHitResult entityHitResult) {
            LivingEntity entity = entityHitResult.getEntity() instanceof LivingEntity living ? living : null;
            if (entity != null && config.ignite_entities_on_hit) {
                entity.setRemainingFireTicks(config.entity_fire_ticks);
                shouldDiscard = true;
            }
        } else if (result instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitFace = blockHitResult.getDirection();

            if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
                if (hitFace == Direction.UP && config.place_torch_on_top_hit) {
                    BlockPos torchPos = hitPos.above();
                    BlockState torchState = Blocks.TORCH.defaultBlockState();

                    if (torchState.canSurvive(serverLevel, torchPos)) {
                        serverLevel.setBlock(torchPos, torchState, 3);
                        shouldDiscard = true;
                    }
                } else if (hitFace != Direction.DOWN && config.place_wall_torch_on_side_hit) {
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

        if (shouldDiscard && config.discard_after_successful_place) {
            this.discard();
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}