package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class WindProjectile extends AbstractArrow {

    private static final float BASE_DAMAGE = 3.0f;
    private static final int LEVITATION_DURATION = 20;

    public WindProjectile(EntityType<? extends WindProjectile> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public WindProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WIND_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (target == this.getOwner()) {
                return;
            }

            // Apply damage and levitation effect
            target.hurt(damageSources().magic(), (float) this.getBaseDamage());
            if (!target.hasEffect(MobEffects.LEVITATION)) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION, 1));
            }
        }

        this.discard();
    }


    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0, 0.1, 0);
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
