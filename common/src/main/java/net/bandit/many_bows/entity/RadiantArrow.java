package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RadiantArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private boolean hasExploded = false;

    public RadiantArrow(EntityType<? extends RadiantArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RadiantArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.RADIANT_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!hasExploded && (result.getType() == HitResult.Type.BLOCK || result.getType() == HitResult.Type.ENTITY)) {
            hasExploded = true;
            createRadiantExplosion(this.level(), this);
            this.discard();
        }
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }

    private void createRadiantExplosion(Level level, RadiantArrow arrow) {
        level.explode(null, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0F, Level.ExplosionInteraction.NONE);

        AABB explosionArea = new AABB(
                arrow.getX() - 5, arrow.getY() - 5, arrow.getZ() - 5,
                arrow.getX() + 5, arrow.getY() + 5, arrow.getZ() + 5
        );

        float scaledDamage;

        LivingEntity shooter = arrow.getOwner() instanceof LivingEntity livingShooter ? livingShooter : null;

        if (shooter != null) {
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                scaledDamage = attrInstance != null ? (float) attrInstance.getValue() / 2.0F : 3.0F;
            } else {
                scaledDamage = 3.0F;
            }
        } else {
            scaledDamage = 3.0F;
        }

        // Damage valid targets only
        level.getEntitiesOfClass(LivingEntity.class, explosionArea).forEach(entity -> {
            // Skip self and allies
            if (entity == shooter || (shooter != null && entity.isAlliedTo(shooter))) return;

            float damage = entity.isInvertedHealAndHarm() ? scaledDamage * 2.0F * this.powerMultiplier : scaledDamage * this.powerMultiplier;
            entity.hurt(AncientSageDamageSource.createRadiantDamage(level, arrow), damage);
        });
    }



    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }


    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
