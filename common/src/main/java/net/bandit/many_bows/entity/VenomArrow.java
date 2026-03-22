package net.bandit.many_bows.entity;

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
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VenomArrow extends AbstractArrow {

    private boolean hasHit = false;
    private int hitTimer = 0;
    private int lifetime = 0;

    public VenomArrow(EntityType<? extends VenomArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public VenomArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VENOM_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private VenomBowConfig config() {
        return VenomBowConfig.get();
    }

    private void applyConfigValues() {
        VenomBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        VenomBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (hasHit) {
            hitTimer++;
            if (hitTimer >= config.post_hit_linger_ticks) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            double speedFactor = 0.1D;
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < config.spore_trail_particle_count; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        this.getX() + motion.x * i * speedFactor,
                        this.getY() + motion.y * i * speedFactor,
                        this.getZ() + motion.z * i * speedFactor,
                        xOffset, yOffset, zOffset
                );
            }

            if (config.glow_trail_enabled) {
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

                if (config.apply_direct_hit_poison) {
                    hitEntity.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            config.direct_hit_poison_duration_ticks,
                            config.direct_hit_poison_amplifier
                    ));
                }

                createPoisonExplosion(result.getLocation(), hitEntity);
                this.hasHit = true;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            createPoisonExplosion(result.getLocation(), null);
            this.hasHit = true;
        }
    }

    private void createPoisonExplosion(Vec3 position, @Nullable LivingEntity entityHit) {
        VenomBowConfig config = config();

        List<LivingEntity> entities = level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(config.poison_burst_radius)
        );

        for (LivingEntity entity : entities) {
            if (entity != this.getOwner() && entity != entityHit && config.apply_aoe_poison) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        config.aoe_poison_duration_ticks,
                        config.aoe_poison_amplifier
                ));
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    position.x,
                    position.y,
                    position.z,
                    config.witch_particle_count,
                    1.0D,
                    0.5D,
                    1.0D,
                    0.1D
            );

            serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    position.x,
                    position.y,
                    position.z,
                    config.sculk_soul_particle_count,
                    1.0D,
                    0.25D,
                    1.0D,
                    0.01D
            );
        }

        this.level().playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.SLIME_SQUISH,
                this.getSoundSource(),
                config.impact_sound_volume,
                config.impact_sound_pitch
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