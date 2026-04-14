package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SoulhoardBowConfig;
import net.bandit.many_bows.item.SoulhoardBow;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.registry.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SoulhoardArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "soulhoard_bow";

    private static final EntityDataAccessor<Integer> DATA_HOARDED_SOULS =
            SynchedEntityData.defineId(SoulhoardArrow.class, EntityDataSerializers.INT);

    private float powerMultiplier = 1.0F;
    private int particleTicksRemaining = 40;
    private int lifetime = 0;
    private boolean releasedSkulls = false;
    private UUID sourceBowId;
    private boolean soulLanternEmpowered = false;

    public SoulhoardArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues(null);
    }

    public SoulhoardArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SOULHOARD_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues(shooter);
    }
    public void setSoulLanternEmpowered(boolean soulLanternEmpowered) {
        this.soulLanternEmpowered = soulLanternEmpowered;
    }

    private static SoulhoardBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SoulhoardBowConfig.class, SoulhoardBowConfig::new);
    }

    private void applyConfiguredValues(LivingEntity shooter) {
        SoulhoardBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public void setSourceBowId(UUID bowId) {
        this.sourceBowId = bowId;
    }

    public UUID getSourceBowId() {
        return this.sourceBowId;
    }

    public void setHoardedSouls(int souls) {
        int clamped = Math.max(0, souls);
        this.entityData.set(DATA_HOARDED_SOULS, clamped);

        if (clamped > 0) {
            SoulhoardBowConfig config = config();

            double perSoul = soulLanternEmpowered && config.soul_lantern_synergy_enabled
                    ? config.soul_lantern_bonus_arrow_damage_per_soul
                    : config.bonus_arrow_damage_per_soul;

            double totalBonus = clamped * perSoul;

            if (soulLanternEmpowered && config.soul_lantern_synergy_enabled) {
                totalBonus += config.soul_lantern_flat_release_bonus_damage;
            }

            this.setBaseDamage(this.getBaseDamage() + totalBonus);
        }
    }

    public int getHoardedSouls() {
        return this.entityData.get(DATA_HOARDED_SOULS);
    }

    public boolean isEmpowered() {
        return this.getHoardedSouls() > 0;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        LivingEntity target = result.getEntity() instanceof LivingEntity living ? living : null;

        super.onHitEntity(result);

        SoulhoardBowConfig config = config();

        if (!level().isClientSide) {
            if (target != null) {
                if (target.isAlive()) {
                    createImpactParticles(target.getX(), target.getY(), target.getZ(), config);
                } else {
                    createImpactParticles(target.getX(), target.getY(), target.getZ(), config);
                    awardSoulIfKilled(target, config);
                }
            }

            if (target != null && isEmpowered() && !releasedSkulls) {
                LivingEntity preferredTarget = target.isAlive() ? target : null;
                Vec3 spawnPos = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
                releaseStoredSkulls(spawnPos, preferredTarget, 0, config);
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        SoulhoardBowConfig config = config();

        if (!level().isClientSide && isEmpowered() && !releasedSkulls) {
            Vec3 hitPos = result.getLocation().add(0.0D, 0.10D, 0.0D);
            releaseStoredSkulls(hitPos, null, config.skull_hover_ticks_on_block_hit, config);
            this.discard();
        }
    }

    private void releaseStoredSkulls(Vec3 origin, LivingEntity preferredTarget, int hoverDelay, SoulhoardBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int skullCount = Math.max(0, getHoardedSouls());

        if (this.soulLanternEmpowered && config.soul_lantern_synergy_enabled) {
            skullCount += Math.max(0, config.soul_lantern_bonus_extra_skulls);
        }

        if (skullCount <= 0) {
            return;
        }

        float skullDamage = resolveScaledDamage(
                serverLevel,
                (float) config.skull_damage_base,
                config.use_ranged_damage_attribute_for_skull_damage,
                config.ranged_damage_attribute_namespace,
                config.ranged_damage_attribute_path,
                config.skull_damage_attribute_divisor
        );

        if (config.skull_damage_scales_with_power_multiplier) {
            skullDamage *= this.powerMultiplier;
        }

        if (this.soulLanternEmpowered && config.soul_lantern_synergy_enabled) {
            skullDamage *= (float) config.soul_lantern_skull_damage_multiplier;
        }

        UUID ownerUUID = this.getOwner() != null ? this.getOwner().getUUID() : null;
        UUID preferredTargetUUID = preferredTarget != null ? preferredTarget.getUUID() : null;

        for (int i = 0; i < skullCount; i++) {
            HoardedSkullEntity skull = new HoardedSkullEntity(EntityRegistry.HOARDED_SKULL.get(), serverLevel);

            skull.setPos(origin.x, origin.y, origin.z);
            skull.setOwnerUUID(ownerUUID);
            skull.setSourceBowId(this.sourceBowId);
            skull.setPreferredTargetUUID(preferredTargetUUID);
            skull.setLifetime(config.skull_lifetime_ticks);
            skull.setHitsRemaining(config.skull_max_hits);
            skull.setDamage(skullDamage);
            skull.setSeekRadius(config.skull_seek_radius);
            skull.setSpeed(config.skull_speed);
            skull.setSteering(config.skull_steering);
            skull.setBurnSeconds(config.skull_burn_seconds);
            skull.setHoverDelay(hoverDelay);
            skull.setRenderScale(0.65F + (0.06F * i));

            double angle = ((Math.PI * 2.0D) / Math.max(1, skullCount)) * i;
            Vec3 outward = new Vec3(Math.cos(angle), 0.25D, Math.sin(angle)).scale(0.10D);
            skull.setDeltaMovement(outward);

            serverLevel.addFreshEntity(skull);
        }

        releasedSkulls = true;
        this.entityData.set(DATA_HOARDED_SOULS, 0);

        serverLevel.playSound(
                null,
                origin.x,
                origin.y,
                origin.z,
                SoundRegistry.SOULHOARD_RELEASE.get(),
                SoundSource.PLAYERS,
                0.95F,
                1.0F
        );
    }

    private void awardSoulIfKilled(LivingEntity target, SoulhoardBowConfig config) {
        if (!(this.getOwner() instanceof Player player)) {
            return;
        }

        if (this.sourceBowId == null) {
            return;
        }

        boolean awarded = SoulhoardBow.awardSoulsToMatchingBow(
                player,
                this.sourceBowId,
                Math.max(1, config.souls_gained_per_kill)
        );

        if (awarded && config.play_harvest_sound) {
            level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundRegistry.SOULHOARD_HARVEST.get(),
                    SoundSource.PLAYERS,
                    config.harvest_sound_volume,
                    config.harvest_sound_pitch
            );
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

        var registry = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        Holder<Attribute> rangedAttrHolder = registry.getHolder(
                ResourceLocation.fromNamespaceAndPath(attributeNamespace, attributePath)
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

        SoulhoardBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide && config.trail_particles_enabled && particleTicksRemaining > 0) {
            createTrailParticles(config);
            particleTicksRemaining--;
        }
    }

    private void createImpactParticles(double x, double y, double z, SoulhoardBowConfig config) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
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
                ParticleTypes.SOUL,
                x,
                y + config.impact_particle_base_y_offset,
                z,
                Math.max(6, config.impact_particle_count / 2),
                config.impact_particle_offset_x * 0.65D,
                config.impact_particle_offset_y * 0.65D,
                config.impact_particle_offset_z * 0.65D,
                config.impact_particle_speed * 0.5D
        );
    }

    private void createTrailParticles(SoulhoardBowConfig config) {
        Vec3 vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < config.trail_stationary_speed_threshold) {
            if (config.spawn_stationary_trail_particle) {
                level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
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

            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, vx, vy, vz);

            if (this.getHoardedSouls() > 0 && random.nextBoolean()) {
                level().addParticle(ParticleTypes.SOUL, px, py, pz, 0.0D, 0.0D, 0.0D);
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HOARDED_SOULS, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HoardedSouls", this.getHoardedSouls());
        tag.putFloat("PowerMultiplier", this.powerMultiplier);
        tag.putInt("Lifetime", this.lifetime);
        tag.putBoolean("ReleasedSkulls", this.releasedSkulls);
        tag.putBoolean("SoulLanternEmpowered", this.soulLanternEmpowered);

        if (this.sourceBowId != null) {
            tag.putUUID("SourceBowId", this.sourceBowId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_HOARDED_SOULS, Math.max(0, tag.getInt("HoardedSouls")));
        this.powerMultiplier = tag.getFloat("PowerMultiplier");
        this.lifetime = tag.getInt("Lifetime");
        this.releasedSkulls = tag.getBoolean("ReleasedSkulls");
        this.soulLanternEmpowered = tag.getBoolean("SoulLanternEmpowered");

        if (tag.hasUUID("SourceBowId")) {
            this.sourceBowId = tag.getUUID("SourceBowId");
        }
    }
}