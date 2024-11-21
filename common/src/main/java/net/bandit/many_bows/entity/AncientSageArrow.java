package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AncientSageArrow extends AbstractArrow {

    private static final float DEFAULT_ARMOR_PENETRATION_FACTOR = 0.33f;
    private static final int PARTICLE_LIFESPAN = 60; // Number of ticks to show particles
    private float armorPenetration = DEFAULT_ARMOR_PENETRATION_FACTOR;
    private int particleTicksRemaining = PARTICLE_LIFESPAN;

    public AncientSageArrow(EntityType<? extends AncientSageArrow> entityType, Level level) {
        super(entityType, level);
    }

    public AncientSageArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.ANCIENT_SAGE_ARROW.get(), shooter, level);
    }

    public void setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            float baseDamage = (float) this.getBaseDamage();
            float armorReducedDamage = baseDamage * (1 - armorPenetration);
            target.hurt(AncientSageDamageSource.create(this.level(), this, this.getOwner()), armorReducedDamage);
            createHitParticles();
        }

        // Safely discard the arrow
        this.discard();
    }


    @Override
    public void tick() {
        super.tick();

        // Only create trail particles for a limited number of ticks
        if (particleTicksRemaining > 0) {
            createTrailParticles();
            particleTicksRemaining--;
        }
    }

    private void createHitParticles() {
        for (int i = 0; i < 15; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetY = this.random.nextDouble() * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.ENCHANTED_HIT, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0.1, 0);
        }
    }

    private void createTrailParticles() {
        level().addParticle(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 0, 0.05, 0);
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
