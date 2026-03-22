package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SpectralWhisperBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpectralArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "spectral_whisper";

    private int remainingObstacles = 1;
    private int lifespan = 140;

    public SpectralArrow(EntityType<? extends SpectralArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SpectralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SPECTRAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static SpectralWhisperBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SpectralWhisperBowConfig.class, SpectralWhisperBowConfig::new);
    }

    private void applyConfiguredValues() {
        SpectralWhisperBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.remainingObstacles = Math.max(0, config.obstacles_to_phase_through);
        this.lifespan = config.max_lifetime_ticks;
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        if (lifespan > 0 && --lifespan <= 0) {
            this.discard();
            return;
        }

        SpectralWhisperBowConfig config = config();

        Vec3 currentPosition = this.position();
        Vec3 nextPosition = currentPosition.add(this.getDeltaMovement());

        EntityHitResult entityHitResult = this.findHitEntity(currentPosition, nextPosition, config.entity_hit_box_inflation);
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
        if (!level().isClientSide()) {
            SpectralWhisperBowConfig config = config();

            if (remainingObstacles > 0) {
                remainingObstacles--;

                Vec3 movement = this.getDeltaMovement();
                Vec3 hitPos = result.getLocation();
                double offset = config.phase_position_offset_multiplier;

                this.setPos(
                        hitPos.x + movement.x * offset,
                        hitPos.y + movement.y * offset,
                        hitPos.z + movement.z * offset
                );

                this.setDeltaMovement(movement);
            } else if (config.stop_when_out_of_phases) {
                if (config.discard_when_out_of_phases) {
                    this.discard();
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (super.canHitEntity(result.getEntity())) {
                target.hurt(damageSources().arrow(this, this.getOwner()), (float) this.getBaseDamage());
                this.discard();
            }
        }
    }

    public EntityHitResult findHitEntity(Vec3 start, Vec3 end, double inflation) {
        return ProjectileUtil.getEntityHitResult(
                level(),
                this,
                start,
                end,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(inflation),
                entity -> super.canHitEntity(entity)
        );
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