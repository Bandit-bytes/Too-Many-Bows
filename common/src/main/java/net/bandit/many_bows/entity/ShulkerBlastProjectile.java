package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.ShulkerBlastBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShulkerBlastProjectile extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private int lifetime = 0;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public ShulkerBlastProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public ShulkerBlastProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private ShulkerBlastBowConfig config() {
        return ShulkerBlastBowConfig.get();
    }

    private void applyConfigValues() {
        ShulkerBlastBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.setNoGravity(!config.use_gravity);
    }

    @Override
    public void tick() {
        super.tick();

        ShulkerBlastBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            LivingEntity target = findNearestTarget();
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dy = (target.getY() + target.getEyeHeight() / 2.0D) - this.getY();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0.0D) {
                    this.setDeltaMovement(
                            this.getDeltaMovement().x * (1.0D - config.homing_factor) + (dx / distance) * config.speed * config.homing_factor,
                            this.getDeltaMovement().y * (1.0D - config.homing_factor) + (dy / distance) * config.speed * config.homing_factor,
                            this.getDeltaMovement().z * (1.0D - config.homing_factor) + (dz / distance) * config.speed * config.homing_factor
                    );
                }
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            for (int i = 0; i < config.trail_particle_count; i++) {
                this.level().addParticle(
                        ParticleTypes.END_ROD,
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

    private LivingEntity findNearestTarget() {
        ShulkerBlastBowConfig config = config();

        AABB searchBox = this.getBoundingBox().inflate(config.homing_range);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != this.getOwner()
                        && entity.isAlive()
                        && !(entity instanceof ArmorStand)
                        && (!config.ignore_tamed_animals || !(entity instanceof TamableAnimal tamable && tamable.isTame()))
        );

        return entities.stream()
                .min((e1, e2) -> Double.compare(e1.distanceTo(this), e2.distanceTo(this)))
                .orElse(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            ShulkerBlastBowConfig config = config();

            float damage = config.ranged_damage_fallback;

            if (config.use_ranged_damage_attribute_scaling && this.getOwner() instanceof LivingEntity shooter) {
                var registry = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
                var rangedAttrHolder = registry.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

                if (rangedAttrHolder != null) {
                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
                    if (attrInstance != null) {
                        damage = (float) attrInstance.getValue();
                    }
                }
            }

            damage *= this.powerMultiplier * config.final_damage_multiplier;

            DamageSource damageSource = this.level().damageSources().arrow(this, this.getOwner());
            if (damage > 0.0F) {
                target.hurt(damageSource, damage);
            }

            if (config.apply_levitation) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION,
                        config.levitation_duration_ticks,
                        config.levitation_amplifier
                ));
            }

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

            if (config.impact_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        config.impact_particle_count,
                        0.3D,
                        0.3D,
                        0.3D,
                        0.0D
                );
            }
        }

        this.discard();
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