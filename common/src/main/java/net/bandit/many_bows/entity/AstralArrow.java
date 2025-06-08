package net.bandit.many_bows.entity;


import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AstralArrow extends AbstractArrow {
    //No need for ranged_weapon:damage scaling
    private int ricochetCount = 3;

    public AstralArrow(EntityType<? extends AstralArrow> entityType, Level level) {
        super(entityType, level);
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ASTRAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(7.0);

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().arrow(this, this.getOwner()), (float) this.getBaseDamage());
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide()) {
            int maxRicochets = 6;
            if (ricochetCount < maxRicochets) {
                ricochetCount++;

                Vec3 velocity = this.getDeltaMovement();
                Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
                Vec3 reflected = velocity.subtract(normal.scale(2 * velocity.dot(normal)));


                this.setDeltaMovement(reflected.scale(0.7));

                Vec3 positionOffset = reflected.normalize().scale(0.1);
                this.setPos(this.getX() + positionOffset.x, this.getY() + positionOffset.y, this.getZ() + positionOffset.z);


                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.METAL_HIT,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

            } else {
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }


    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}