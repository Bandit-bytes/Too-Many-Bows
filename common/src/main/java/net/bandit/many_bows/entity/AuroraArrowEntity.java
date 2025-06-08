package net.bandit.many_bows.entity;


import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AuroraArrowEntity extends AbstractArrow {

    public AuroraArrowEntity(EntityType<? extends AuroraArrowEntity> entityType, Level world) {
        super(entityType, world);
    }

    public AuroraArrowEntity(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AURORA_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(7.0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            spawnRift(BlockPos.containing(result.getLocation()));
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            spawnRift(result.getBlockPos());
        }
        this.discard();
    }
    //Scaled Entity's damage
    private void spawnRift(BlockPos position) {
        if (this.getOwner() instanceof LivingEntity owner) {
            RiftEntity rift = new RiftEntity(this.level(), owner, position);
            this.level().addFreshEntity(rift);
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
