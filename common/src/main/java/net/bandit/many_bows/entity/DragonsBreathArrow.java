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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

public class DragonsBreathArrow extends AbstractArrow {

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

            // Play sound on impact
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Apply damage to nearby entities
            level.getEntities((Entity) null, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity)
                    .forEach(entity -> {
                        if (entity != target) {
                            entity.hurt(target.damageSources().magic(), 4.0F);
                        }
                    });

            // Create an AreaEffectCloud for lingering particles
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
            areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
            areaEffectCloud.setRadius(5.0F); // Smaller radius to avoid lingering too long
            areaEffectCloud.setDuration(300); // Reduced duration
            areaEffectCloud.setRadiusPerTick(-0.1F); // Faster shrink rate
            areaEffectCloud.setWaitTime(0);
            areaEffectCloud.setFixedColor(0x00FF00);
            areaEffectCloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HARM, 40, 1));

            // Add the AreaEffectCloud to the level
            level.addFreshEntity(areaEffectCloud);

            // Create green dust particles for visual effect
            for (int i = 0; i < 20; i++) {
                double xOffset = (random.nextDouble() - 0.5D) * 1.5D;
                double yOffset = random.nextDouble() * 1.5D;
                double zOffset = (random.nextDouble() - 0.5D) * 1.5D;
                level.addParticle(new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.0F),
                        target.getX() + xOffset, target.getY() + yOffset, target.getZ() + zOffset,
                        0.0D, 0.0D, 0.0D);
            }
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
