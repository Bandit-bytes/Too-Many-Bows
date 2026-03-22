package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.DragonsBreathBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DragonsBreathArrow extends AbstractArrow {

    private static final Identifier RANGED_WEAPON_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    private float powerMultiplier = 1.0F;
    private int particleTicksRemaining = 40;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends DragonsBreathArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private DragonsBreathBowConfig config() {
        return DragonsBreathBowConfig.get();
    }

    private void applyConfigValues() {
        DragonsBreathBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.particleTicksRemaining = config.trail_particle_lifespan_ticks;
    }

    private static PowerParticleOption dragonBreath(float power) {
        return PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, power);
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        Level level = target.level();
        if (level.isClientSide()) {
            return;
        }

        DragonsBreathBowConfig config = config();

        float directDamage = (float) config.base_damage * this.powerMultiplier;
        if (directDamage > 0.0F) {
            target.hurt(this.damageSources().arrow(this, this.getOwner()), directDamage);
        }

        level.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.PLAYERS,
                config.impact_sound_volume,
                config.impact_sound_pitch
        );

        float rangedDamage = config.ranged_damage_fallback;
        if (config.use_ranged_damage_attribute_for_area_damage && this.getOwner() instanceof LivingEntity shooter) {
            var attrLookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedRefOpt = attrLookup.get(RANGED_WEAPON_DAMAGE_ID);

            if (rangedRefOpt.isPresent()) {
                var inst = shooter.getAttribute(rangedRefOpt.get());
                if (inst != null) {
                    rangedDamage = (float) inst.getValue();
                }
            }
        }

        float aoeDamage = rangedDamage * config.aoe_damage_multiplier * this.powerMultiplier;
        if (aoeDamage > 0.0F) {
            level.getEntities(this, target.getBoundingBox().inflate(config.aoe_radius), e -> e instanceof LivingEntity)
                    .forEach(e -> {
                        if (e instanceof LivingEntity living && living != target && living != this.getOwner()) {
                            living.hurt(target.damageSources().magic(), aoeDamage);
                        }
                    });
        }

        if (config.spawn_damage_cloud) {
            final float dotDamage = rangedDamage * config.cloud_dot_damage_multiplier * this.powerMultiplier;

            AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ()) {
                int ticksExisted = 0;

                @Override
                public void tick() {
                    super.tick();

                    if (!this.level().isClientSide() && (++ticksExisted % 20 == 0) && dotDamage > 0.0F) {
                        this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).stream()
                                .filter(e -> e != getOwner() && this.distanceToSqr(e) <= (double) (this.getRadius() * this.getRadius()))
                                .forEach(e -> e.hurt(this.damageSources().magic(), dotDamage));
                    }
                }
            };

            if (this.getOwner() instanceof LivingEntity owner) {
                cloud.setOwner(owner);
            }

            cloud.setDuration(config.cloud_duration_ticks);
            cloud.setWaitTime(config.cloud_wait_time_ticks);
            cloud.setRadius(config.cloud_radius);
            cloud.setRadiusPerTick(config.cloud_radius_per_tick);
            cloud.setCustomParticle(dragonBreath(0.6F));

            level.addFreshEntity(cloud);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    dragonBreath(0.8F),
                    target.getX(),
                    target.getY(0.5D),
                    target.getZ(),
                    config.impact_particle_count,
                    0.8D,
                    0.4D,
                    0.8D,
                    0.10D
            );
        }

        if (config.discard_on_hit) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        DragonsBreathBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            if (!this.isInGround() && config.breath_puff_particles_enabled) {
                createBreathPuffs(config.breath_puff_particles_per_tick);
            }

            if (!this.isInGround() && config.trail_particles_enabled && particleTicksRemaining > 0) {
                createTrailParticles(config.trail_particle_steps);
                particleTicksRemaining--;
            }
        }
    }

    private void createTrailParticles(int steps) {
        var vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < 0.01D) {
            level().addParticle(dragonBreath(0.8F), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }

        double nx = -vel.x / speed;
        double ny = -vel.y / speed;
        double nz = -vel.z / speed;
        double spacing = 0.15D;

        for (int i = 0; i < steps; i++) {
            double px = this.getX() + nx * i * spacing + (random.nextDouble() - 0.5D) * 0.05D;
            double py = this.getY() + ny * i * spacing + (random.nextDouble() - 0.5D) * 0.05D;
            double pz = this.getZ() + nz * i * spacing + (random.nextDouble() - 0.5D) * 0.05D;

            double vx = nx * 0.02D + (random.nextDouble() - 0.5D) * 0.01D;
            double vy = ny * 0.02D + (random.nextDouble() - 0.5D) * 0.01D;
            double vz = nz * 0.02D + (random.nextDouble() - 0.5D) * 0.01D;

            level().addParticle(dragonBreath(0.8F), px, py, pz, vx, vy, vz);
        }
    }

    private void createBreathPuffs(int count) {
        for (int i = 0; i < count; i++) {
            double ox = Mth.nextDouble(this.random, -0.15D, 0.15D);
            double oy = Mth.nextDouble(this.random, -0.15D, 0.15D);
            double oz = Mth.nextDouble(this.random, -0.15D, 0.15D);

            level().addParticle(
                    dragonBreath(0.8F),
                    this.getX() + ox,
                    this.getY() + oy,
                    this.getZ() + oz,
                    0.0D,
                    -0.01D,
                    0.0D
            );
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