package net.bandit.many_bows.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

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

            // Damage nearby entities
            level.getEntities((Entity) null, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity).forEach(entity -> {
                if (entity != target) {
                    entity.hurt(target.damageSources().magic(), 4.0F); // Deal damage to nearby entities
                }
            });

            // Create and spawn an AreaEffectCloud with a custom color
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
            areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
            areaEffectCloud.setRadius(3.0F); // The radius of the cloud
            areaEffectCloud.setDuration(200); // Lasts for 10 seconds (200 ticks)
            areaEffectCloud.setRadiusPerTick(-0.05F); // Shrink the radius slowly over time
            areaEffectCloud.setWaitTime(0); // Starts affecting immediately

            // Set custom color (hex code for bright green: #00FF00)
            areaEffectCloud.setFixedColor(0x00FF00); // Bright green to match your bow

            // Apply custom damage logic for entities in the cloud
            areaEffectCloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HARM, 1, 0)); // Apply HARM

            level.addFreshEntity(areaEffectCloud); // Add the area effect cloud to the world
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // Arrow cannot be picked up
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
