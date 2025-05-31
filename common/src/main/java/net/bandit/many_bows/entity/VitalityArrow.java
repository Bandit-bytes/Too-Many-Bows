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

    public VitalityArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.VITALITY_ARROW.get(), shooter, level);
    }

    public void setOnHitCallback(Consumer<LivingEntity> callback) {
        this.onHitCallback = callback;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            LivingEntity shooter = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;

            if (shooter != null) {
                if (onHitCallback != null) {
                    onHitCallback.accept(target);
                }

                float maxHeal = (float) (this.getBaseDamage() * 0.5f);
                float actualHeal = Math.min(maxHeal, shooter.getMaxHealth() - shooter.getHealth());

                if (actualHeal > 0f) {
                    shooter.heal(actualHeal);
                    level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);
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
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
