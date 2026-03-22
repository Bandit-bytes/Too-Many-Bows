package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VenomBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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

public class VenomArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "verdant_viper";

    private boolean hasHit = false;
    private int hitTimer = 0;

    public VenomArrow(EntityType<? extends VenomArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public VenomArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VENOM_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static VenomBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, VenomBowConfig.class, VenomBowConfig::new);
    }

    private void applyConfiguredValues() {
        VenomBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    public void tick() {
        super.tick();

        VenomBowConfig config = config();

        if (hasHit) {
            hitTimer++;
            if (config.discard_after_hit_delay && hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            double speedFactor = config.trail_speed_factor;
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < Math.max(0, config.trail_particle_steps); i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
                double yOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;
                double zOffset = (this.random.nextDouble() - 0.5D) * config.trail_random_offset_scale;

                double x = this.getX() + motion.x * i * speedFactor;
                double y = this.getY() + motion.y * i * speedFactor;
                double z = this.getZ() + motion.z * i * speedFactor;

                if (config.trail_spore_particles_enabled) {
                    this.level().addParticle(
                            ParticleTypes.SPORE_BLOSSOM_AIR,
                            x, y, z,
                            xOffset, yOffset, zOffset
                    );
                }
            }

            if (config.trail_glow_particle_enabled) {
                this.level().addParticle(
                        ParticleTypes.GLOW,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D, 0.0D, 0.0D
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide()) {
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity hitEntity) {
                VenomBowConfig config = config();

                if (config.direct_poison_enabled) {
                    hitEntity.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            config.direct_poison_duration_ticks,
                            config.direct_poison_amplifier
                    ));
                }

                createPoisonExplosion(result.getLocation(), hitEntity, config);

                if (config.start_hit_timer_on_entity_hit) {
                    this.hasHit = true;
                    this.hitTimer = 0;
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            VenomBowConfig config = config();
            createPoisonExplosion(result.getLocation(), null, config);

            if (config.start_hit_timer_on_block_hit) {
                this.hasHit = true;
                this.hitTimer = 0;
            }
        }
    }

    private void createPoisonExplosion(Vec3 position, @Nullable LivingEntity entityHit, VenomBowConfig config) {
        if (config.aoe_poison_enabled) {
            double radius = Math.max(0.0D, config.aoe_radius);
            List<LivingEntity> entities = level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(radius)
            );

            for (LivingEntity entity : entities) {
                if (!config.aoe_affects_owner && entity == this.getOwner()) {
                    continue;
                }

                if (!config.aoe_affects_primary_target && entity == entityHit) {
                    continue;
                }

                if (entity.position().distanceTo(position) > radius) {
                    continue;
                }

                entity.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        config.aoe_poison_duration_ticks,
                        config.aoe_poison_amplifier
                ));
            }
        }

        if (this.level() instanceof ServerLevel serverLevel && config.explosion_particles_enabled) {
            if (config.effect_particle_count > 0) {
                serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        position.x,
                        position.y,
                        position.z,
                        config.effect_particle_count,
                        config.effect_particle_offset_xz,
                        config.effect_particle_offset_y,
                        config.effect_particle_offset_xz,
                        config.effect_particle_speed_y
                );
            }

            if (config.sculk_soul_particle_count > 0) {
                serverLevel.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        position.x,
                        position.y,
                        position.z,
                        config.sculk_soul_particle_count,
                        config.sculk_soul_particle_offset_xz,
                        config.sculk_soul_particle_offset_y,
                        config.sculk_soul_particle_offset_xz,
                        0.0D
                );
            }
        }

        if (config.explosion_sound_enabled) {
            this.level().playSound(
                    null,
                    position.x,
                    position.y,
                    position.z,
                    SoundEvents.SLIME_SQUISH,
                    this.getSoundSource(),
                    config.explosion_sound_volume,
                    config.explosion_sound_pitch
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