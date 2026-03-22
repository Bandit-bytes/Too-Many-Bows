package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AncientSageBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AncientSageArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "ancient_sage_bow";

    private float powerMultiplier = 1.0F;
    private float armorPenetration = 0.33F;
    private int particleTicksRemaining = 60;

    public AncientSageArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public AncientSageArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ANCIENT_SAGE_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static AncientSageBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AncientSageBowConfig.class, AncientSageBowConfig::new);
    }

    private void applyConfiguredValues() {
        AncientSageBowConfig config = config();

        this.armorPenetration = (float) config.default_armor_penetration_factor;
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public void setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        AncientSageBowConfig config = config();

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (config.bonus_penetration_damage_enabled) {
                float baseDamage = (float) this.getBaseDamage();
                float resolvedPenetration = resolveArmorPenetration(config);
                float multiplier = config.bonus_penetration_scales_with_power_multiplier ? this.powerMultiplier : 1.0F;
                float bonusDamage = baseDamage * resolvedPenetration * multiplier;

                if (bonusDamage > 0.0F) {
                    target.hurt(AncientSageDamageSource.create(this.level(), this, this.getOwner()), bonusDamage);
                }
            }

            createHitParticles(config);
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private float resolveArmorPenetration(AncientSageBowConfig config) {
        float resolved = this.armorPenetration;

        if (config.use_ranged_damage_attribute_for_penetration && this.getOwner() instanceof Player player) {
            ResourceLocation attributeId = ResourceLocation.fromNamespaceAndPath(
                    config.ranged_damage_attribute_namespace,
                    config.ranged_damage_attribute_path
            );

            Holder<Attribute> rangedDamageAttr = this.level().registryAccess()
                    .registryOrThrow(Registries.ATTRIBUTE)
                    .getHolder(attributeId)
                    .orElse(null);

            if (rangedDamageAttr != null) {
                AttributeInstance attrInstance = player.getAttribute(rangedDamageAttr);
                if (attrInstance != null) {
                    double divisor = config.ranged_damage_attribute_divisor;
                    if (divisor != 0.0D) {
                        resolved = (float) (attrInstance.getValue() / divisor);
                    }
                }
            }
        }

        float min = (float) config.min_armor_penetration_factor;
        float max = (float) config.max_armor_penetration_factor;

        if (min > max) {
            float temp = min;
            min = max;
            max = temp;
        }

        return Math.min(Math.max(resolved, min), max);
    }

    @Override
    public void tick() {
        super.tick();

        AncientSageBowConfig config = config();

        if (config.trail_particles_enabled && particleTicksRemaining > 0) {
            createTrailParticles(config);
            particleTicksRemaining--;
        }
    }

    private void createHitParticles(AncientSageBowConfig config) {
        if (!config.hit_particles_enabled) {
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    Math.max(0, config.hit_particle_count),
                    config.hit_particle_offset_x,
                    config.hit_particle_offset_y,
                    config.hit_particle_offset_z,
                    config.hit_particle_speed
            );
        }
    }

    private void createTrailParticles(AncientSageBowConfig config) {
        int count = Math.max(0, config.trail_particles_per_tick);

        for (int i = 0; i < count; i++) {
            level().addParticle(
                    ParticleTypes.GLOW,
                    this.getX(),
                    this.getY() + config.trail_particle_offset_y,
                    this.getZ(),
                    config.trail_particle_velocity_x,
                    config.trail_particle_velocity_y,
                    config.trail_particle_velocity_z
            );
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