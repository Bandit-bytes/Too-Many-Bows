package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.FlameBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
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

public class FlameArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private boolean hasHit = false;
    private int hitTimer = 0;
    private int lifetime = 0;

    public FlameArrow(EntityType<? extends FlameArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public FlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.FLAME_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private FlameBowConfig config() {
        return FlameBowConfig.get();
    }

    private void applyConfigValues() {
        FlameBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    public void tick() {
        super.tick();

        FlameBowConfig config = config();

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

            for (int i = 0; i < config.flame_trail_particles; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(
                        ParticleTypes.FLAME,
                        this.getX() + motion.x * i * speedFactor,
                        this.getY() + motion.y * i * speedFactor,
                        this.getZ() + motion.z * i * speedFactor,
                        xOffset,
                        yOffset,
                        zOffset
                );
            }

            if (config.smoke_trail_enabled) {
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        FlameBowConfig config = config();

        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity hitEntity) {
                if (config.direct_hit_fire_ticks > 0) {
                    hitEntity.setRemainingFireTicks(config.direct_hit_fire_ticks);
                }

                if (config.apply_direct_hit_slowness) {
                    hitEntity.addEffect(new MobEffectInstance(
                            MobEffects.SLOWNESS,
                            config.direct_hit_slowness_duration_ticks,
                            config.direct_hit_slowness_amplifier
                    ));
                }

                createFireExplosion(result.getLocation(), hitEntity);
            }

            this.hasHit = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            createFireExplosion(result.getLocation(), null);
            this.hasHit = true;
        }
    }

    private void createFireExplosion(Vec3 position, @Nullable LivingEntity entityHit) {
        FlameBowConfig config = config();

        List<LivingEntity> entities = level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(config.fire_burst_radius)
        );

        float fireDamage = config.aoe_fire_damage_fallback;

        if (config.use_ranged_damage_attribute_for_aoe_damage && this.getOwner() instanceof LivingEntity shooter) {
            var lookup = this.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);

            ResourceKey<Attribute> rangedDamageKey =
                    ResourceKey.create(Registries.ATTRIBUTE, Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            Holder<Attribute> rangedAttr = lookup.get(rangedDamageKey).orElse(null);

            if (rangedAttr != null && config.ranged_damage_to_aoe_divisor > 0.0F) {
                var attrInstance = shooter.getAttribute(rangedAttr);
                if (attrInstance != null) {
                    fireDamage = (float) attrInstance.getValue() / config.ranged_damage_to_aoe_divisor;
                }
            }
        }

        fireDamage *= this.powerMultiplier;

        for (LivingEntity entity : entities) {
            if (entity != this.getOwner() && entity != entityHit) {
                if (config.aoe_fire_ticks > 0) {
                    entity.setRemainingFireTicks(config.aoe_fire_ticks);
                }

                if (fireDamage > 0.0F) {
                    entity.hurt(entity.damageSources().onFire(), fireDamage);
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    position.x,
                    position.y,
                    position.z,
                    config.burst_flame_particle_count,
                    1.0D,
                    0.5D,
                    1.0D,
                    0.1D
            );

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    position.x,
                    position.y,
                    position.z,
                    config.burst_explosion_particle_count,
                    1.0D,
                    0.25D,
                    1.0D,
                    0.01D
            );

            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    position.x,
                    position.y,
                    position.z,
                    config.burst_smoke_particle_count,
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
                SoundEvents.GENERIC_EXPLODE,
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

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}