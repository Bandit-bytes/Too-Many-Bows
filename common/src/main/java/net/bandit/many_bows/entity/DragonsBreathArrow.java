package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

public class DragonsBreathArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private static final int PARTICLE_LIFESPAN = 40;
    private static final int MAX_LIFETIME = 100;
    private int particleTicksRemaining = PARTICLE_LIFESPAN;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(7.0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) return;

        Level level = target.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

        float scaledDamage;

        if (this.getOwner() instanceof LivingEntity shooter) {
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    scaledDamage = (float) attrInstance.getValue() / 1.5F;
                } else {
                    scaledDamage = 4.0F;
                }
            } else {
                scaledDamage = 4.0F;
            }
        } else {
            scaledDamage = 4.0F;
        }

        level.getEntities(this, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity && livingEntity != target) {
                        livingEntity.hurt(target.damageSources().magic(), scaledDamage);
                    }
                });

        float dotDamage; // fallback
        if (this.getOwner() instanceof LivingEntity shooter) {
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);
            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    dotDamage = (float) attrInstance.getValue() / 2F; // DoT scaling
                } else {
                    dotDamage = 6.0F;
                }
            } else {
                dotDamage = 6.0F;
            }
        } else {
            dotDamage = 6.0F;
        }
        final float power = this.powerMultiplier;
        AreaEffectCloud damagingCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ()) {
            int ticksExisted = 0;

            @Override
            public void tick() {
                super.tick();
                if (++ticksExisted % 20 == 0 && !this.level().isClientSide) {
                    this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).stream()
                            .filter(e -> e != getOwner() && this.distanceToSqr(e) <= this.getRadius() * this.getRadius())
                            .forEach(e -> e.hurt(this.damageSources().magic(), dotDamage * power));
                }
            }
        };

        damagingCloud.setOwner((LivingEntity) this.getOwner());
        damagingCloud.setDuration(100);
        damagingCloud.setWaitTime(10);
        damagingCloud.setRadius(3.0F);
        damagingCloud.setRadiusPerTick(-0.05F);
        damagingCloud.setParticle(ParticleTypes.DRAGON_BREATH);

        level.addFreshEntity(damagingCloud);
        createImpactParticles(target.getX(), target.getY(), target.getZ());

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (level().isClientSide() && particleTicksRemaining > 0) {
            createTrailParticles();
            particleTicksRemaining--;
        }
    }

    private void createImpactParticles(double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            double dx = (random.nextDouble() - 0.5D) * 0.8D;
            double dy = random.nextDouble() * 0.4D;
            double dz = (random.nextDouble() - 0.5D) * 0.8D;

            double vx = dx * 0.10;
            double vy = dy * 0.10;
            double vz = dz * 0.10;

            level().addParticle(ParticleTypes.DRAGON_BREATH,
                    x + dx, y + 0.1D + dy, z + dz,
                    vx, vy, vz);
        }
    }

    private void createTrailParticles() {
        var vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < 0.01) {
            level().addParticle(ParticleTypes.DRAGON_BREATH,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, 0.0D, 0.0D);
            return;
        }

        double nx = -vel.x / speed;
        double ny = -vel.y / speed;
        double nz = -vel.z / speed;

        int steps = 10;
        double spacing = 0.15;

        for (int i = 0; i < steps; i++) {
            double px = this.getX() + nx * i * spacing + (random.nextDouble() - 0.5) * 0.05;
            double py = this.getY() + ny * i * spacing + (random.nextDouble() - 0.5) * 0.05;
            double pz = this.getZ() + nz * i * spacing + (random.nextDouble() - 0.5) * 0.05;

            double vx = nx * 0.02 + (random.nextDouble() - 0.5) * 0.01;
            double vy = ny * 0.02 + (random.nextDouble() - 0.5) * 0.01;
            double vz = nz * 0.02 + (random.nextDouble() - 0.5) * 0.01;

            level().addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, vx, vy, vz);
        }
    }


    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }
}
