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

    public LightningArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.LIGHTNING_ARROW.get(), shooter, level);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        System.out.println("onHitEntity triggered!");
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
        System.out.println("tick method called, inGround: " + this.inGround);

        if (!level().isClientSide() && this.inGround && !lightningStruck) {
            summonLightning(this.getX(), this.getY(), this.getZ());
            lightningStruck = true;
            this.discard();
        }

        if (level().isClientSide()) {
            createParticles();
        }
    }

    private void summonLightning(double x, double y, double z) {
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level());
        if (lightningBolt == null) {
            System.out.println("Lightning bolt creation failed!");
            return;
        }
        lightningBolt.moveTo(x, y, z);

        // Optional owner check for cause
        if (this.getOwner() instanceof ServerPlayer player) {
            lightningBolt.setCause(player);
        } else {
            System.out.println("No valid owner found for lightning cause.");
        }

        if (!level().isClientSide) {
            level().addFreshEntity(lightningBolt);
        } else {
            System.out.println("Attempting to summon lightning on the client side, skipping.");
        }
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
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
