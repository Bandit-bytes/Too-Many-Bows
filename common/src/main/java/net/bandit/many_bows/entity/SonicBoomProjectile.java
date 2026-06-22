package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SonicBoomProjectile extends AbstractArrow {

    private static final double DEFAULT_DAMAGE = 20.0D;
    private int lifetime = 60;
    private int particleAge = 0;
    private int enchantmentKnockback = 0;

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setBaseDamage(DEFAULT_DAMAGE);
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
        this.setBaseDamage(DEFAULT_DAMAGE);
    }


    public void setEnchantmentKnockback(int level) {
        this.enchantmentKnockback = Math.max(0, level);
        this.setKnockback(this.enchantmentKnockback);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            boolean damaged = target.hurt(damageSources().sonicBoom(this), (float) this.getBaseDamage());
            if (damaged) {
                if (this.isOnFire()) {
                    target.setSecondsOnFire(5);
                }

                double knockbackStrength = 2.0D + (this.enchantmentKnockback * 0.6D);
                target.knockback(knockbackStrength,
                        Math.sin(this.getYRot() * Math.PI / 180.0F),
                        -Math.cos(this.getYRot() * Math.PI / 180.0F));
            }
            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        particleAge++;

        if (!this.level().isClientSide && --this.lifetime <= 0) {
            this.discard();
        }

        if (this.level().isClientSide) {
            createSonicBoomSpiral();
        }
    }

    private void createSonicBoomSpiral() {
        int particles = 25;
        double radius = 0.5;
        double spiralExpansionRate = 0.15;

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * (i + particleAge * 0.1);
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = particleAge * 0.05 - (i * 0.01);

            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D);

            radius += spiralExpansionRate;
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
