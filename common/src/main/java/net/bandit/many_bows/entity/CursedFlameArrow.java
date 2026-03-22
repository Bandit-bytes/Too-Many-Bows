package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.NecroFlameBowConfig;
import net.bandit.many_bows.registry.EffectRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class CursedFlameArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private int particleTimer = 0;
    private int lifetime = 0;

    public CursedFlameArrow(EntityType<? extends CursedFlameArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public CursedFlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.CURSED_FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private NecroFlameBowConfig config() {
        return NecroFlameBowConfig.get();
    }

    private void applyConfigValues() {
        NecroFlameBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    public void tick() {
        super.tick();

        NecroFlameBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.isInGround()) {
            particleTimer++;
        }

        if (particleTimer < config.trail_particle_duration_ticks && this.level().isClientSide() && config.trail_particles_enabled) {
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < config.trail_particle_count; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + motion.x * i * 0.1D,
                        this.getY() + motion.y * i * 0.1D,
                        this.getZ() + motion.z * i * 0.1D,
                        xOffset,
                        yOffset,
                        zOffset
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide() || !(result.getEntity() instanceof LivingEntity hit)) {
            return;
        }

        NecroFlameBowConfig config = config();

        float directDamage = (float) config.base_damage;

        if (config.use_ranged_damage_attribute_scaling && getOwner() instanceof LivingEntity shooter) {
            var attrLookup = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedDamageRefOpt = attrLookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            if (rangedDamageRefOpt.isPresent() && config.ranged_damage_to_direct_damage_divisor > 0.0F) {
                var inst = shooter.getAttribute(rangedDamageRefOpt.get());
                if (inst != null) {
                    directDamage = (float) inst.getValue() / config.ranged_damage_to_direct_damage_divisor;
                }
            }
        }

        directDamage *= this.powerMultiplier;
        if (directDamage > 0.0F) {
            hit.hurt(this.damageSources().arrow(this, this.getOwner()), directDamage);
        }

        if (config.fire_ticks > 0) {
            hit.setRemainingFireTicks(config.fire_ticks);
        }

        float bonusFireDamage = config.bonus_fire_damage * this.powerMultiplier;
        if (bonusFireDamage > 0.0F) {
            hit.hurt(hit.damageSources().onFire(), bonusFireDamage);
        }

        if (config.remove_regeneration) {
            hit.removeEffect(MobEffects.REGENERATION);
        }

        if (config.remove_instant_health) {
            hit.removeEffect(MobEffects.INSTANT_HEALTH);
        }

        if (config.apply_cursed_flame) {
            var mobEffectRegistry = level().registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
            var cursedFlameHolder = mobEffectRegistry.getOrThrow(EffectRegistry.CURSED_FLAME.getKey());

            hit.addEffect(new MobEffectInstance(
                    cursedFlameHolder,
                    config.cursed_flame_duration_ticks,
                    config.cursed_flame_amplifier,
                    false,
                    true
            ));
        }

        if (config.impact_soul_particles_enabled && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    hit.getX(),
                    hit.getY(0.5D),
                    hit.getZ(),
                    config.impact_soul_particle_count,
                    1.0D,
                    0.5D,
                    1.0D,
                    0.1D
            );
        }

        if (config.discard_on_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        NecroFlameBowConfig config = config();
        Vec3 position = result.getLocation();

        if (config.impact_soul_particles_enabled) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    position.x,
                    position.y,
                    position.z,
                    config.impact_soul_particle_count,
                    1.0D,
                    0.5D,
                    1.0D,
                    0.1D
            );
        }

        if (config.impact_flame_particles_enabled) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    position.x,
                    position.y,
                    position.z,
                    config.impact_flame_particle_count,
                    0.5D,
                    0.5D,
                    0.5D,
                    0.01D
            );
        }

        serverLevel.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.SOUL_ESCAPE,
                this.getSoundSource(),
                config.block_impact_sound_volume,
                config.block_impact_sound_pitch
        );

        particleTimer = config.trail_particle_duration_ticks;

        if (config.discard_on_block_hit) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}