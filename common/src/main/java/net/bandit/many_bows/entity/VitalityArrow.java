package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.VitalityWeaverBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Consumer;

public class VitalityArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "vitality_weaver";

    private Consumer<LivingEntity> onHitCallback;

    public VitalityArrow(EntityType<? extends VitalityArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public VitalityArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.VITALITY_ARROW.get(), shooter, level);
        applyConfiguredValues();
    }

    private static VitalityWeaverBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, VitalityWeaverBowConfig.class, VitalityWeaverBowConfig::new);
    }

    private void applyConfiguredValues() {
        VitalityWeaverBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    public void setOnHitCallback(Consumer<LivingEntity> callback) {
        this.onHitCallback = callback;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        VitalityWeaverBowConfig config = config();
        // Captured before the vanilla hit, which may discard the arrow.
        float baseDamage = (float) this.getBaseDamage();

        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            LivingEntity shooter = this.getOwner() instanceof LivingEntity living ? living : null;

            if (shooter != null) {
                if (config.run_on_hit_callback && onHitCallback != null) {
                    onHitCallback.accept(target);
                }

                if (config.heal_shooter_on_hit) {
                    float healAmount = (float) (baseDamage * config.heal_percent_of_damage_dealt);

                    if (config.cap_heal_to_target_current_health) {
                        healAmount = Math.min(healAmount, target.getHealth());
                    }

                    healAmount = Math.min(healAmount, shooter.getMaxHealth() - shooter.getHealth());

                    if (healAmount > 0.0F) {
                        shooter.heal(healAmount);

                        if (config.heal_sound_enabled) {
                            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                                    config.heal_sound_volume, config.heal_sound_pitch);
                        }
                    }
                }
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
