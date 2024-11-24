package net.bandit.many_bows.entity;


import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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

    public AuroraArrowEntity(Level world, LivingEntity shooter) {
        super(EntityRegistry.AURORA_ARROW.get(), shooter, world);
        this.setBaseDamage(7.0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Spawn a rift when hitting an entity
        if (!this.level().isClientSide) {
            spawnRift(BlockPos.containing(result.getLocation()));
        }
        this.discard(); // Remove the arrow after hitting an entity
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // Spawn a rift when hitting a block
        if (!this.level().isClientSide) {
            spawnRift(result.getBlockPos());
        }
        this.discard(); // Remove the arrow after hitting a block
    }

    private void spawnRift(BlockPos position) {
        RiftEntity rift = new RiftEntity(this.level(), position);
        this.level().addFreshEntity(rift);
    }

    private void spawnRift(double x, double y, double z) {
        RiftEntity rift = new RiftEntity(this.level(), x, y, z);
        this.level().addFreshEntity(rift);
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
