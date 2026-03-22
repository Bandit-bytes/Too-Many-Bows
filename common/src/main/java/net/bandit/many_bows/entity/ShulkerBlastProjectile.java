package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ShulkerBlastBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class ShulkerBlastProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "shulker_blast";

    private float powerMultiplier = 1.0F;
    private int lifetime = 0;

    public ShulkerBlastProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public ShulkerBlastProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static ShulkerBlastBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, ShulkerBlastBowConfig.class, ShulkerBlastBowConfig::new);
    }

    private void applyConfiguredValues() {
        ShulkerBlastBowConfig config = config();
        this.setNoGravity(config.no_gravity);
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    public void tick() {
        super.tick();

        ShulkerBlastBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && config.homing_enabled) {
            LivingEntity target = findNearestTarget(config);
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dy = (target.getY() + target.getEyeHeight() / 2.0D) - this.getY();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0.0D) {
                    double homingFactor = config.homing_factor;
                    double speed = config.homing_speed;

                    this.setDeltaMovement(
                            this.getDeltaMovement().x * (1.0D - homingFactor) + (dx / distance) * speed * homingFactor,
                            this.getDeltaMovement().y * (1.0D - homingFactor) + (dy / distance) * speed * homingFactor,
                            this.getDeltaMovement().z * (1.0D - homingFactor) + (dz / distance) * speed * homingFactor
                    );
                }
            }
        }

        if (this.level().isClientSide && config.trail_particles_enabled) {
            for (int i = 0; i < Math.max(0, config.trail_particle_count); i++) {
                this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private LivingEntity findNearestTarget(ShulkerBlastBowConfig config) {
        AABB searchBox = this.getBoundingBox().inflate(config.homing_range);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> {
                    if (config.exclude_owner && entity == this.getOwner()) {
                        return false;
                    }
                    if (!entity.isAlive()) {
                        return false;
                    }
                    if (config.exclude_armor_stands && entity instanceof ArmorStand) {
                        return false;
                    }
                    if (config.exclude_tamed_animals && entity instanceof TamableAnimal tamable && tamable.isTame()) {
                        return false;
                    }
                    return true;
                }
        );

        return entities.stream()
                .min((e1, e2) -> Double.compare(e1.distanceTo(this), e2.distanceTo(this)))
                .orElse(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            ShulkerBlastBowConfig config = config();

            float scaledDamage = resolveDamage(config);
            if (config.damage_scales_with_power_multiplier) {
                scaledDamage *= this.powerMultiplier;
            }

            DamageSource damageSource = this.level().damageSources().arrow(this, this.getOwner());
            target.hurt(damageSource, scaledDamage);

            if (config.apply_levitation) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION,
                        config.levitation_duration_ticks,
                        config.levitation_amplifier
                ));
            }

            if (config.impact_sound_enabled) {
                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.SHULKER_BULLET_HIT,
                        SoundSource.PLAYERS,
                        config.impact_sound_volume,
                        config.impact_sound_pitch
                );
            }

            if (config.impact_particles_enabled) {
                for (int i = 0; i < Math.max(0, config.impact_particle_count); i++) {
                    double xOffset = (this.random.nextDouble() - 0.5D) * config.impact_particle_offset_xz;
                    double yOffset = this.random.nextDouble() * config.impact_particle_offset_y;
                    double zOffset = (this.random.nextDouble() - 0.5D) * config.impact_particle_offset_xz;

                    this.level().addParticle(
                            ParticleTypes.ENCHANTED_HIT,
                            this.getX() + xOffset,
                            this.getY() + yOffset,
                            this.getZ() + zOffset,
                            0.0D, 0.0D, 0.0D
                    );
                }
            }
        }

        if (config().discard_after_entity_hit) {
            this.discard();
        }
    }

    private float resolveDamage(ShulkerBlastBowConfig config) {
        float scaledDamage = (float) config.direct_hit_damage;

        if (config.use_ranged_damage_attribute_for_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    scaledDamage = (float) attrInstance.getValue();
                }
            }
        }

        return scaledDamage;
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