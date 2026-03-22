package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SentinelWrathBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SentinelWrathArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "sentinel_wrath";

    private float powerMultiplier = 1.0F;

    public SentinelWrathArrow(EntityType<? extends SentinelWrathArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SentinelWrathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SENTINEL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static SentinelWrathBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SentinelWrathBowConfig.class, SentinelWrathBowConfig::new);
    }

    private void applyConfiguredValues() {
        SentinelWrathBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            SentinelWrathBowConfig config = config();

            if (config.bonus_damage_vs_raid_mobs_enabled && isConfiguredRaidMob(target, config)) {
                float scaledDamage = resolveBonusDamage(config);

                if (config.bonus_damage_scales_with_power_multiplier) {
                    scaledDamage *= this.powerMultiplier;
                }

                DamageSource damageSource = this.getOwner() instanceof LivingEntity
                        ? this.level().damageSources().arrow(this, this.getOwner())
                        : this.level().damageSources().arrow(this, null);

                target.hurt(damageSource, scaledDamage);

                if (config.impact_sound_enabled) {
                    this.level().playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.RAVAGER_ROAR,
                            SoundSource.PLAYERS,
                            config.impact_sound_volume,
                            config.impact_sound_pitch
                    );
                }

                if (config.impact_particles_enabled) {
                    for (int i = 0; i < Math.max(0, config.impact_particle_count); i++) {
                        double xOffset = (this.random.nextDouble() - 0.5D) * config.impact_particle_offset_scale;
                        double yOffset = (this.random.nextDouble() - 0.5D) * config.impact_particle_offset_scale;
                        double zOffset = (this.random.nextDouble() - 0.5D) * config.impact_particle_offset_scale;

                        this.level().addParticle(
                                ParticleTypes.CRIT,
                                this.getX() + xOffset,
                                this.getY() + yOffset,
                                this.getZ() + zOffset,
                                0.0D, 0.0D, 0.0D
                        );
                    }
                }
            }
        }
    }

    private float resolveBonusDamage(SentinelWrathBowConfig config) {
        float scaledDamage = (float) config.bonus_damage_base;

        if (config.use_ranged_damage_attribute_for_bonus_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);

            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    scaledDamage = (float) (attrInstance.getValue() * config.ranged_damage_attribute_multiplier);
                }
            }
        }

        return scaledDamage;
    }

    private boolean isConfiguredRaidMob(LivingEntity entity, SentinelWrathBowConfig config) {
        String id = entity.getType().builtInRegistryHolder().key().location().toString();
        return config.raid_mob_whitelist.contains(id);
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