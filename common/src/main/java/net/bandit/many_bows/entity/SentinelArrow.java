package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.EntityHitResult;

public class SentinelArrow extends AbstractArrow {
    private static final float DAMAGE_MULTIPLIER = 5.0f;

    public SentinelArrow(EntityType<? extends SentinelArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SentinelArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.SENTINEL_ARROW.get(), shooter, level);
        this.setBaseDamage(4.0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            float finalDamage = (float) this.getBaseDamage();
            Entity owner = this.getOwner();

            if (isRaidMob(target)) {
                finalDamage *= DAMAGE_MULTIPLIER;

                level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7F, 1.0F);
                for (int i = 0; i < 10; i++) {
                    double xOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    double yOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    double zOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    level().addParticle(ParticleTypes.CRIT, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, 0, 0, 0);
                }
            }

            DamageSource damageSource = (owner instanceof LivingEntity living)
                    ? level().damageSources().arrow(this, living)
                    : level().damageSources().arrow(this, null);

            target.hurt(damageSource, finalDamage);
        }
    }


    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Pillager ||
                entity instanceof Vindicator ||
                entity instanceof Evoker ||
                entity instanceof Ravager ||
                entity instanceof Illusioner ||
                entity instanceof Witch;
    }
}
