package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VaultpiercerBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class VaultpiercerArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "vaultpiercer";

    private float powerMultiplier = 1.0F;
    private int particleTicksRemaining = 40;
    private int lifetime = 0;

    private boolean followUpProjectile = false;
    private boolean portalStrikeEnabled = true;

    private UUID homingTargetUUID;
    private int homingTicksRemaining = 0;

    public VaultpiercerArrow(EntityType<? extends VaultpiercerArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfiguredValues();
    }

    public VaultpiercerArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VAULTPIERCER_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfiguredValues();
    }

    private static VaultpiercerBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, VaultpiercerBowConfig.class, VaultpiercerBowConfig::new);
    }

    private void applyConfiguredValues() {
        VaultpiercerBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    public void setFollowUpProjectile(boolean followUpProjectile) {
        this.followUpProjectile = followUpProjectile;
    }

    public void setPortalStrikeEnabled(boolean portalStrikeEnabled) {
        this.portalStrikeEnabled = portalStrikeEnabled;
    }

    public void setHomingTarget(UUID homingTargetUUID, int homingTicks) {
        this.homingTargetUUID = homingTargetUUID;
        this.homingTicksRemaining = homingTicks;
    }

    public boolean isPickupAllowedByConfig() {
        return config().allow_pickup;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        VaultpiercerBowConfig config = config();
        Level level = target.level();

        if (!level.isClientSide()) {
            if (config.impact_sound_enabled) {
                level.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundRegistry.VAULT_PORTAL_IMPACT.get(),
                        SoundSource.PLAYERS,
                        config.impact_sound_volume,
                        followUpProjectile ? 1.25F : config.impact_sound_pitch
                );
            }

            if (!followUpProjectile) {
                applyMarkEffects(target, config);
                spawnPortalVolley(target, config);
            } else if (config.follow_up_impact_burst_enabled) {
                doFollowUpBurst(target, config);
            }

            if (config.impact_particles_enabled) {
                createImpactParticles(target.getX(), target.getY(), target.getZ(), config);
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private void applyMarkEffects(LivingEntity target, VaultpiercerBowConfig config) {
        if (!config.apply_mark_on_hit) {
            return;
        }

        if (config.mark_glowing_duration_ticks > 0) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    config.mark_glowing_duration_ticks,
                    0,
                    false,
                    true
            ));
        }

        if (config.mark_slowness_duration_ticks > 0) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    config.mark_slowness_duration_ticks,
                    Math.max(0, config.mark_slowness_amplifier),
                    false,
                    true
            ));
        }
    }

    private void spawnPortalVolley(LivingEntity target, VaultpiercerBowConfig config) {
        if (!config.portal_strike_enabled || !portalStrikeEnabled || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int portalCount = Math.max(1, config.base_portal_count);
        if (this.powerMultiplier >= config.full_charge_threshold) {
            portalCount += Math.max(0, config.bonus_portal_count_at_full_charge);
        }

        if (config.portal_open_sound_enabled) {
            serverLevel.playSound(
                    null,
                    target.getX(),
                    target.getY() + 1.0D,
                    target.getZ(),
                    SoundRegistry.VAULT_PORTAL_OPEN.get(),
                    SoundSource.PLAYERS,
                    config.portal_open_sound_volume,
                    config.portal_open_sound_pitch
            );
        }

        for (int i = 0; i < portalCount; i++) {
            double angle = ((Math.PI * 2.0D) / portalCount) * i;
            double xOffset = Math.cos(angle) * config.portal_radius;
            double zOffset = Math.sin(angle) * config.portal_radius;
            double yOffset = config.portal_height + ((i % 2 == 0) ? 0.25D : -0.10D);

            VaultPortalEntity portal = new VaultPortalEntity(EntityRegistry.VAULT_PORTAL.get(), serverLevel);
            portal.setOwnerUUID(this.getOwner() instanceof LivingEntity living ? living.getUUID() : null);
            portal.setTargetUUID(target.getUUID());
            portal.setOffset(xOffset, yOffset, zOffset);
            portal.setWarmupTicks(config.portal_warmup_ticks + (i * Math.max(0, config.portal_stagger_ticks)));
            portal.setLifetimeTicks(config.portal_lifetime_ticks + (i * Math.max(0, config.portal_stagger_ticks)));
            portal.setTrackTarget(config.portal_tracks_target);
            portal.setPowerMultiplier(this.powerMultiplier);
            portal.setPortalIndex(i);
            portal.setPos(target.getX() + xOffset, target.getY() + yOffset, target.getZ() + zOffset);
            portal.setYRot(0.0F);
            portal.setXRot(0.0F);

            serverLevel.addFreshEntity(portal);
        }
    }

    private void doFollowUpBurst(LivingEntity target, VaultpiercerBowConfig config) {
        float burstDamage = resolveScaledDamage(
                target.level(),
                (float) config.follow_up_impact_burst_damage_base,
                config.use_ranged_damage_attribute_for_burst_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.follow_up_impact_burst_damage_attribute_divisor
        );

        if (config.follow_up_impact_burst_scales_with_power_multiplier) {
            burstDamage *= this.powerMultiplier;
        }

        final float finalBurstDamage = burstDamage;
        double radius = Math.max(0.0D, config.follow_up_impact_burst_radius);

        target.level().getEntities(this, target.getBoundingBox().inflate(radius), e -> e instanceof LivingEntity)
                .forEach(entity -> {
                    if (!(entity instanceof LivingEntity livingEntity)) {
                        return;
                    }

                    if (!config.burst_affects_primary_target && livingEntity == target) {
                        return;
                    }

                    if (!config.burst_affects_owner && livingEntity == this.getOwner()) {
                        return;
                    }

                    livingEntity.hurt(this.damageSources().magic(), finalBurstDamage);
                });
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

    @Override
    public void tick() {
        super.tick();

        VaultpiercerBowConfig config = config();

        if (!level().isClientSide() && followUpProjectile && config.follow_up_homing_enabled && homingTicksRemaining > 0) {
            tickHoming(config);
        }

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

    private void tickHoming(VaultpiercerBowConfig config) {
        homingTicksRemaining--;

        if (!(level() instanceof ServerLevel serverLevel) || homingTargetUUID == null) {
            return;
        }

        Entity entity = serverLevel.getEntity(homingTargetUUID);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        Vec3 aimPos = target.getBoundingBox().getCenter().add(0.0D, 0.15D, 0.0D);
        Vec3 desired = aimPos.subtract(this.position()).normalize().scale(config.follow_up_projectile_velocity);
        Vec3 current = this.getDeltaMovement();

        double strength = Math.max(0.0D, config.follow_up_homing_strength);
        Vec3 newMotion = current.scale(1.0D - strength).add(desired.scale(strength));

        this.setDeltaMovement(newMotion);

        if (config.follow_up_no_gravity) {
            this.setNoGravity(true);
        }
    }

    private void createImpactParticles(double x, double y, double z, VaultpiercerBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                x,
                y + config.impact_particle_base_y_offset,
                z,
                Math.max(0, config.impact_particle_count),
                config.impact_particle_offset_x,
                config.impact_particle_offset_y,
                config.impact_particle_offset_z,
                config.impact_particle_speed
        );

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                x,
                y + config.impact_particle_base_y_offset,
                z,
                Math.max(4, config.impact_particle_count / 3),
                0.12D,
                0.12D,
                0.12D,
                0.01D
        );
    }

    private void createTrailParticles(VaultpiercerBowConfig config) {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < config.trail_stationary_speed_threshold) {
            if (config.spawn_stationary_trail_particle) {
                level().addParticle(ParticleTypes.ENCHANT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
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

            level().addParticle(ParticleTypes.ENCHANT, px, py, pz, vx, vy, vz);

            if (i % 2 == 0) {
                level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.0D, 0.0D);
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
    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}
