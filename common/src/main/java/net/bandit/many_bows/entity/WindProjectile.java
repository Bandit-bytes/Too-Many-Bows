package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WindProjectile extends AbstractArrow {

    private static final float BASE_DAMAGE = 3.0f;

    // How big the gust radius is
    private static final double GUST_RADIUS = 4.0D;
    // How strongly entities are pushed
    private static final double KNOCKBACK_STRENGTH = 1.1D;

    // Duration of buffs on the shooter
    private static final int SHOOTER_SPEED_DURATION = 60;      // 3 seconds
    private static final int SHOOTER_SLOW_FALL_DURATION = 60;  // 3 seconds

    public WindProjectile(EntityType<? extends WindProjectile> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public WindProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WIND_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide()) {
            Entity hit = result.getEntity();
            if (hit instanceof LivingEntity target) {
                // Direct hit damage
                target.hurt(damageSources().magic(), (float) this.getBaseDamage());
            }

            // Trigger the gust at the hit position
            spawnGust(this.getX(), this.getY(), this.getZ());
        }

        this.discard();
    }

    /**
     * Creates an outward gust that:
     * - Pushes nearby entities away
     * - Lightly damages them
     * - Buffs the shooter if they are near the blast
     */
    private void spawnGust(double x, double y, double z) {
        Level level = this.level();
        if (level.isClientSide()) return;

        // Area to affect
        AABB area = new AABB(
                x - GUST_RADIUS, y - GUST_RADIUS, z - GUST_RADIUS,
                x + GUST_RADIUS, y + GUST_RADIUS, z + GUST_RADIUS
        );

        List<Entity> entities = level.getEntities(this, area, e -> e instanceof LivingEntity);
        Entity owner = this.getOwner();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) continue;

            // Optional: don't push the owner, we'll handle them separately
            if (owner != null && entity.getUUID().equals(owner.getUUID())) {
                continue;
            }

            // Vector from center to entity
            Vec3 dir = new Vec3(
                    entity.getX() - x,
                    0.0D,
                    entity.getZ() - z
            );

            if (dir.lengthSqr() > 0.0001D) {
                dir = dir.normalize().scale(KNOCKBACK_STRENGTH);
                entity.push(dir.x, 0.25D, dir.z);
                entity.hurt(this.damageSources().magic(), 1.5f);
            }
        }

        // If the shooter is close to the gust, give them a mobility buff
        if (owner instanceof LivingEntity livingOwner) {
            double distSqr = livingOwner.position().distanceToSqr(x, y, z);
            if (distSqr <= (GUST_RADIUS * GUST_RADIUS)) {
                livingOwner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SHOOTER_SPEED_DURATION, 1));
                livingOwner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SHOOTER_SLOW_FALL_DURATION, 0));
            }
        }

        // Extra gust particles (server-side spawn, clients see them)
        for (int i = 0; i < 30; i++) {
            double dx = x + (level.random.nextDouble() - 0.5D) * (GUST_RADIUS * 1.5D);
            double dy = y + level.random.nextDouble() * 1.5D;
            double dz = z + (level.random.nextDouble() - 0.5D) * (GUST_RADIUS * 1.5D);
            level.addParticle(ParticleTypes.CLOUD, dx, dy, dz, 0.0D, 0.05D, 0.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            // Simple wind trail
            level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0, 0.05, 0);
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
