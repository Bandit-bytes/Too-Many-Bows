package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.WindBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WindProjectile extends AbstractArrow {

    private int lifetime = 0;

    public WindProjectile(EntityType<? extends WindProjectile> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public WindProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WIND_PROJECTILE.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private WindBowConfig config() {
        return WindBowConfig.get();
    }

    private void applyConfigValues() {
        WindBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        WindBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            for (int i = 0; i < config.trail_particle_count; i++) {
                level().addParticle(
                        ParticleTypes.CLOUD,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D,
                        0.05D,
                        0.0D
                );
            }
        }
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

        float directDamage = (float) config().base_damage;
        if (directDamage > 0.0F) {
            target.hurt(this.damageSources().arrow(this, this.getOwner()), directDamage);
        }

        spawnGust(this.getX(), this.getY(), this.getZ());
        this.discard();
    }

    private void spawnGust(double x, double y, double z) {
        Level level = this.level();
        if (level.isClientSide()) {
            return;
        }

        WindBowConfig config = config();

        AABB area = new AABB(
                x - config.gust_radius, y - config.gust_radius, z - config.gust_radius,
                x + config.gust_radius, y + config.gust_radius, z + config.gust_radius
        );

        List<Entity> entities = level.getEntities(this, area, e -> e instanceof LivingEntity);
        Entity owner = this.getOwner();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (owner != null && entity.getUUID().equals(owner.getUUID())) {
                continue;
            }

            Vec3 dir = new Vec3(
                    entity.getX() - x,
                    0.0D,
                    entity.getZ() - z
            );

            if (dir.lengthSqr() > 0.0001D) {
                dir = dir.normalize().scale(config.knockback_strength);
                entity.push(dir.x, 0.25D, dir.z);

                if (config.gust_bonus_damage > 0.0F) {
                    entity.hurt(this.damageSources().magic(), config.gust_bonus_damage);
                }
            }
        }

        if (config.buff_owner_if_in_radius && owner instanceof LivingEntity livingOwner) {
            double distSqr = livingOwner.position().distanceToSqr(x, y, z);
            if (distSqr <= (config.gust_radius * config.gust_radius)) {
                livingOwner.addEffect(new MobEffectInstance(
                        MobEffects.SPEED,
                        config.owner_speed_duration_ticks,
                        config.owner_speed_amplifier
                ));
                livingOwner.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        config.owner_slow_falling_duration_ticks,
                        config.owner_slow_falling_amplifier
                ));
            }
        }

        if (config.gust_particles_enabled) {
            for (int i = 0; i < config.gust_particle_count; i++) {
                double dx = x + (level.getRandom().nextDouble() - 0.5D) * (config.gust_radius * 1.5D);
                double dy = y + level.getRandom().nextDouble() * 1.5D;
                double dz = z + (level.getRandom().nextDouble() - 0.5D) * (config.gust_radius * 1.5D);

                level.addParticle(
                        ParticleTypes.CLOUD,
                        dx,
                        dy,
                        dz,
                        0.0D,
                        0.05D,
                        0.0D
                );
            }
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

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}