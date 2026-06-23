package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.GravewireBowConfig;
import net.bandit.many_bows.registry.AttributesRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class GravewireArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "gravewire_bow";

    private float powerMultiplier = 1.0F;
    private int particleTicksRemaining = 40;
    private int lifetime = 0;
    private boolean ignoreTargetHurtFrames = false;

    public GravewireArrow(EntityType<? extends GravewireArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfiguredValues(null);
    }

    public GravewireArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.GRAVEWIRE_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfiguredValues(shooter);
    }

    private static GravewireBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, GravewireBowConfig.class, GravewireBowConfig::new);
    }

    private void applyConfiguredValues(@org.jetbrains.annotations.Nullable LivingEntity shooter) {
        GravewireBowConfig config = config();
        double baseDamage = config.direct_hit_damage;

        if (shooter != null) {
            Holder<Attribute> gravewireDamageHolder =
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributesRegistry.GRAVEWIRE_BOW_DAMAGE.get());

            baseDamage += shooter.getAttributeValue(gravewireDamageHolder);
        }

        this.setBaseDamage(baseDamage);
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public void setIgnoreTargetHurtFrames(boolean ignoreTargetHurtFrames) {
        this.ignoreTargetHurtFrames = ignoreTargetHurtFrames;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && ignoreTargetHurtFrames && result.getEntity() instanceof LivingEntity livingTarget) {
            livingTarget.invulnerableTime = 0;
            livingTarget.hurtTime = 0;
        }

        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        Level level = target.level();
        GravewireBowConfig config = config();

        if (!level.isClientSide()) {
            if (config.impact_sound_enabled) {
                level.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.WITCH_THROW,
                        SoundSource.PLAYERS,
                        config.impact_sound_volume,
                        config.impact_sound_pitch
                );
            }

            if (config.mark_primary_target) {
                if (config.primary_glowing_duration_ticks > 0) {
                    target.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING,
                            config.primary_glowing_duration_ticks,
                            0,
                            false,
                            true
                    ));
                }

                if (config.primary_weakness_duration_ticks > 0) {
                    target.addEffect(new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            config.primary_weakness_duration_ticks,
                            Math.max(0, config.primary_weakness_amplifier),
                            false,
                            true
                    ));
                }
                if (config.grave_mark_entity_enabled) {
                    spawnGraveMark(target, config);
                }
                if (config.soul_rip_enabled) {
                    spawnSoulRip(target, config);
                }
                if (config.grave_mist_enabled) {
                    spawnGraveMist(target, config);
                }
            }

            if (config.chain_enabled) {
                doChainLash(target, config);
            }
            if (config.grave_bloom_on_kill && !target.isAlive()) {
                triggerGraveBloom(target, config);
            }

            if (config.impact_particles_enabled) {
                createImpactParticles(target.getX(), target.getY(), target.getZ(), config);
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private void doChainLash(LivingEntity primaryTarget, GravewireBowConfig config) {
        float chainDamage = resolveScaledDamage(
                primaryTarget.level(),
                (float) config.chain_damage_base,
                config.use_ranged_damage_attribute_for_chain_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.chain_damage_attribute_divisor
        );

        if (config.chain_damage_scales_with_power_multiplier) {
            chainDamage *= this.powerMultiplier;
        }

        double radius = Math.max(0.0D, config.chain_radius);
        int maxTargets = Math.max(0, config.chain_targets);
        int hitCount = 0;

        for (LivingEntity living : primaryTarget.level().getEntitiesOfClass(
                LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(radius),
                e -> e.isAlive()
        )) {
            if (living == primaryTarget && !config.chain_affects_primary_target) {
                continue;
            }

            if (living == this.getOwner() && !config.chain_affects_owner) {
                continue;
            }

            if (living == primaryTarget) {
                continue;
            }

            living.hurt(this.damageSources().magic(), chainDamage);

            if (config.soul_rip_enabled && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        living.getX(), living.getY() + 1.0D, living.getZ(),
                        4, 0.08D, 0.12D, 0.08D, 0.01D
                );
            }
            if (config.grave_bloom_on_kill && !living.isAlive()) {
                triggerGraveBloom(living, config);
            }

            if (config.chain_visuals_enabled) {
                spawnChainParticles(primaryTarget, living, config);
            }

            if (config.impact_particles_enabled && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        living.getX(),
                        living.getY() + 0.5D,
                        living.getZ(),
                        6,
                        0.15D,
                        0.15D,
                        0.15D,
                        0.01D
                );
            }

            hitCount++;
            if (hitCount >= maxTargets) {
                break;
            }
        }
    }
    private void spawnChainParticles(LivingEntity from, LivingEntity to, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 start = from.position().add(0.0D, config.chain_visual_y_offset, 0.0D);
        Vec3 end = to.position().add(0.0D, config.chain_visual_y_offset, 0.0D);
        Vec3 diff = end.subtract(start);

        int segments = Math.max(2, config.chain_particle_segments);
        double randomness = Math.max(0.0D, config.chain_visual_randomness);

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;

            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            double rx = (random.nextDouble() - 0.5D) * randomness;
            double ry = (random.nextDouble() - 0.5D) * randomness;
            double rz = (random.nextDouble() - 0.5D) * randomness;

            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    x + rx,
                    y + ry,
                    z + rz,
                    Math.max(1, config.chain_particles_per_segment),
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );

            if (i % 2 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        x,
                        y,
                        z,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }

        if (config.chain_visual_double_strand) {
            Vec3 axis = diff.normalize();
            Vec3 side = new Vec3(-axis.z, 0.0D, axis.x);

            if (side.lengthSqr() < 1.0E-4D) {
                side = new Vec3(1.0D, 0.0D, 0.0D);
            } else {
                side = side.normalize();
            }

            double offset = config.chain_visual_strand_offset;
            Vec3 strandOffset = side.scale(offset);

            for (int i = 0; i <= segments; i++) {
                double t = i / (double) segments;
                Vec3 base = start.add(diff.scale(t));

                Vec3 p1 = base.add(strandOffset);
                Vec3 p2 = base.subtract(strandOffset);

                serverLevel.sendParticles(ParticleTypes.WITCH, p1.x, p1.y, p1.z, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.WITCH, p2.x, p2.y, p2.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private float resolveScaledDamage(
            Level level,
            float fallback,
            boolean useAttributeScaling,
            String attributeNamespace,
            String attributePath,
            double divisor
    ) {
        if (!useAttributeScaling || !(this.getOwner() instanceof LivingEntity shooter)) {
            return fallback;
        }

        var registry = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var rangedAttrHolder = registry.get(
                Identifier.fromNamespaceAndPath(attributeNamespace, attributePath)
        ).orElse(null);

        if (rangedAttrHolder == null) {
            return fallback;
        }

        var attrInstance = shooter.getAttribute(rangedAttrHolder);
        if (attrInstance == null || divisor == 0.0D) {
            return fallback;
        }

        return (float) (attrInstance.getValue() / divisor);
    }

    private void spawnGraveMark(LivingEntity target, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        GravewireMarkEntity mark = new GravewireMarkEntity(EntityRegistry.GRAVEWIRE_MARK.get(), serverLevel);
        mark.setTargetUUID(target.getUUID());
        mark.setLifetime(config.grave_mark_lifetime_ticks);
        mark.setYOffset(config.grave_mark_y_offset);
        mark.setRiseAmount(config.grave_mark_rise_amount);
        mark.setPos(target.getX(), target.getY() + config.grave_mark_y_offset, target.getZ());
        serverLevel.addFreshEntity(mark);
    }

    private void spawnSoulRip(LivingEntity target, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                ParticleTypes.SOUL,
                target.getX(), target.getY() + config.soul_rip_height, target.getZ(),
                Math.max(0, config.soul_rip_particle_count),
                0.10D, 0.18D, 0.10D,
                config.soul_rip_speed
        );
        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(), target.getY() + config.soul_rip_height, target.getZ(),
                Math.max(3, config.soul_rip_particle_count / 4),
                0.06D, 0.10D, 0.06D,
                0.008D
        );
    }

    private void spawnGraveMist(LivingEntity target, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                ParticleTypes.ASH,
                target.getX(), target.getY() + config.grave_mist_y_offset, target.getZ(),
                Math.max(0, config.grave_mist_particle_count),
                config.grave_mist_radius, 0.03D, config.grave_mist_radius,
                0.005D
        );
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                target.getX(), target.getY() + config.grave_mist_y_offset, target.getZ(),
                Math.max(4, config.grave_mist_particle_count / 3),
                config.grave_mist_radius * 0.7D, 0.02D, config.grave_mist_radius * 0.7D,
                0.003D
        );
    }

    private void triggerGraveBloom(LivingEntity deadTarget, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float bloomDamage = resolveScaledDamage(
                deadTarget.level(),
                (float) config.grave_bloom_damage_base,
                config.use_ranged_damage_attribute_for_grave_bloom_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.grave_bloom_damage_attribute_divisor
        );

        if (config.grave_bloom_damage_scales_with_power_multiplier) {
            bloomDamage *= this.powerMultiplier;
        }

        final float finalBloomDamage = bloomDamage;
        double radius = Math.max(0.0D, config.grave_bloom_radius);

        deadTarget.level()
                .getEntities(this, deadTarget.getBoundingBox().inflate(radius), e -> e instanceof LivingEntity)
                .forEach(entity -> {
                    if (!(entity instanceof LivingEntity livingEntity)) {
                        return;
                    }
                    if (!config.grave_bloom_affects_primary_target && livingEntity == deadTarget) {
                        return;
                    }
                    if (!config.grave_bloom_affects_owner && livingEntity == this.getOwner()) {
                        return;
                    }
                    livingEntity.hurt(this.damageSources().magic(), finalBloomDamage);
                });

        serverLevel.sendParticles(
                ParticleTypes.SOUL,
                deadTarget.getX(), deadTarget.getY() + 0.9D, deadTarget.getZ(),
                Math.max(0, config.grave_bloom_particle_count),
                0.35D, 0.45D, 0.35D,
                config.grave_bloom_particle_speed
        );
        serverLevel.sendParticles(
                ParticleTypes.WITCH,
                deadTarget.getX(), deadTarget.getY() + 0.8D, deadTarget.getZ(),
                Math.max(8, config.grave_bloom_particle_count / 2),
                0.30D, 0.30D, 0.30D,
                0.02D
        );
        serverLevel.sendParticles(
                ParticleTypes.ASH,
                deadTarget.getX(), deadTarget.getY() + 0.05D, deadTarget.getZ(),
                Math.max(6, config.grave_bloom_particle_count / 3),
                0.40D, 0.05D, 0.40D,
                0.005D
        );
    }

    @Override
    public void tick() {
        super.tick();

        GravewireBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide() && config.trail_particles_enabled && particleTicksRemaining > 0) {
            createTrailParticles(config);
            particleTicksRemaining--;
        }
    }

    private void createImpactParticles(double x, double y, double z, GravewireBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.WITCH,
                x,
                y + config.impact_particle_base_y_offset,
                z,
                Math.max(0, config.impact_particle_count),
                config.impact_particle_offset_x,
                config.impact_particle_offset_y,
                config.impact_particle_offset_z,
                config.impact_particle_speed
        );
    }

    private void createTrailParticles(GravewireBowConfig config) {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < config.trail_stationary_speed_threshold) {
            if (config.spawn_stationary_trail_particle) {
                level().addParticle(
                        ParticleTypes.WITCH,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
            return;
        }

        double nx = -vel.x / speed;
        double ny = -vel.y / speed;
        double nz = -vel.z / speed;

        int steps = Math.max(1, config.trail_steps);
        double spacing = config.trail_spacing;

        for (int i = 0; i < steps; i++) {
            double px = this.getX() + nx * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;
            double py = this.getY() + ny * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;
            double pz = this.getZ() + nz * i * spacing + (random.nextDouble() - 0.5D) * config.trail_position_randomness;

            double vx = nx * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;
            double vy = ny * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;
            double vz = nz * config.trail_velocity_scale + (random.nextDouble() - 0.5D) * config.trail_velocity_randomness;

            level().addParticle(ParticleTypes.WITCH, px, py, pz, vx, vy, vz);
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
    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}
