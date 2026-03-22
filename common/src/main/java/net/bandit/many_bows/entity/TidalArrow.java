package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.TidalBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TidalArrow extends AbstractArrow {

    private int lifetime = 0;

    public TidalArrow(EntityType<? extends TidalArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public TidalArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TIDAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private TidalBowConfig config() {
        return TidalBowConfig.get();
    }

    private void applyConfigValues() {
        TidalBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        TidalBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.level().isClientSide() && this.isInWater() && config.underwater_bubble_trail_enabled) {
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < config.underwater_bubble_trail_count; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(
                        ParticleTypes.BUBBLE,
                        this.getX() + motion.x * i * 0.1D,
                        this.getY() + motion.y * i * 0.1D,
                        this.getZ() + motion.z * i * 0.1D,
                        xOffset, yOffset, zOffset
                );
            }
        }
    }

    @Override
    protected float getWaterInertia() {
        return config().water_inertia;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            TidalBowConfig config = config();

            if (config.apply_direct_hit_slowness) {
                hitEntity.addEffect(new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        config.direct_hit_slowness_duration_ticks,
                        config.direct_hit_slowness_amplifier
                ));
            }

            createWaterBindingEffect(hitEntity);
        }
    }

    private void createWaterBindingEffect(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        TidalBowConfig config = config();
        BlockPos entityPos = entity.blockPosition();

        if (config.apply_water_bind_slowness) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    config.water_bind_slowness_duration_ticks,
                    config.water_bind_slowness_amplifier,
                    false,
                    false
            ));
        }

        for (int i = 0; i < config.water_bind_particle_count; i++) {
            double angle = i * Math.PI / 15.0D;
            double xOffset = Math.cos(angle) * 0.5D;
            double zOffset = Math.sin(angle) * 0.5D;
            double yOffset = 0.3D * i;

            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    entityPos.getX() + xOffset,
                    entityPos.getY() + yOffset,
                    entityPos.getZ() + zOffset,
                    1,
                    0.0D, 0.0D, 0.0D, 0.0D
            );
        }

        this.level().playSound(
                null,
                entityPos,
                SoundEvents.GENERIC_SPLASH,
                this.getSoundSource(),
                config.water_bind_sound_volume,
                config.water_bind_sound_pitch
        );
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        createWaterSplash(result.getLocation());
    }

    private void createWaterSplash(Vec3 position) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        TidalBowConfig config = config();

        serverLevel.sendParticles(
                ParticleTypes.SPLASH,
                position.x,
                position.y,
                position.z,
                config.block_splash_particle_count,
                1.0D,
                0.5D,
                1.0D,
                0.1D
        );

        this.level().playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.SPLASH_POTION_BREAK,
                this.getSoundSource(),
                config.block_splash_sound_volume,
                config.block_splash_sound_pitch
        );
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}