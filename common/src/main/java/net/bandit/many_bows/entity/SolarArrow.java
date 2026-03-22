package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SolarBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SolarArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "solar_bow";

    private float powerMultiplier = 1.0F;

    private boolean tornadoActive = false;
    private int tornadoTick = 0;
    private Vec3 tornadoOrigin = null;

    public SolarArrow(EntityType<? extends SolarArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SolarArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SOLAR_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static SolarBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SolarBowConfig.class, SolarBowConfig::new);
    }

    private void applyConfiguredValues() {
        SolarBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        SolarBowConfig config = config();

        if (tornadoActive && tornadoOrigin != null) {
            this.setPos(tornadoOrigin.x, tornadoOrigin.y, tornadoOrigin.z);

            if (config.tornado_particles_enabled) {
                spawnRagingTornado((ServerLevel) level(), tornadoOrigin, tornadoTick, config);
            }

            LivingEntity shooter = this.getOwner() instanceof LivingEntity le ? le : null;
            float finalDamage = resolveTornadoDamage(config);

            if (config.tornado_damage_scales_with_power_multiplier) {
                finalDamage *= this.powerMultiplier;
            }

            float finalDamage1 = finalDamage;
            level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(config.tornado_damage_radius)).forEach(entity -> {
                if (!config.tornado_affects_owner && entity == shooter) {
                    return;
                }
                if (!config.tornado_affects_allies && shooter != null && entity.isAlliedTo(shooter)) {
                    return;
                }

                entity.setRemainingFireTicks(config.tornado_fire_ticks);
                entity.hurt(level().damageSources().magic(), finalDamage1);
            });

            tornadoTick++;
            if (tornadoTick > config.tornado_duration_ticks) {
                this.discard();
                return;
            }
        } else if (!tornadoActive && this.inGround && config.start_tornado_on_in_ground) {
            startTornado(config);
        } else if (!tornadoActive && config.pre_tornado_particles_enabled) {
            spawnAirSpiral((ServerLevel) level(), this.position(), tornadoTick, config);
            tornadoTick++;
        }

        if (config.ambient_sound_enabled
                && config.ambient_sound_interval_ticks > 0
                && tornadoTick % config.ambient_sound_interval_ticks == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    this.getSoundSource(),
                    config.ambient_sound_volume,
                    config.ambient_sound_base_pitch + this.random.nextFloat() * config.ambient_sound_random_pitch_range
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && !tornadoActive && config().start_tornado_on_entity_hit) {
            this.inGround = true;
            startTornado(config());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && !tornadoActive && config().start_tornado_on_block_hit) {
            this.inGround = true;
            startTornado(config());
        }
    }

    private void startTornado(SolarBowConfig config) {
        this.tornadoActive = true;
        this.tornadoOrigin = this.position();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setInvisible(true);

        if (config.startup_explosion_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    config.startup_explosion_particle_count,
                    config.startup_explosion_offset_x,
                    config.startup_explosion_offset_y,
                    config.startup_explosion_offset_z,
                    config.startup_explosion_speed
            );
        }

        if (config.startup_sound_enabled) {
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), config.startup_sound_volume, config.startup_sound_pitch);
        }
    }

    private float resolveTornadoDamage(SolarBowConfig config) {
        float scaledDamage = (float) config.tornado_damage_base;

        if (config.use_ranged_damage_attribute_for_tornado_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null && config.tornado_damage_attribute_divisor != 0.0D) {
                    scaledDamage = (float) (attrInstance.getValue() / config.tornado_damage_attribute_divisor);
                }
            }
        }

        return scaledDamage;
    }

    private void spawnRagingTornado(ServerLevel level, Vec3 center, int age, SolarBowConfig config) {
        int height = Math.min(age, config.tornado_height_cap);
        int spiralCount = Math.max(1, config.tornado_spiral_count);

        for (int y = 0; y < height; y++) {
            float progress = config.tornado_height_cap <= 0 ? 1.0F : (float) y / (float) config.tornado_height_cap;
            float radius = config.tornado_max_radius * (config.tornado_progress_base_multiplier + config.tornado_progress_growth_multiplier * progress);
            double baseY = center.y + y * config.tornado_height_step;

            for (int i = 0; i < spiralCount; i++) {
                double angle = age * config.tornado_age_angle_scale
                        + i * (2 * Math.PI / spiralCount)
                        + y * config.tornado_vertical_angle_scale;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                level.sendParticles(ParticleTypes.FLAME, x, baseY, z, config.tornado_flame_particles_per_point, 0, 0, 0, 0);

                if (config.tornado_lava_enabled
                        && config.tornado_lava_every_n_height > 0
                        && config.tornado_lava_every_n_spiral > 0
                        && y % config.tornado_lava_every_n_height == 0
                        && i % config.tornado_lava_every_n_spiral == 0) {
                    level.sendParticles(ParticleTypes.LAVA, x, baseY, z, 1, 0, 0, 0, 0);
                }

                if (config.tornado_smoke_enabled
                        && config.tornado_smoke_every_n_height > 0
                        && config.tornado_smoke_every_n_spiral > 0
                        && y % config.tornado_smoke_every_n_height == 0
                        && i % config.tornado_smoke_every_n_spiral == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, x, baseY, z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void spawnAirSpiral(ServerLevel level, Vec3 center, int age, SolarBowConfig config) {
        int points = Math.max(1, config.pre_tornado_points);

        for (int i = 0; i < points; i++) {
            double angle = age * config.pre_tornado_angle_scale + i * (2 * Math.PI / points);
            double x = center.x + Math.cos(angle) * config.pre_tornado_radius;
            double z = center.z + Math.sin(angle) * config.pre_tornado_radius;
            double y = center.y + config.pre_tornado_y_offset;

            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0);
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