package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.FlameBowConfig;
import net.minecraft.server.level.ServerLevel;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlameArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "flame_bow";

    private boolean hasHit = false;
    private int hitTimer = 0;

    public FlameArrow(EntityType<? extends FlameArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public FlameArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.FLAME_ARROW.get(), shooter, level);
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

    @Override
    public void tick() {
        super.tick();

        FlameBowConfig config = config();

        if (hasHit && config.discard_after_hit_delay) {
            hitTimer++;
            if (hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            double speedFactor = config.trail_speed_factor;
            double spread = config.trail_random_offset_scale;
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < Math.max(0, config.trail_steps); i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * spread;
                double yOffset = (this.random.nextDouble() - 0.5D) * spread;
                double zOffset = (this.random.nextDouble() - 0.5D) * spread;

                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + motion.x * i * speedFactor,
                        this.getY() + motion.y * i * speedFactor,
                        this.getZ() + motion.z * i * speedFactor,
                        xOffset, yOffset, zOffset);
                if (config.trail_smoke_enabled) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
                }
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
                    hitEntity.setSecondsOnFire(config.primary_target_fire_seconds);
                }
                if (config.apply_primary_target_slowness) {
                    hitEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            config.primary_target_slowness_duration_ticks, config.primary_target_slowness_amplifier));
                }
                createFireExplosion(result.getLocation(), hitEntity, config);
            }

            if (config.start_hit_timer_on_entity_hit) {
                this.hasHit = true;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            FlameBowConfig config = config();
            createFireExplosion(result.getLocation(), null, config);

            if (config.start_hit_timer_on_block_hit) {
                this.hasHit = true;
            }
        }
    }

    private void createFireExplosion(Vec3 position, @Nullable LivingEntity entityHit, FlameBowConfig config) {
        if (config.aoe_enabled) {
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(Math.max(0.0D, config.aoe_radius)));

            for (LivingEntity entity : entities) {
                if (!config.aoe_affects_owner && entity == this.getOwner()) continue;

                boolean isPrimary = entity == entityHit;
                if (!isPrimary || config.aoe_fire_affects_primary_target) {
                    entity.setSecondsOnFire(config.aoe_fire_seconds);
                }
                if (config.aoe_damage_enabled && (!isPrimary || config.aoe_damage_affects_primary_target)) {
                    entity.hurt(this.damageSources().onFire(), (float) config.aoe_damage_base);
                }
            }
        }

        if (config.explosion_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
            if (config.flame_burst_particle_count > 0) {
                serverLevel.sendParticles(ParticleTypes.FLAME, position.x, position.y + 0.5D, position.z,
                        config.flame_burst_particle_count, 0.5D, 0.25D, 0.5D, 0.05D);
            }
            if (config.explosion_particle_count > 0) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y + 0.25D, position.z,
                        Math.max(1, config.explosion_particle_count / 10), 0.5D, 0.125D, 0.5D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, position.x, position.y + 0.25D, position.z,
                        config.explosion_particle_count, 0.5D, 0.125D, 0.5D, 0.0D);
            }
        }

        if (config.explosion_sound_enabled) {
            this.level().playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE,
                    this.getSoundSource(), config.explosion_sound_volume, config.explosion_sound_pitch);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
