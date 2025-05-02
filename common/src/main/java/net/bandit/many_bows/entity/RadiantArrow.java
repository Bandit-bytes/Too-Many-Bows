package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.bandit.many_bows.util.AncientSageDamageSource;
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

        level.getEntitiesOfClass(LivingEntity.class, explosionArea).forEach(entity -> {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                DamageSource radiantSource = AncientSageDamageSource.createRadiantDamage(level, arrow);
                if (livingEntity.isInvertedHealAndHarm()) {
                    livingEntity.hurt(radiantSource, 6.0F);
                } else {
                    livingEntity.hurt(radiantSource, 3.0F);
                }
            }
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
