package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class LightningArrow extends AbstractArrow {
    private boolean lightningStruck = false;

    public LightningArrow(EntityType<? extends LightningArrow> entityType, Level level) {
        super(entityType, level);
    }

    public LightningArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.LIGHTNING_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && !lightningStruck) {
            summonLightning(result.getLocation().x(), result.getLocation().y(), result.getLocation().z());
            lightningStruck = true;
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Summon lightning if the arrow is stuck in the ground and hasn't struck yet
        if (!level().isClientSide() && this.inGround && !lightningStruck) {
            summonLightning(this.getX(), this.getY(), this.getZ());
            lightningStruck = true;
            this.discard();
        }

        // Create visual effects on the client side
        if (level().isClientSide()) {
            createParticles();
        }
    }

    private void summonLightning(double x, double y, double z) {
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level());
        if (lightningBolt == null) {
            return;
        }
        lightningBolt.moveTo(x, y, z);

        // Set the cause of the lightning bolt to the shooter if applicable
        if (this.getOwner() instanceof ServerPlayer player) {
            lightningBolt.setCause(player);
        }

        level().addFreshEntity(lightningBolt);
    }

    private void createParticles() {
        for (int i = 0; i < 10; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetY = this.random.nextDouble() * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0, 0.1, 0.0);
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
