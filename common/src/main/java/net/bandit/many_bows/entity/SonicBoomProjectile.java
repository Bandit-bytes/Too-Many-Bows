package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SonicBoomBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SonicBoomProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "dark_bow";

    private float powerMultiplier = 1.0F;
    private int lifetime = 0;
    private int spiralTick = 0;

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static SonicBoomBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SonicBoomBowConfig.class, SonicBoomBowConfig::new);
    }

    private void applyConfiguredValues() {
        SonicBoomBowConfig config = config();
        this.setNoGravity(config.no_gravity);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            SonicBoomBowConfig config = config();

            if (config.sonic_damage_enabled) {
                float finalDamage = resolveSonicDamage(config);

                if (config.sonic_damage_scales_with_power_multiplier) {
                    finalDamage *= this.powerMultiplier;
                }

                target.hurt(damageSources().sonicBoom(this), finalDamage);
            }

            if (config.apply_knockback) {
                target.knockback(
                        config.knockback_strength,
                        Math.sin(this.getYRot() * Math.PI / 180.0F),
                        -Math.cos(this.getYRot() * Math.PI / 180.0F)
                );
            }

            if (config.hit_sound_enabled) {
                level().playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM,
                        SoundSource.PLAYERS,
                        config.hit_sound_volume,
                        config.hit_sound_pitch
                );
            }

            if (config.discard_after_entity_hit) {
                this.discard();
            }
        }
    }

    private float resolveSonicDamage(SonicBoomBowConfig config) {
        float scaledDamage = (float) config.sonic_damage_base;

        if (config.use_ranged_damage_attribute_for_sonic_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    scaledDamage = (float) (attrInstance.getValue() * config.ranged_damage_attribute_multiplier + config.ranged_damage_flat_bonus);
                }
            }
        }

        return scaledDamage;
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;
        spiralTick++;

        SonicBoomBowConfig config = config();

        if (!this.level().isClientSide && lifetime >= Math.max(0, config.max_lifetime_ticks)) {
            this.discard();
            return;
        }

        if (this.level().isClientSide && config.spiral_particles_enabled) {
            createSonicBoomSpiral(config);
        }
    }

    private void createSonicBoomSpiral(SonicBoomBowConfig config) {
        int particles = Math.max(0, config.spiral_particle_count);
        double radius = config.spiral_base_radius;

        for (int i = 0; i < particles; i++) {
            double angle = 2 * Math.PI * (i + spiralTick * config.spiral_angle_time_scale);
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = spiralTick * config.spiral_y_rise_per_tick - (i * config.spiral_y_offset_per_particle);

            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D
            );

            radius += config.spiral_expansion_rate;
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