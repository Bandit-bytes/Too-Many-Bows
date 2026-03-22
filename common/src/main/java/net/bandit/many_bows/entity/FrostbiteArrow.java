package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.FrostbiteBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FrostbiteArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "frostbite";

    private boolean hasHit = false;
    private int hitTimer = 0;

    public FrostbiteArrow(EntityType<? extends FrostbiteArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public FrostbiteArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.FROSTBITE_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static FrostbiteBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, FrostbiteBowConfig.class, FrostbiteBowConfig::new);
    }

    private void applyConfiguredValues() {
        FrostbiteBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        FrostbiteBowConfig config = config();

        if (hasHit) {
            hitTimer++;
            if (config.discard_after_hit_delay && hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            spawnTrailParticles(config);
        }
    }

    private void spawnTrailParticles(FrostbiteBowConfig config) {
        double speedFactor = config.trail_speed_factor;
        Vec3 motion = this.getDeltaMovement();
        int steps = Math.max(0, config.trail_particle_steps);

        for (int i = 0; i < steps; i++) {
            double xOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
            double yOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
            double zOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;

            double x = this.getX() + motion.x * i * speedFactor;
            double y = this.getY() + motion.y * i * speedFactor;
            double z = this.getZ() + motion.z * i * speedFactor;

            if (config.trail_snowflake_enabled) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        x, y, z,
                        xOffset, yOffset, zOffset
                );
            }

            if (config.trail_cloud_enabled) {
                for (int c = 0; c < Math.max(0, config.trail_cloud_particles_per_step); c++) {
                    this.level().addParticle(
                            ParticleTypes.CLOUD,
                            this.getX(), this.getY(), this.getZ(),
                            0.0D, 0.0D, 0.0D
                    );
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            FrostbiteBowConfig config = config();

            if (config.apply_primary_target_slowness) {
                hitEntity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        config.primary_target_slowness_duration_ticks,
                        config.primary_target_slowness_amplifier
                ));
            }

            createFrostExplosion(result.getLocation(), hitEntity, config);

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
            FrostbiteBowConfig config = config();
            createFrostExplosion(result.getLocation(), null, config);

            if (config.start_hit_timer_on_block_hit) {
                this.hasHit = true;
                this.hitTimer = 0;
            }
        }
    }

    private void createFrostExplosion(Vec3 position, @Nullable LivingEntity entityHit, FrostbiteBowConfig config) {
        double radius = Math.max(0.0D, config.aoe_radius);

        if (config.aoe_slowness_enabled && radius > 0.0D) {
            List<LivingEntity> entities = level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(radius)
            );

            for (LivingEntity entity : entities) {
                boolean isOwner = entity == this.getOwner();
                boolean isPrimary = entity == entityHit;

                if (isOwner && !config.aoe_affects_owner) {
                    continue;
                }

                if (isPrimary && !config.aoe_affects_primary_target) {
                    continue;
                }

                if (entity.position().distanceTo(position) > radius) {
                    continue;
                }

                entity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        config.aoe_slowness_duration_ticks,
                        config.aoe_slowness_amplifier
                ));
            }
        }

        if (config.frost_explosion_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
            if (config.snowflake_burst_particle_count > 0) {
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        position.x,
                        position.y + 0.25D,
                        position.z,
                        config.snowflake_burst_particle_count,
                        config.snowflake_burst_offset_xz,
                        config.snowflake_burst_offset_y,
                        config.snowflake_burst_offset_xz,
                        config.snowflake_burst_speed_y
                );
            }

            if (config.cloud_burst_particle_count > 0) {
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        position.x,
                        position.y + 0.15D,
                        position.z,
                        config.cloud_burst_particle_count,
                        config.cloud_burst_offset_xz,
                        config.cloud_burst_offset_y,
                        config.cloud_burst_offset_xz,
                        config.cloud_burst_speed
                );
            }
        }

        if (config.impact_sound_enabled) {
            this.level().playSound(
                    null,
                    position.x,
                    position.y,
                    position.z,
                    SoundEvents.GLASS_BREAK,
                    this.getSoundSource(),
                    config.impact_sound_volume,
                    config.impact_sound_pitch
            );
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return config().allow_pickup && super.tryPickup(player);
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