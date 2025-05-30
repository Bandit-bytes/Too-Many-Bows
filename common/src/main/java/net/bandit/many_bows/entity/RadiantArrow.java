package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class RadiantArrow extends AbstractArrow {

    private boolean hasExploded = false; // Track if the explosion has already occurred

    public RadiantArrow(EntityType<? extends RadiantArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RadiantArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.RADIANT_ARROW.get(), shooter, level);
        this.setBaseDamage(9);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // Trigger explosion only if it hasn't occurred yet
        if (!hasExploded && result.getType() == HitResult.Type.BLOCK) {
            hasExploded = true; // Mark as exploded
            createRadiantExplosion(this.level(), this);
            this.discard(); // Remove the arrow after explosion
        }
    }

    private void createRadiantExplosion(Level level, RadiantArrow arrow) {
        // Create a visual explosion effect (doesn't break blocks)
        level.explode(null, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0F, Level.ExplosionInteraction.NONE);

        // Area of effect damage and healing
        AABB explosionArea = new AABB(
                arrow.getX() - 5, arrow.getY() - 5, arrow.getZ() - 5,
                arrow.getX() + 5, arrow.getY() + 5, arrow.getZ() + 5
        );

        level.getEntitiesOfClass(LivingEntity.class, explosionArea).forEach(entity -> {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Apply radiant damage
                DamageSource radiantSource = AncientSageDamageSource.createRadiantDamage(level, arrow);
                if (livingEntity.isInvertedHealAndHarm()) { // Check if undead
                    livingEntity.hurt(radiantSource, 6.0F); // Damage undead more
                } else {
                    livingEntity.hurt(radiantSource, 3.0F); // Damage others
                }
            }
        });
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // No item pickup
    }
}
