package net.bandit.many_bows.entity;

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
        super(EntityType.ARROW, shooter, level);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            Level level = target.level();
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

            level.getEntities((Entity) null, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity).forEach(entity -> {
                if (entity != target) {
                    entity.hurt(target.damageSources().magic(), 4.0F);
                }
            });
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
            areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
            areaEffectCloud.setRadius(5.0F);
            areaEffectCloud.setDuration(300);
            areaEffectCloud.setRadiusPerTick(-0.05F);
            areaEffectCloud.setWaitTime(0);
            areaEffectCloud.setFixedColor(0x00FF00);
            areaEffectCloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HARM, 50, 1));

            level.addFreshEntity(areaEffectCloud);
            for (int i = 0; i < 50; i++) {
                double xOffset = (random.nextDouble() - 0.5D) * 2.0D;
                double yOffset = random.nextDouble();
                double zOffset = (random.nextDouble() - 0.5D) * 2.0D;
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
