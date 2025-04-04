package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TidalArrow extends AbstractArrow {

    public TidalArrow(EntityType<? extends TidalArrow> entityType, Level level) {
        super(entityType, level);
    }

    public TidalArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TIDAL_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() && this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < 5; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                this.level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + motion.x * i * 0.1,
                        this.getY() + motion.y * i * 0.1,
                        this.getZ() + motion.z * i * 0.1,
                        xOffset, yOffset, zOffset);
            }
        }
    }

    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            hitEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            createWaterBindingEffect(hitEntity);
        }
    }

    private void createWaterBindingEffect(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            BlockPos entityPos = entity.blockPosition();
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false));
            for (int i = 0; i < 30; i++) {
                double angle = i * Math.PI / 15;
                double xOffset = Math.cos(angle) * 0.5;
                double zOffset = Math.sin(angle) * 0.5;
                double yOffset = 0.3 * i;

                serverLevel.sendParticles(
                        ParticleTypes.SPLASH,
                        entityPos.getX() + xOffset,
                        entityPos.getY() + yOffset,
                        entityPos.getZ() + zOffset,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
            this.level().playSound(null, entityPos, SoundEvents.GENERIC_SPLASH, this.getSoundSource(), 0.5F, 0.8F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        createWaterSplash(result.getLocation());
    }

    private void createWaterSplash(Vec3 position) {
        if (!this.level().isClientSide()) {
            for (int i = 0; i < 20; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 2.0D;
                double yOffset = this.random.nextDouble();
                double zOffset = (this.random.nextDouble() - 0.5D) * 2.0D;

                this.level().addParticle(ParticleTypes.SPLASH,
                        position.x + xOffset,
                        position.y + yOffset,
                        position.z + zOffset,
                        0, 0.1D, 0);
            }

            this.level().playSound(null, position.x, position.y, position.z, SoundEvents.SPLASH_POTION_BREAK, this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }


    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
