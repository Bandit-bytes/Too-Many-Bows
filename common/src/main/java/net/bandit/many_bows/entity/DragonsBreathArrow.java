package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.DragonsBreathBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

public class DragonsBreathArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "dragons_breath";

    private float powerMultiplier = 1.0F;
    private int particleTicksRemaining = 40;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static DragonsBreathBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }

    private void applyConfiguredValues() {
        DragonsBreathBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        Level level = target.level();
        DragonsBreathBowConfig config = config();

        if (!level.isClientSide) {
            if (config.impact_sound_enabled) {
                level.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.DRAGON_FIREBALL_EXPLODE,
                        SoundSource.PLAYERS,
                        config.impact_sound_volume,
                        config.impact_sound_pitch
                );
            }

            if (config.splash_damage_enabled) {
                doSplashDamage(target, config);
            }

            if (config.damage_cloud_enabled) {
                spawnDamageCloud(target, config);
            }

            if (config.impact_particles_enabled) {
                createImpactParticles(target.getX(), target.getY(), target.getZ(), config);
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private void doSplashDamage(LivingEntity target, DragonsBreathBowConfig config) {
        float splashDamage = resolveScaledDamage(
                target.level(),
                (float) config.splash_damage_base,
                config.use_ranged_damage_attribute_for_splash_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.splash_damage_attribute_divisor
        );

        double radius = Math.max(0.0D, config.splash_radius);

        target.level().getEntities(this, target.getBoundingBox().inflate(radius), e -> e instanceof LivingEntity)
                .forEach(entity -> {
                    if (!(entity instanceof LivingEntity livingEntity)) {
                        return;
                    }

                    if (!config.splash_damage_affects_primary_target && livingEntity == target) {
                        return;
                    }

                    if (!config.splash_damage_affects_owner && livingEntity == this.getOwner()) {
                        return;
                    }

                    livingEntity.hurt(target.damageSources().magic(), splashDamage);
                });
    }

    private void spawnDamageCloud(LivingEntity target, DragonsBreathBowConfig config) {
        float cloudDamage = resolveScaledDamage(
                target.level(),
                (float) config.cloud_damage_base,
                config.use_ranged_damage_attribute_for_cloud_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.cloud_damage_attribute_divisor
        );

        if (config.cloud_damage_scales_with_power_multiplier) {
            cloudDamage *= this.powerMultiplier;
        }

        final float finalCloudDamage = cloudDamage;
        final int interval = Math.max(1, config.cloud_damage_interval_ticks);
        final boolean affectOwner = config.cloud_affects_owner;
        final boolean affectPrimary = config.cloud_affects_primary_target;
        final LivingEntity primaryTarget = target;

        AreaEffectCloud damagingCloud = new AreaEffectCloud(target.level(), target.getX(), target.getY(), target.getZ()) {
            int ticksExisted = 0;

            @Override
            public void tick() {
                super.tick();

                if (!this.level().isClientSide && ++ticksExisted % interval == 0) {
                    this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).stream()
                            .filter(e -> e.isAlive())
                            .filter(e -> affectOwner || e != getOwner())
                            .filter(e -> affectPrimary || e != primaryTarget)
                            .filter(e -> this.distanceToSqr(e) <= this.getRadius() * this.getRadius())
                            .forEach(e -> e.hurt(this.damageSources().magic(), finalCloudDamage));
                }
            }
        };

        if (this.getOwner() instanceof LivingEntity livingOwner) {
            damagingCloud.setOwner(livingOwner);
        }

        damagingCloud.setDuration(Math.max(0, config.cloud_duration_ticks));
        damagingCloud.setWaitTime(Math.max(0, config.cloud_wait_time_ticks));
        damagingCloud.setRadius(Math.max(0.0F, config.cloud_radius));
        damagingCloud.setRadiusPerTick(config.cloud_radius_per_tick);
        damagingCloud.setParticle(ParticleTypes.DRAGON_BREATH);

        target.level().addFreshEntity(damagingCloud);
    }

    private float resolveScaledDamage(
            Level level,
            float fallback,
            boolean useAttributeScaling,
            String attributeNamespace,
            String attributePath,
            double divisor
    ) {
        if (!useAttributeScaling || !(this.getOwner() instanceof LivingEntity shooter)) {
            return fallback;
        }

        var registry = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        var rangedAttrHolder = registry.getHolder(
                ResourceLocation.fromNamespaceAndPath(attributeNamespace, attributePath)
        ).orElse(null);

        if (rangedAttrHolder == null) {
            return fallback;
        }

        var attrInstance = shooter.getAttribute(rangedAttrHolder);
        if (attrInstance == null || divisor == 0.0D) {
            return fallback;
        }

        return (float) (attrInstance.getValue() / divisor);
    }

    @Override
    public void tick() {
        super.tick();

        DragonsBreathBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide && config.trail_particles_enabled && particleTicksRemaining > 0) {
            createTrailParticles(config);
            particleTicksRemaining--;
        }
    }

    private void createImpactParticles(double x, double y, double z, DragonsBreathBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                x,
                y + config.impact_particle_base_y_offset,
                z,
                Math.max(0, config.impact_particle_count),
                config.impact_particle_offset_x,
                config.impact_particle_offset_y,
                config.impact_particle_offset_z,
                config.impact_particle_speed
        );
    }

    private void createTrailParticles(DragonsBreathBowConfig config) {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < config.trail_stationary_speed_threshold) {
            if (config.spawn_stationary_trail_particle) {
                level().addParticle(
                        ParticleTypes.DRAGON_BREATH,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
            return;
        }

        double nx = -vel.x / speed;
        double ny = -vel.y / speed;
        double nz = -vel.z / speed;

        int steps = Math.max(1, config.trail_steps);
        double spacing = config.trail_spacing;

        for (int i = 0; i < steps; i++) {
            double px = this.getX() + nx * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;
            double py = this.getY() + ny * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;
            double pz = this.getZ() + nz * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;

            double vx = nx * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;
            double vy = ny * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;
            double vz = nz * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;

            level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, vx, vy, vz);
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