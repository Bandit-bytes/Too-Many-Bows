//package net.bandit.many_bows.entity;
//
//
//import net.bandit.many_bows.registry.EntityRegistry;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.projectile.AbstractArrow;
//import net.minecraft.world.entity.projectile.ProjectileUtil;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.ClipContext;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.EntityHitResult;
//import net.minecraft.world.phys.HitResult;
//import net.minecraft.world.phys.Vec3;
//
//public class SpectralArrow extends AbstractArrow {
//    private int remainingObstacles;
//    private int lifespan = 140;
//
//    public SpectralArrow(EntityType<? extends SpectralArrow> entityType, Level level) {
//        super(entityType, level);
//    }
//
//    public SpectralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
//        super(EntityRegistry.SPECTRAL_ARROW.get(), shooter, level, bowStack, arrowStack);
//        this.setBaseDamage(7.0);
//
//        this.remainingObstacles = 1;
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        if (--lifespan <= 0) {
//            this.discard();
//            return;
//        }
//
//        Vec3 currentPosition = this.position();
//        Vec3 nextPosition = currentPosition.add(this.getDeltaMovement());
//        EntityHitResult entityHitResult = this.findHitEntity(currentPosition, nextPosition);
//        if (entityHitResult != null) {
//            this.onHitEntity(entityHitResult);
//            return;
//        }
//
//        HitResult blockHitResult = this.level().clip(new ClipContext(
//                currentPosition,
//                nextPosition,
//                ClipContext.Block.COLLIDER,
//                ClipContext.Fluid.NONE,
//                this
//        ));
//
//        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
//            this.onHitBlock((BlockHitResult) blockHitResult);
//        }
//    }
//
//    @Override
//    protected void onHitBlock(BlockHitResult result) {
//        if (!level().isClientSide()) {
//            if (remainingObstacles > 0) {
//                remainingObstacles--;
//
//                Vec3 movement = this.getDeltaMovement();
//                Vec3 hitPos = result.getLocation();
//                this.setPos(hitPos.x + movement.x * 0.5, hitPos.y + movement.y * 0.5, hitPos.z + movement.z * 0.5);
//
//                this.setDeltaMovement(movement);
//            } else {
//                this.setDeltaMovement(Vec3.ZERO);
//            }
//        }
//    }
//
//    @Override
//    protected void onHitEntity(EntityHitResult result) {
//        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
//            if (super.canHitEntity(result.getEntity())) {
//                target.hurt(damageSources().arrow(this, this.getOwner()), (float) this.getBaseDamage());
//                this.discard();
//            }
//        }
//    }
//
//    public EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
//        return ProjectileUtil.getEntityHitResult(level(), this, start, end,
//                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D),
//                entity -> super.canHitEntity(entity));
//    }
//
//    @Override
//    protected ItemStack getPickupItem() {
//        return new ItemStack(Items.ARROW);
//    }
//
//
//    @Override
//    protected ItemStack getDefaultPickupItem() {
//        return new ItemStack(Items.ARROW);
//    }
//
//}
