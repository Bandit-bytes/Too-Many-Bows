package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

public class DragonsBreathArrow extends AbstractArrow {
    private static final int PARTICLE_LIFESPAN = 40;
    private static final int MAX_LIFETIME = 100;
    private int particleTicksRemaining = PARTICLE_LIFESPAN;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof LivingEntity target) {
            Level level = target.level();
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

            level.getEntities(this, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity)
                    .forEach(entity -> {
                        if (entity instanceof LivingEntity livingEntity && livingEntity != target) {
                            livingEntity.hurt(target.damageSources().magic(), 4.0F);
                        }
                    });

            // Create area effect cloud
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
            areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
            areaEffectCloud.setRadius(3.0F);
            areaEffectCloud.setDuration(100);
            areaEffectCloud.setRadiusPerTick(-0.05F);
            areaEffectCloud.setWaitTime(10);
            areaEffectCloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.HARM, 40, 1));

            level.addFreshEntity(areaEffectCloud);
            createImpactParticles(target.getX(), target.getY(), target.getZ());
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (level().isClientSide() && particleTicksRemaining > 0) {
            createTrailParticles();
            particleTicksRemaining--;
        }
    }

    private void createImpactParticles(double x, double y, double z) {
        for (int i = 0; i < 20; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * 1.5D;
            double yOffset = random.nextDouble() * 1.5D;
            double zOffset = (random.nextDouble() - 0.5D) * 1.5D;
            level().addParticle(new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.0F),
                    x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
        }
    }

    private void createTrailParticles() {
        level().addParticle(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(), this.getZ(), 0.0D, -0.05D, 0.0D);
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
