package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.TidalBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
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

    private static final String CONFIG_NAME = "tidal_bow";

    public TidalArrow(EntityType<? extends TidalArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public TidalArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.TIDAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static TidalBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, TidalBowConfig.class, TidalBowConfig::new);
    }

    private void applyConfiguredValues() {
        TidalBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    public void tick() {
        super.tick();

        TidalBowConfig config = config();

        if (this.level().isClientSide() && this.isInWater() && config.underwater_trail_particles_enabled) {
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < Math.max(0, config.underwater_trail_particle_count); i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * config.underwater_trail_offset_scale;
                double yOffset = (this.random.nextDouble() - 0.5D) * config.underwater_trail_offset_scale;
                double zOffset = (this.random.nextDouble() - 0.5D) * config.underwater_trail_offset_scale;

                this.level().addParticle(
                        ParticleTypes.BUBBLE,
                        this.getX() + motion.x * i * config.underwater_trail_spacing,
                        this.getY() + motion.y * i * config.underwater_trail_spacing,
                        this.getZ() + motion.z * i * config.underwater_trail_spacing,
                        xOffset,
                        yOffset,
                        zOffset
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

            if (config.entity_hit_slowness_enabled) {
                hitEntity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        config.entity_hit_slowness_duration_ticks,
                        config.entity_hit_slowness_amplifier
                ));
            }

            createWaterBindingEffect(hitEntity, config);
        }
    }

    private void createWaterBindingEffect(LivingEntity entity, TidalBowConfig config) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (config.binding_slowness_enabled) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    config.binding_slowness_duration_ticks,
                    config.binding_slowness_amplifier,
                    false,
                    false
            ));
        }

        if (config.binding_particles_enabled) {
            for (int i = 0; i < Math.max(0, config.binding_particle_count); i++) {
                double angle = i * Math.PI / 15.0D;
                double xOffset = Math.cos(angle) * config.binding_ring_radius;
                double zOffset = Math.sin(angle) * config.binding_ring_radius;
                double yOffset = config.binding_y_step * i;

                serverLevel.sendParticles(
                        ParticleTypes.SPLASH,
                        entity.getX() + xOffset,
                        entity.getY() + yOffset,
                        entity.getZ() + zOffset,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );
            }
        }

        if (config.binding_sound_enabled) {
            this.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.GENERIC_SPLASH,
                    this.getSoundSource(),
                    config.binding_sound_volume,
                    config.binding_sound_pitch
            );
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        createWaterSplash(result.getLocation(), config());
    }

    private void createWaterSplash(Vec3 position, TidalBowConfig config) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (config.block_splash_particles_enabled) {
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    position.x,
                    position.y,
                    position.z,
                    Math.max(0, config.block_splash_particle_count),
                    config.block_splash_offset_xz,
                    config.block_splash_offset_y,
                    config.block_splash_offset_xz,
                    config.block_splash_speed_y
            );
        }

        if (config.block_splash_sound_enabled) {
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
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}