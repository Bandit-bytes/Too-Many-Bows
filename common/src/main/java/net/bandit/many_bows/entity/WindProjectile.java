package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.WindBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WindProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "wind_bow";

    public WindProjectile(EntityType<? extends WindProjectile> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public WindProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.WIND_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static WindBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, WindBowConfig.class, WindBowConfig::new);
    }

    private void applyConfiguredValues() {
        WindBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        WindBowConfig config = config();

        if (!level().isClientSide()) {
            Entity hit = result.getEntity();
            if (config.bonus_magic_damage_on_direct_hit && hit instanceof LivingEntity target) {
                target.hurt(damageSources().magic(), (float) this.getBaseDamage());
            }

            spawnGust(this.getX(), this.getY(), this.getZ(), config);
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private void spawnGust(double x, double y, double z, WindBowConfig config) {
        Level level = this.level();
        if (level.isClientSide()) {
            return;
        }

        double radius = Math.max(0.0D, config.gust_radius);

        AABB area = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        List<Entity> entities = level.getEntities(this, area, e -> e instanceof LivingEntity);
        Entity owner = this.getOwner();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (!config.gust_affects_owner && owner != null && entity.getUUID().equals(owner.getUUID())) {
                continue;
            }

            Vec3 dir = new Vec3(
                    entity.getX() - x,
                    0.0D,
                    entity.getZ() - z
            );

            if (dir.lengthSqr() > 0.0001D) {
                if (config.gust_knockback_enabled) {
                    dir = dir.normalize().scale(config.gust_knockback_strength);
                    entity.push(dir.x, config.gust_vertical_push, dir.z);
                }

                if (config.gust_damage_enabled) {
                    entity.hurt(this.damageSources().magic(), (float) config.gust_damage);
                }
            }
        }

        if (config.owner_buff_enabled && owner instanceof LivingEntity livingOwner) {
            double distSqr = livingOwner.position().distanceToSqr(x, y, z);
            if (!config.owner_buff_requires_being_in_radius || distSqr <= (radius * radius)) {
                livingOwner.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        config.owner_speed_duration_ticks,
                        config.owner_speed_amplifier
                ));
                livingOwner.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        config.owner_slow_fall_duration_ticks,
                        config.owner_slow_fall_amplifier
                ));
            }
        }

        if (config.gust_particles_enabled) {
            for (int i = 0; i < Math.max(0, config.gust_particle_count); i++) {
                double dx = x + (level.random.nextDouble() - 0.5D) * (radius * config.gust_particle_radius_multiplier);
                double dy = y + level.random.nextDouble() * config.gust_particle_height;
                double dz = z + (level.random.nextDouble() - 0.5D) * (radius * config.gust_particle_radius_multiplier);

                level.addParticle(
                        ParticleTypes.CLOUD,
                        dx, dy, dz,
                        0.0D, config.gust_particle_speed_y, 0.0D
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        WindBowConfig config = config();

        if (level().isClientSide() && config.trail_particles_enabled) {
            for (int i = 0; i < Math.max(0, config.trail_particle_count); i++) {
                level().addParticle(
                        ParticleTypes.CLOUD,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0D, config.trail_particle_speed_y, 0.0D
                );
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
}