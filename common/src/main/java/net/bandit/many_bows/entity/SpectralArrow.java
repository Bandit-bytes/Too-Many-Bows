package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.SpectralWhisperBowConfig;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SpectralArrow extends AbstractArrow {

    private int remainingObstacles;
    private int lifespan;

    public SpectralArrow(EntityType<? extends SpectralArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public SpectralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SPECTRAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private SpectralWhisperBowConfig config() {
        return SpectralWhisperBowConfig.get();
    }

    private void applyConfigValues() {
        SpectralWhisperBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.remainingObstacles = config.phase_through_block_layers;
        this.lifespan = config.max_lifetime_ticks;
    }

    @Override
    public void tick() {
        super.tick();

        if (--lifespan <= 0) {
            this.discard();
            return;
        }

        Vec3 currentPosition = this.position();
        Vec3 nextPosition = currentPosition.add(this.getDeltaMovement());

        EntityHitResult entityHitResult = this.findHitEntity(currentPosition, nextPosition);
        if (entityHitResult != null) {
            this.onHitEntity(entityHitResult);
            return;
        }

        HitResult blockHitResult = this.level().clip(new ClipContext(
                currentPosition,
                nextPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) blockHitResult);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        SpectralWhisperBowConfig config = config();

        if (remainingObstacles > 0) {
            remainingObstacles--;

            Vec3 movement = this.getDeltaMovement();
            Vec3 hitPos = result.getLocation();

            this.setPos(
                    hitPos.x + movement.x * config.phase_nudge_multiplier,
                    hitPos.y + movement.y * config.phase_nudge_multiplier,
                    hitPos.z + movement.z * config.phase_nudge_multiplier
            );

            this.setDeltaMovement(movement);
        } else {
            if (config.discard_when_out_of_phases) {
                this.discard();
            } else if (config.stop_when_out_of_phases) {
                this.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!canHitEntity(target)) {
            return;
        }

        float damage = (float) ((AbstractArrowAccessor) this).manybows$getBaseDamage();
        target.hurt(damageSources().arrow(this, getOwner()), damage);
        discard();
    }

    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return ProjectileUtil.getEntityHitResult(
                level(),
                this,
                start,
                end,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D),
                entity -> super.canHitEntity(entity)
        );
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}