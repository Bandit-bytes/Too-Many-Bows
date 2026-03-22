package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.SonicBoomBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SonicBoomProjectile extends AbstractArrow {

    private static final Identifier RANGED_WEAPON_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    private float powerMultiplier = 1.0F;
    private int visualTick = 0;
    private int lifetime = 0;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private SonicBoomBowConfig config() {
        return SonicBoomBowConfig.get();
    }

    private void applyConfigValues() {
        SonicBoomBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.setNoGravity(!config.use_gravity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        SonicBoomBowConfig config = config();
        LivingEntity shooter = (this.getOwner() instanceof LivingEntity le) ? le : null;

        float damage = (float) config.base_damage;

        if (config.use_ranged_damage_attribute_scaling && shooter != null) {
            var attrLookup = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedRefOpt = attrLookup.get(RANGED_WEAPON_DAMAGE_ID);

            if (rangedRefOpt.isPresent()) {
                var inst = shooter.getAttribute(rangedRefOpt.get());
                if (inst != null) {
                    damage = (float) inst.getValue() * config.ranged_damage_multiplier + config.flat_bonus_damage;
                }
            }
        }

        float finalDamage = damage * this.powerMultiplier * config.final_damage_multiplier;

        if (finalDamage > 0.0F) {
            target.hurt(damageSources().sonicBoom(this), finalDamage);
        }

        target.knockback(
                config.knockback_strength,
                Math.sin(this.getYRot() * Math.PI / 180.0F),
                -Math.cos(this.getYRot() * Math.PI / 180.0F)
        );

        level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS,
                config.impact_sound_volume,
                config.impact_sound_pitch
        );

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        visualTick++;
        lifetime++;

        if (!this.level().isClientSide() && lifetime >= config().max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            SonicBoomBowConfig config = config();

            if (config.beam_particles_enabled) {
                createWardenSonicBoomBeam(config.beam_length, config.beam_step);
            }

            if (config.spiral_particles_enabled) {
                createSonicBoomSpiral(
                        config.spiral_particle_count,
                        config.spiral_start_radius,
                        config.spiral_expansion_rate
                );
            }
        }
    }

    private void createSonicBoomSpiral(int particles, double startRadius, double expansionRate) {
        double radius = startRadius;

        for (int i = 0; i < particles; i++) {
            double angle = 2.0D * Math.PI * (i + visualTick * 0.15D);

            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = i * 0.01D;

            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D
            );

            radius += expansionRate;
        }
    }

    private void createWardenSonicBoomBeam(double length, double step) {
        Vec3 vel = this.getDeltaMovement();
        Vec3 dir;

        if (vel.lengthSqr() > 0.0001D) {
            dir = vel.normalize();
        } else {
            dir = Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        }

        Vec3 start = this.position().add(dir.scale(0.35D));

        for (double d = 0.0D; d <= length; d += step) {
            Vec3 p = start.add(dir.scale(d));

            this.level().addParticle(
                    ParticleTypes.SONIC_BOOM,
                    p.x, p.y, p.z,
                    0.0D, 0.0D, 0.0D
            );
        }
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