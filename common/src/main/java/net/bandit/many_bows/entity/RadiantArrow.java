package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.RadiantBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class RadiantArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "radiant";

    private float powerMultiplier = 1.0F;
    private boolean hasExploded = false;

    public RadiantArrow(EntityType<? extends RadiantArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public RadiantArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.RADIANT_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static RadiantBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, RadiantBowConfig.class, RadiantBowConfig::new);
    }

    private void applyConfiguredValues() {
        RadiantBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (hasExploded || level().isClientSide()) {
            return;
        }

        RadiantBowConfig config = config();
        boolean shouldExplode =
                (result.getType() == HitResult.Type.BLOCK && config.explode_on_block_hit) ||
                        (result.getType() == HitResult.Type.ENTITY && config.explode_on_entity_hit);

        if (shouldExplode) {
            hasExploded = true;
            createRadiantExplosion(this.level(), this, config);

            if (config.discard_after_explosion) {
                this.discard();
            }
        }
    }

    private void createRadiantExplosion(Level level, RadiantArrow arrow, RadiantBowConfig config) {
        level.explode(
                null,
                arrow.getX(),
                arrow.getY(),
                arrow.getZ(),
                config.visual_explosion_power,
                Level.ExplosionInteraction.NONE
        );

        double radius = Math.max(0.0D, config.damage_radius);
        AABB explosionArea = new AABB(
                arrow.getX() - radius, arrow.getY() - radius, arrow.getZ() - radius,
                arrow.getX() + radius, arrow.getY() + radius, arrow.getZ() + radius
        );

        LivingEntity shooter = arrow.getOwner() instanceof LivingEntity livingShooter ? livingShooter : null;
        float scaledDamage = resolveExplosionDamage(level, shooter, config);

        level.getEntitiesOfClass(LivingEntity.class, explosionArea).forEach(entity -> {
            if (!config.affect_owner && entity == shooter) {
                return;
            }

            if (!config.affect_allies && shooter != null && entity.isAlliedTo(shooter)) {
                return;
            }

            if (entity.position().distanceTo(arrow.position()) > radius) {
                return;
            }

            if (config.explosion_damage_enabled) {
                float damage = entity.isInvertedHealAndHarm()
                        ? scaledDamage * (float) config.inverted_heal_and_harm_damage_multiplier
                        : scaledDamage;

                if (config.explosion_damage_scales_with_power_multiplier) {
                    damage *= this.powerMultiplier;
                }

                entity.hurt(AncientSageDamageSource.createRadiantDamage(level, arrow), damage);
            }
        });
    }

    private float resolveExplosionDamage(Level level, LivingEntity shooter, RadiantBowConfig config) {
        float scaledDamage = (float) config.base_explosion_damage;

        if (config.use_ranged_damage_attribute_for_explosion_damage && shooter != null) {
            var registry = level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null && config.ranged_damage_attribute_divisor != 0.0D) {
                    scaledDamage = (float) (attrInstance.getValue() / config.ranged_damage_attribute_divisor);
                }
            }
        }

        return scaledDamage;
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