package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Consumer;

public class VitalityArrow extends AbstractArrow {

    private Consumer<LivingEntity> onHitCallback;

    public VitalityArrow(EntityType<? extends VitalityArrow> entityType, Level level) {
        super(entityType, level);
    }

    public VitalityArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VITALITY_ARROW.get(), shooter, level, bowStack, arrowStack);
    }


    public void setOnHitCallback(Consumer<LivingEntity> callback) {
        this.onHitCallback = callback;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        LivingEntity target = result.getEntity() instanceof LivingEntity le ? le : null;
        if (target == null) {
            super.onHitEntity(result);
            return;
        }

        if (level().isClientSide()) {
            super.onHitEntity(result);
            return;
        }

        float before = target.getHealth();

        super.onHitEntity(result);

        float after = target.getHealth();
        float damageDealt = before - after;

        if (damageDealt > 0.0F && this.getOwner() instanceof LivingEntity shooter) {
            if (onHitCallback != null) {
                onHitCallback.accept(target);
            }

            float healAmount = damageDealt * 0.5F;
            shooter.heal(healAmount);

            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }


    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
