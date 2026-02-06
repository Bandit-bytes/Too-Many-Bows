package net.bandit.many_bows.entity;

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

    private static PowerParticleOption dragonBreath(float power) {
        return PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, power);
    }

    private float powerMultiplier = 1.0F;

    private static final int PARTICLE_LIFESPAN = 40;
    private static final int MAX_LIFETIME = 100;

    private int particleTicksRemaining = PARTICLE_LIFESPAN;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends DragonsBreathArrow> entityType, Level level) {
        super(entityType, level);
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(7.0);
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target)) return;

        Level level = target.level();

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

        float rangedDamage = 6.0F;
        if (this.getOwner() instanceof LivingEntity shooter) {
            var attrLookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedRefOpt = attrLookup.get(RANGED_WEAPON_DAMAGE_ID);
            if (rangedRefOpt.isPresent()) {
                var inst = shooter.getAttribute(rangedRefOpt.get());
                if (inst != null) rangedDamage = (float) inst.getValue();
            }
        }

        float aoeDamage = rangedDamage / 1.5F;
        level.getEntities(this, target.getBoundingBox().inflate(2.0D), e -> e instanceof LivingEntity)
                .forEach(e -> {
                    if (e instanceof LivingEntity le && le != target) {
                        le.hurt(target.damageSources().magic(), aoeDamage);
                    }
                });

        final float dotDamage = rangedDamage / 2.0F;
        final float power = this.powerMultiplier;

        AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ()) {
            int ticksExisted = 0;

            @Override
            public void tick() {
                super.tick();
                if (!this.level().isClientSide() && (++ticksExisted % 20 == 0)) {
                    this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).stream()
                            .filter(e -> e != getOwner() && this.distanceToSqr(e) <= (double) (this.getRadius() * this.getRadius()))
                            .forEach(e -> e.hurt(this.damageSources().magic(), dotDamage * power));
                }
            }
        };

        if (this.getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }

        cloud.setDuration(100);
        cloud.setWaitTime(10);
        cloud.setRadius(3.0F);
        cloud.setRadiusPerTick(-0.05F);
        cloud.setCustomParticle(dragonBreath(0.6F));

        level.addFreshEntity(cloud);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(
                    dragonBreath(0.8F),
                    target.getX(), target.getY(0.5), target.getZ(),
                    40,
                    0.8, 0.4, 0.8,
                    0.10
            );
        }
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

        if (this.level().isClientSide()) {
            if (!this.isInGround()) {
                createBreathPuffs();
            }

            if (!this.isInGround() && particleTicksRemaining > 0) {
                createTrailParticles();
                particleTicksRemaining--;
            }
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

            level().addParticle(dragonBreath(0.8F),
                    x + dx, y + 0.1D + dy, z + dz,
                    vx, vy, vz);
        }
    }

    private void createTrailParticles() {
        var vel = this.getDeltaMovement();
        double speed = vel.length();

        if (speed < 0.01) {
            level().addParticle(dragonBreath(0.8F), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
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

            level().addParticle(dragonBreath(0.8F), px, py, pz, vx, vy, vz);
        }
    }
    private void createBreathPuffs() {
        for (int i = 0; i < 2; i++) {
            double ox = Mth.nextDouble(this.random, -0.15, 0.15);
            double oy = Mth.nextDouble(this.random, -0.15, 0.15);
            double oz = Mth.nextDouble(this.random, -0.15, 0.15);

            level().addParticle(dragonBreath(0.8F),
                    this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    0.0D, -0.01D, 0.0D);
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
