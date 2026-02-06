package net.bandit.many_bows.entity;


import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AstralArrow extends AbstractArrow {
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
        super.onHitEntity(result);
        if (!level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) return;

        int maxRicochets = 6;
        if (ricochetCount >= maxRicochets) {
            this.discard();
            return;
        }

        ricochetCount++;

        Vec3 velocity = this.getDeltaMovement();

        var face = result.getDirection();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());

        Vec3 reflected = velocity.subtract(normal.scale(2.0D * velocity.dot(normal)));

        this.setDeltaMovement(reflected.scale(0.7D));

        Vec3 nudgeDir = reflected.lengthSqr() < 1.0E-6 ? normal : reflected.normalize();
        Vec3 positionOffset = nudgeDir.scale(0.1D);

        this.setPos(this.getX() + positionOffset.x, this.getY() + positionOffset.y, this.getZ() + positionOffset.z);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.METAL_HIT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0F,
                1.0F);
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