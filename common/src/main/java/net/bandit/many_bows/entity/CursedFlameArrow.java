package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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

public class CursedFlameArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private int particleTimer = 0;
    private static final int MAX_PARTICLE_DURATION = 100;

    public CursedFlameArrow(EntityType<? extends CursedFlameArrow> entityType, Level level) {
        super(entityType, level);
    }

    public CursedFlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.CURSED_FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.inGround) {
            particleTimer++;
        }
        if (particleTimer < MAX_PARTICLE_DURATION && this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < 5; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + motion.x * i * 0.1,
                        this.getY() + motion.y * i * 0.1,
                        this.getZ() + motion.z * i * 0.1,
                        xOffset, yOffset, zOffset);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            // Set fire duration
            hitEntity.setRemainingFireTicks(200); // 10 seconds of fire

            // Default base fire damage
            float fireDamage = 4.0F;

            // Check if the shooter is a living entity and get their ranged_weapon:damage
            if (this.getOwner() instanceof LivingEntity shooter) {
                var registry = this.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
                var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

                if (rangedAttrHolder != null) {
                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
                    if (attrInstance != null) {
                        // You can adjust the scaling as needed
                        fireDamage = (float) attrInstance.getValue() / 1.5F; // Example: 100 damage → 20 fire
                    }
                }
            }

            // Apply fire damage
            hitEntity.hurt(hitEntity.damageSources().onFire(), fireDamage * this.powerMultiplier);


            // Cancel healing effects
            hitEntity.removeEffect(MobEffects.REGENERATION);
            hitEntity.removeEffect(MobEffects.HEAL);

            // Visual effect
            createCursedSoulFireParticles(hitEntity.position());
        }
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            Vec3 position = result.getLocation();
            BlockPos hitPos = new BlockPos((int) position.x, (int) position.y, (int) position.z);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, position.x, position.y, position.z, 30, 0.5, 0.5, 0.5, 0.01);
            serverLevel.playSound(null, position.x, position.y, position.z, SoundEvents.SOUL_ESCAPE, this.getSoundSource(), 1.0F, 1.0F);

            particleTimer = MAX_PARTICLE_DURATION;
        }
    }

    private void createCursedSoulFireParticles(Vec3 position) {
        for (int i = 0; i < 30; i++) {
            double xOffset = (this.random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = this.random.nextDouble() * 0.5D;
            double zOffset = (this.random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(ParticleTypes.SOUL, position.x + xOffset, position.y + yOffset, position.z + zOffset, 0, 0.1D, 0);
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
