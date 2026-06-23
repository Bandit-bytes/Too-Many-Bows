package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.SolarBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SolarArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private boolean tornadoActive = false;
    private int tornadoTick = 0;
    private Vec3 tornadoOrigin = null;
    private int lifetime = 0;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public SolarArrow(EntityType<? extends SolarArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public SolarArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SOLAR_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private SolarBowConfig config() {
        return SolarBowConfig.get();
    }

    private void applyConfigValues() {
        SolarBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        SolarBowConfig config = config();
        lifetime++;

        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide()) {
            return;
        }

        if (tornadoActive && tornadoOrigin != null) {
            this.setPos(tornadoOrigin.x, tornadoOrigin.y, tornadoOrigin.z);

            if (level() instanceof ServerLevel serverLevel && config.spawn_tornado_particles) {
                spawnRagingTornado(serverLevel, tornadoOrigin, tornadoTick);
            }

            LivingEntity shooter = this.getOwner() instanceof LivingEntity le ? le : null;
            float scaledDamage = config.tornado_damage_fallback;

            if (config.use_ranged_damage_attribute_scaling && shooter != null && config.ranged_damage_divisor > 0.0F) {
                var registry = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
                var rangedAttrHolder = registry.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

                if (rangedAttrHolder != null) {
                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
                    if (attrInstance != null) {
                        scaledDamage = (float) attrInstance.getValue() / config.ranged_damage_divisor;
                    }
                }
            }

            float finalDamage = scaledDamage * this.powerMultiplier * config.final_tornado_damage_multiplier;

            AABB area = new AABB(
                    tornadoOrigin.x - config.tornado_radius,
                    tornadoOrigin.y - config.tornado_radius,
                    tornadoOrigin.z - config.tornado_radius,
                    tornadoOrigin.x + config.tornado_radius,
                    tornadoOrigin.y + config.tornado_radius,
                    tornadoOrigin.z + config.tornado_radius
            );

            level().getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
                if (config.exclude_owner && entity == shooter) {
                    return;
                }
                if (config.exclude_allies_of_owner && shooter != null && entity.isAlliedTo(shooter)) {
                    return;
                }

                if (config.tornado_fire_ticks > 0) {
                    entity.setRemainingFireTicks(config.tornado_fire_ticks);
                }

                if (finalDamage > 0.0F) {
                    entity.hurt(level().damageSources().magic(), finalDamage);
                }
            });

            tornadoTick++;
            if (tornadoTick > config.tornado_duration_ticks) {
                this.discard();
                return;
            }
        } else if (!tornadoActive && this.isInGround()) {
            startTornado();
        } else if (!tornadoActive && level() instanceof ServerLevel serverLevel && config.spawn_air_spiral_particles) {
            spawnAirSpiral(serverLevel, this.position(), tornadoTick);
            tornadoTick++;
        }

        if (config.ambient_sound_interval_ticks > 0 && tornadoTick % config.ambient_sound_interval_ticks == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    this.getSoundSource(),
                    config.ambient_sound_volume,
                    config.ambient_sound_pitch_min + this.random.nextFloat() * (config.ambient_sound_pitch_max - config.ambient_sound_pitch_min)
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide() && !tornadoActive) {
            startTornado();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide() && !tornadoActive) {
            startTornado();
        }
    }

    private void startTornado() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        SolarBowConfig config = config();

        this.tornadoActive = true;
        this.tornadoOrigin = this.position();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setInvisible(true);

        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY(),
                this.getZ(),
                config.startup_explosion_particles,
                0.3D,
                0.1D,
                0.3D,
                0.05D
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                this.getSoundSource(),
                config.startup_sound_volume,
                config.startup_sound_pitch
        );
    }

    private void spawnRagingTornado(ServerLevel level, Vec3 center, int age) {
        int height = Math.min(age, 40);
        int spiralCount = 6;
        float maxRadius = 3.5F;

        for (int y = 0; y < height; y++) {
            float progress = (float) y / 40.0F;
            float radius = maxRadius * (0.2F + 0.8F * progress);
            double baseY = center.y + y * 0.2D;

            for (int i = 0; i < spiralCount; i++) {
                double angle = age * 0.25D + i * (2.0D * Math.PI / spiralCount) + y * 0.3D;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                level.sendParticles(ParticleTypes.FLAME, x, baseY, z, 2, 0.0D, 0.0D, 0.0D, 0.0D);

                if (y % 5 == 0 && i % 2 == 0) {
                    level.sendParticles(ParticleTypes.LAVA, x, baseY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                if (y % 7 == 0 && i % 3 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, x, baseY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    private void spawnAirSpiral(ServerLevel level, Vec3 center, int age) {
        float radius = 1.2F;
        int points = 20;

        for (int i = 0; i < points; i++) {
            double angle = age * 0.3D + i * (2.0D * Math.PI / points);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.1D;

            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
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