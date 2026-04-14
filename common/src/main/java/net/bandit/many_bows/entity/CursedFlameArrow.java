package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.NecroFlameBowConfig;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.bandit.many_bows.registry.EffectRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CursedFlameArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "necro_flame";

    private float powerMultiplier = 1.0F;
    private int particleTimer = 0;

    public CursedFlameArrow(EntityType<? extends CursedFlameArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues(null);
    }

    public CursedFlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.CURSED_FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues(shooter);
    }

    private static NecroFlameBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, NecroFlameBowConfig.class, NecroFlameBowConfig::new);
    }

    private void applyConfiguredValues(@Nullable LivingEntity shooter) {
        NecroFlameBowConfig config = config();

        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        double baseDamage = this.getBaseDamage();

        if (config.direct_hit_damage_override >= 0.0D) {
            baseDamage = config.direct_hit_damage_override;
        }

        if (shooter != null) {
            Holder<Attribute> necroDamageHolder =
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.NECRO_BOW_DAMAGE.get());

            baseDamage += shooter.getAttributeValue(necroDamageHolder);
        }

        this.setBaseDamage(baseDamage);
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    public void tick() {
        super.tick();

        NecroFlameBowConfig config = config();

        if (this.inGround) {
            particleTimer++;
        }

        if (config.trail_particles_enabled
                && particleTimer < config.max_trail_particle_duration_ticks
                && this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            int count = Math.max(0, config.trail_particle_count);

            for (int i = 0; i < count; i++) {
                double randomScale = config.trail_random_offset_scale;
                double xOffset = (this.random.nextDouble() - 0.5D) * randomScale;
                double yOffset = (this.random.nextDouble() - 0.5D) * randomScale;
                double zOffset = (this.random.nextDouble() - 0.5D) * randomScale;
                double step = config.trail_position_step_multiplier;

                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + motion.x * i * step,
                        this.getY() + motion.y * i * step,
                        this.getZ() + motion.z * i * step,
                        xOffset,
                        yOffset,
                        zOffset
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity hit) {
            NecroFlameBowConfig config = config();

            if (config.set_target_on_fire) {
                hit.setRemainingFireTicks(config.fire_ticks_on_hit);
            }

            if (config.bonus_fire_damage_enabled) {
                float fireDamage = resolveFireDamage(config);
                if (config.bonus_fire_damage_scales_with_power_multiplier) {
                    fireDamage *= this.powerMultiplier;
                }

                if (fireDamage > 0.0F) {
                    hit.hurt(hit.damageSources().onFire(), fireDamage);
                }
            }

            if (config.remove_regeneration_on_hit) {
                hit.removeEffect(MobEffects.REGENERATION);
            }

            if (config.remove_heal_on_hit) {
                hit.removeEffect(MobEffects.HEAL);
            }

            if (config.apply_cursed_flame_effect) {
                var mobEffectRegistry = level().registryAccess().registryOrThrow(Registries.MOB_EFFECT);
                var cursedFlameHolder = mobEffectRegistry.getHolderOrThrow(EffectRegistry.CURSED_FLAME.getKey());

                hit.addEffect(new MobEffectInstance(
                        cursedFlameHolder,
                        config.cursed_flame_duration_ticks,
                        config.cursed_flame_amplifier,
                        false,
                        config.cursed_flame_show_particles,
                        config.cursed_flame_show_icon
                ));
            }

            if (config.entity_hit_particles_enabled && level() instanceof ServerLevel sl) {
                sl.sendParticles(
                        ParticleTypes.SOUL,
                        hit.getX(),
                        hit.getY(0.5),
                        hit.getZ(),
                        Math.max(0, config.entity_hit_particle_count),
                        config.entity_hit_particle_offset_x,
                        config.entity_hit_particle_offset_y,
                        config.entity_hit_particle_offset_z,
                        config.entity_hit_particle_speed
                );
            }
        }
    }

    private float resolveFireDamage(NecroFlameBowConfig config) {
        float fireDamage = (float) config.base_fire_damage;

        if (config.use_ranged_damage_attribute_for_fire_damage && getOwner() instanceof LivingEntity shooter) {
            var attrRegistry = level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedKey = ResourceKey.create(
                    Registries.ATTRIBUTE,
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            );
            var rangedHolder = attrRegistry.getHolder(rangedKey).orElse(null);

            if (rangedHolder != null) {
                var inst = shooter.getAttribute(rangedHolder);
                if (inst != null && config.ranged_damage_attribute_divisor != 0.0D) {
                    fireDamage = (float) (inst.getValue() / config.ranged_damage_attribute_divisor);
                }
            }
        }

        return fireDamage;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        NecroFlameBowConfig config = config();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            Vec3 position = result.getLocation();

            if (config.block_hit_particles_enabled) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        position.x,
                        position.y,
                        position.z,
                        Math.max(0, config.block_hit_particle_count),
                        config.block_hit_particle_offset_x,
                        config.block_hit_particle_offset_y,
                        config.block_hit_particle_offset_z,
                        config.block_hit_particle_speed
                );
            }

            if (config.block_hit_sound_enabled) {
                serverLevel.playSound(
                        null,
                        position.x,
                        position.y,
                        position.z,
                        SoundEvents.SOUL_ESCAPE,
                        this.getSoundSource(),
                        config.block_hit_sound_volume,
                        config.block_hit_sound_pitch
                );
            }

            if (config.stop_trail_particles_after_block_hit) {
                particleTimer = config.max_trail_particle_duration_ticks;
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
}