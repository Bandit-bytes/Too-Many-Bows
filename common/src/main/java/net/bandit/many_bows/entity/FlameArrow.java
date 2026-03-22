package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.FlameBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlameArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "flame_bow";

    private float powerMultiplier = 1.0F;

    private boolean hasHit = false;
    private int hitTimer = 0;

    public FlameArrow(EntityType<? extends FlameArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public FlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static FlameBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, FlameBowConfig.class, FlameBowConfig::new);
    }

    private void applyConfiguredValues() {
        FlameBowConfig config = config();
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

        FlameBowConfig config = config();

        if (hasHit) {
            hitTimer++;
            if (config.discard_after_hit_delay && hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
                return;
            }

            if (this.level().isClientSide()
                    && config.lingering_ring_particles_enabled
                    && shouldRunEvery(hitTimer, config.lingering_ring_interval_ticks)) {
                spawnInfernoRingParticles(config);
            }
        }

        if (this.level().isClientSide() && !hasHit && config.trail_particles_enabled) {
            spawnTrailParticles(config);
        }
    }

    private void spawnTrailParticles(FlameBowConfig config) {
        Vec3 motion = this.getDeltaMovement();

        for (int i = 0; i < Math.max(0, config.trail_steps); i++) {
            double xOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
            double yOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
            double zOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;

            double px = this.getX() + motion.x * i * config.trail_speed_factor;
            double py = this.getY() + motion.y * i * config.trail_speed_factor;
            double pz = this.getZ() + motion.z * i * config.trail_speed_factor;

            if (config.trail_flame_enabled) {
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, xOffset, yOffset, zOffset);
            }

            if (config.trail_lava_enabled && shouldRunEvery(i, config.trail_lava_every_n_steps)) {
                this.level().addParticle(
                        ParticleTypes.LAVA,
                        px, py, pz,
                        xOffset * 0.5D, yOffset * 0.5D, zOffset * 0.5D
                );
            }
        }

        if (config.trail_smoke_enabled && shouldRunEvery(this.tickCount, config.trail_smoke_every_n_ticks)) {
            this.level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    motion.x * config.trail_smoke_velocity_scale,
                    motion.y * config.trail_smoke_velocity_scale,
                    motion.z * config.trail_smoke_velocity_scale
            );
        }

        if (config.trail_soul_fire_spiral_enabled && shouldRunEvery(this.tickCount, config.trail_soul_fire_spiral_every_n_ticks)) {
            double angle = this.tickCount * config.trail_soul_fire_spiral_angle_scale;
            Vec3 perpendicular = new Vec3(-motion.z, 0.0D, motion.x);

            if (perpendicular.lengthSqr() < 1.0E-6D) {
                return;
            }

            perpendicular = perpendicular.normalize();

            Vec3 spiralPos = new Vec3(this.getX(), this.getY(), this.getZ())
                    .add(perpendicular.scale(Math.cos(angle) * config.trail_soul_fire_spiral_radius))
                    .add(0.0D, Math.sin(angle) * config.trail_soul_fire_spiral_radius, 0.0D);

            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    spiralPos.x, spiralPos.y, spiralPos.z,
                    0.0D, 0.0D, 0.0D
            );
        }
    }

    private void spawnInfernoRingParticles(FlameBowConfig config) {
        Vec3 pos = this.position();
        double radius = Math.max(0.0D, config.aoe_radius);
        int ringPoints = Math.max(1, config.lingering_ring_points);

        for (int i = 0; i < ringPoints; i++) {
            double angle = (i / (double) ringPoints) * Math.PI * 2.0D;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + this.random.nextDouble() * config.lingering_ring_random_y_scale;

            if (config.lingering_ring_flame_enabled) {
                this.level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.05D, 0.0D);
            }

            if (config.lingering_ring_lava_enabled && shouldRunEvery(i, config.lingering_ring_lava_every_n_points)) {
                this.level().addParticle(ParticleTypes.LAVA, x, y, z, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide()) {
            FlameBowConfig config = config();

            if (result.getEntity() instanceof LivingEntity hitEntity) {
                if (config.set_primary_target_on_fire) {
                    hitEntity.setRemainingFireTicks(config.primary_target_fire_ticks);
                }

                if (config.apply_primary_target_slowness) {
                    hitEntity.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            config.primary_target_slowness_duration_ticks,
                            config.primary_target_slowness_amplifier
                    ));
                }

                createMassiveInferno(result.getLocation(), hitEntity, config);
            }

            if (config.start_hit_timer_on_entity_hit) {
                this.hasHit = true;
                this.hitTimer = 0;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            FlameBowConfig config = config();
            createMassiveInferno(result.getLocation(), null, config);

            if (config.start_hit_timer_on_block_hit) {
                this.hasHit = true;
                this.hitTimer = 0;
            }
        }
    }

    private void createMassiveInferno(Vec3 position, @Nullable LivingEntity primaryHit, FlameBowConfig config) {
        double radius = Math.max(0.0D, config.aoe_radius);
        float baseDamage = resolveAoEDamage(config);
        int fireDuration = Math.max(0, config.aoe_fire_duration_ticks);

        if (radius > 0.0D) {
            List<LivingEntity> entities = level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(radius)
            );

            for (LivingEntity entity : entities) {
                boolean isOwner = entity == this.getOwner();
                boolean isPrimary = entity == primaryHit;

                if (isOwner && !config.aoe_affects_owner) {
                    continue;
                }

                double distance = entity.position().distanceTo(position);
                if (distance > radius) {
                    continue;
                }

                float distanceMultiplier = radius <= 0.0D ? 1.0F : Math.max(0.0F, 1.0F - (float) (distance / radius));

                if (!isPrimary || config.aoe_fire_affects_primary_target) {
                    entity.setRemainingFireTicks(fireDuration);
                }

                if ((!isPrimary || config.aoe_damage_affects_primary_target) && baseDamage > 0.0F) {
                    float damage = baseDamage;
                    if (config.aoe_damage_scales_with_power_multiplier) {
                        damage *= this.powerMultiplier;
                    }
                    entity.hurt(entity.damageSources().onFire(), damage * distanceMultiplier);
                }

                if (config.aoe_apply_slowness && (!isPrimary || config.aoe_slowness_affects_primary_target)) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            config.aoe_slowness_duration_ticks,
                            config.aoe_slowness_amplifier
                    ));
                }
            }
        }

        spawnInfernoExplosion(position, radius, config);

        if (config.explosion_sound_enabled) {
            this.level().playSound(
                    null,
                    position.x, position.y, position.z,
                    SoundEvents.GENERIC_EXPLODE,
                    this.getSoundSource(),
                    config.explosion_sound_volume,
                    config.explosion_sound_pitch
            );
        }

        if (config.fire_ambient_sound_enabled) {
            this.level().playSound(
                    null,
                    position.x, position.y, position.z,
                    SoundEvents.FIRE_AMBIENT,
                    this.getSoundSource(),
                    config.fire_ambient_sound_volume,
                    config.fire_ambient_sound_pitch
            );
        }

        if (config.ignite_blocks_enabled) {
            igniteArea(position, radius, config);
        }

        if (config.block_damage_enabled) {
            float explosionPower = Math.max(
                    config.minimum_block_damage_explosion_power,
                    (float) (radius * config.block_damage_radius_multiplier)
            );

            this.level().explode(
                    this,
                    position.x, position.y, position.z,
                    explosionPower,
                    Level.ExplosionInteraction.TNT
            );
        }
    }

    private float resolveAoEDamage(FlameBowConfig config) {
        float baseDamage = (float) config.aoe_damage_base;

        if (config.use_ranged_damage_attribute_for_aoe_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = this.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null && config.aoe_damage_attribute_divisor != 0.0D) {
                    baseDamage = (float) (attrInstance.getValue() / config.aoe_damage_attribute_divisor);
                }
            }
        }

        return baseDamage;
    }

    private void spawnInfernoExplosion(Vec3 position, double radius, FlameBowConfig config) {
        if (!(this.level() instanceof ServerLevel server) || !config.inferno_explosion_particles_enabled) {
            return;
        }

        if (config.flash_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.FLASH,
                    position.x, position.y + 0.15D, position.z,
                    config.flash_particle_count,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }

        if (config.flame_burst_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.FLAME,
                    position.x, position.y + 0.15D, position.z,
                    config.flame_burst_particle_count,
                    radius * config.flame_burst_offset_xz_multiplier,
                    radius * config.flame_burst_offset_y_multiplier,
                    radius * config.flame_burst_offset_xz_multiplier,
                    config.flame_burst_speed
            );
        }

        if (config.lava_burst_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.LAVA,
                    position.x, position.y + 0.1D, position.z,
                    config.lava_burst_particle_count,
                    radius * config.lava_burst_offset_xz_multiplier,
                    radius * config.lava_burst_offset_y_multiplier,
                    radius * config.lava_burst_offset_xz_multiplier,
                    config.lava_burst_speed
            );
        }

        if (config.soul_fire_burst_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    position.x, position.y + 0.1D, position.z,
                    config.soul_fire_burst_particle_count,
                    radius * config.soul_fire_burst_offset_xz_multiplier,
                    radius * config.soul_fire_burst_offset_y_multiplier,
                    radius * config.soul_fire_burst_offset_xz_multiplier,
                    config.soul_fire_burst_speed
            );
        }

        if (config.smoke_burst_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    position.x, position.y + 0.2D, position.z,
                    config.smoke_burst_particle_count,
                    radius * config.smoke_burst_offset_xz_multiplier,
                    radius * config.smoke_burst_offset_y_multiplier,
                    radius * config.smoke_burst_offset_xz_multiplier,
                    config.smoke_burst_speed
            );
        }

        if (config.explosion_particle_count > 0) {
            server.sendParticles(
                    ParticleTypes.EXPLOSION,
                    position.x, position.y + 0.15D, position.z,
                    config.explosion_particle_count,
                    radius * config.explosion_offset_xz_multiplier,
                    radius * config.explosion_offset_y_multiplier,
                    radius * config.explosion_offset_xz_multiplier,
                    0.0D
            );
        }

        if (config.inferno_spiral_particles_enabled) {
            int steps = Math.max(1, config.inferno_spiral_steps);

            for (int i = 0; i < steps; i++) {
                double t = i / (double) steps;
                double angle = t * Math.PI * 2.0D * config.inferno_spiral_rotations;
                double r = radius * (config.inferno_spiral_min_radius_multiplier + t * config.inferno_spiral_radius_growth_multiplier);
                double y = position.y + 0.05D + t * (radius * config.inferno_spiral_height_multiplier);

                double x = position.x + Math.cos(angle) * r;
                double z = position.z + Math.sin(angle) * r;

                if (config.inferno_spiral_flame_enabled) {
                    server.sendParticles(
                            ParticleTypes.FLAME,
                            x, y, z,
                            1,
                            config.inferno_spiral_particle_spread,
                            config.inferno_spiral_particle_spread,
                            config.inferno_spiral_particle_spread,
                            0.0D
                    );
                }

                if (config.inferno_spiral_soul_fire_enabled && shouldRunEvery(i, config.inferno_spiral_soul_fire_every_n_steps)) {
                    server.sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            x, y, z,
                            1,
                            config.inferno_spiral_particle_spread,
                            config.inferno_spiral_particle_spread,
                            config.inferno_spiral_particle_spread,
                            0.0D
                    );
                }
            }
        }
    }

    private void igniteArea(Vec3 pos, double radius, FlameBowConfig config) {
        if (config.respect_mob_griefing_for_ignite
                && !this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        int r = (int) Math.ceil(radius);
        BlockPos center = BlockPos.containing(pos);

        int yMin = Math.min(config.ignite_search_y_min, config.ignite_search_y_max);
        int yMax = Math.max(config.ignite_search_y_min, config.ignite_search_y_max);

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double dist2 = x * x + z * z;
                if (dist2 > radius * radius) {
                    continue;
                }

                BlockPos base = center.offset(x, 0, z);

                for (int y = yMin; y <= yMax; y++) {
                    BlockPos check = base.offset(0, y, 0);

                    if (this.level().getBlockState(check).isAir()
                            && this.level().getBlockState(check.below()).isSolidRender(this.level(), check.below())) {

                        if (this.random.nextDouble() < config.ignite_block_chance) {
                            this.level().setBlock(check, Blocks.FIRE.defaultBlockState(), 11);
                        }
                        break;
                    }
                }
            }
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

    private static boolean shouldRunEvery(int value, int interval) {
        return interval > 0 && value % interval == 0;
    }
}