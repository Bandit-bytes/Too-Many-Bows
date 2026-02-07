package net.bandit.many_bows.entity;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SonicBoomProjectile extends AbstractArrow {

    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    private int lifetime = 60;
    private int tickCount = 0;

    private static final Identifier RANGED_WEAPON_DAMAGE_ID =
            Identifier.fromNamespaceAndPath("ranged_weapon", "damage");

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        this.setNoGravity(true);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (level().isClientSide()) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;

        LivingEntity shooter = (this.getOwner() instanceof LivingEntity le) ? le : null;

        float scaledDamage = 20.0F;

        if (shooter != null) {
            var attrLookup = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedRefOpt = attrLookup.get(RANGED_WEAPON_DAMAGE_ID);

            if (rangedRefOpt.isPresent()) {
                var inst = shooter.getAttribute(rangedRefOpt.get());
                if (inst != null) {
                    scaledDamage = (float) inst.getValue() * 1.5F + 12.0F;
                }
            }
        }

        float finalDamage = scaledDamage * this.powerMultiplier;

        target.hurt(damageSources().sonicBoom(this), finalDamage);
        target.knockback(
                2.0F,
                Math.sin(this.getYRot() * Math.PI / 180.0F),
                -Math.cos(this.getYRot() * Math.PI / 180.0F)
        );

        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.0F);

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        if (!this.level().isClientSide() && --this.lifetime <= 0) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            createWardenSonicBoomBeam();
            createSonicBoomSpiral();
        }
    }

    private void createSonicBoomSpiral() {
        int particles = 18;
        double radius = 0.25;
        double spiralExpansionRate = 0.06;

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * (i + tickCount * 0.15);

            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = (i * 0.01);

            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D
            );

            radius += spiralExpansionRate;
        }
    }
    private void createWardenSonicBoomBeam() {
        var vel = this.getDeltaMovement();
        Vec3 dir;

        if (vel.lengthSqr() > 0.0001) {
            dir = vel.normalize();
        } else {
            dir = Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        }

        Vec3 start = this.position().add(dir.scale(0.35));

        double length = 2.6;
        double step = 0.35;

        for (double d = 0.0; d <= length; d += step) {
            Vec3 p = start.add(dir.scale(d));

            this.level().addParticle(
                    ParticleTypes.SONIC_BOOM,
                    p.x, p.y, p.z,
                    0.0D, 0.0D, 0.0D
            );
        }
    }


    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}
