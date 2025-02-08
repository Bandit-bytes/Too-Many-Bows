package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IronCladArrow extends AbstractArrow {
    private boolean hasLanded = false;
    private int vacuumDuration = 80;

    public IronCladArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public IronCladArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.IRONCLAD_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        super.tick();

        if (hasLanded && vacuumDuration > 0) {
            AABB pullArea = new AABB(this.getX() - 5, this.getY() - 5, this.getZ() - 5,
                    this.getX() + 5, this.getY() + 5, this.getZ() + 5);

            List<Entity> nearbyEntities = this.level().getEntities(this, pullArea, e -> e instanceof LivingEntity);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    Vec3 arrowPos = this.position();
                    Vec3 entityPos = livingEntity.position();
                    Vec3 pullVector = arrowPos.subtract(entityPos).normalize().scale(0.2);
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(pullVector));
                }
            }
            vacuumDuration--;
            if (vacuumDuration <= 0) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);
        hasLanded = true;
    }
}
