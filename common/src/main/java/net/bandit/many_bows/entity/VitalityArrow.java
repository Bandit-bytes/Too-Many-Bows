package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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

import java.util.function.Consumer;

public class VitalityArrow extends AbstractArrow {

    private Consumer<LivingEntity> onHitCallback;

    public VitalityArrow(EntityType<? extends VitalityArrow> entityType, Level level) {
        super(entityType, level);
    }

    public VitalityArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.VITALITY_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    /**
     * Sets a callback to be executed when the arrow hits a living entity.
     */
    public void setOnHitCallback(Consumer<LivingEntity> callback) {
        this.onHitCallback = callback;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            LivingEntity shooter = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;

            if (shooter != null) {
                DamageSource damageSource = this.level().damageSources().arrow(this, shooter);
                float damageDealt = (float) this.getBaseDamage();
                boolean didDamage = target.hurt(damageSource, damageDealt);

                if (didDamage && damageDealt > 0) {
                    if (onHitCallback != null) {
                        onHitCallback.accept(target);
                    }

                    float healAmount = Math.min(damageDealt * 0.5F, target.getHealth());
                    shooter.heal(healAmount);
                    level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
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
