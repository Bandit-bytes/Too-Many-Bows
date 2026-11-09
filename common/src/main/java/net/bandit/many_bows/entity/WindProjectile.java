package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.WindBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class WindProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "wind_bow";

    public WindProjectile(EntityType<? extends WindProjectile> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public WindProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.WIND_PROJECTILE.get(), shooter, level);
        applyConfiguredValues();
    }

    private static WindBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, WindBowConfig.class, WindBowConfig::new);
    }

    private void applyConfiguredValues() {
        WindBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        WindBowConfig config = config();
        // Captured before the vanilla hit, which may discard the arrow.
        float bonusDamage = (float) this.getBaseDamage();

        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (config.ignore_owner && target == this.getOwner()) {
                return;
            }

            if (config.bonus_magic_damage_on_direct_hit) {
                target.hurt(damageSources().magic(), bonusDamage);
            }

            if (config.levitation_enabled && !target.hasEffect(MobEffects.LEVITATION)) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                        config.levitation_duration_ticks, config.levitation_amplifier));
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() && config().trail_particles_enabled) {
            level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
                    0, config().trail_particle_speed_y, 0);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}
