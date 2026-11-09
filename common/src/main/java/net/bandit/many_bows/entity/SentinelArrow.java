package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SentinelWrathBowConfig;
import net.minecraft.server.level.ServerLevel;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.EntityHitResult;

public class SentinelArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "sentinel_wrath";

    public SentinelArrow(EntityType<? extends SentinelArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SentinelArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.SENTINEL_ARROW.get(), shooter, level);
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

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Captured before the vanilla hit, which may discard the arrow.
        float baseDamage = (float) this.getBaseDamage();

        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            SentinelWrathBowConfig config = config();
            float finalDamage = baseDamage;
            Entity owner = this.getOwner();

            if (config.bonus_damage_vs_raid_mobs_enabled && isRaidMob(target, config)) {
                finalDamage *= (float) config.raid_mob_damage_multiplier;

                if (config.impact_sound_enabled) {
                    level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ROAR,
                            SoundSource.PLAYERS, config.impact_sound_volume, config.impact_sound_pitch);
                }
                if (config.impact_particles_enabled && config.impact_particle_count > 0
                        && level() instanceof ServerLevel serverLevel) {
                    double spread = config.impact_particle_offset_scale / 2.0D;
                    serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                            config.impact_particle_count, spread, spread, spread, 0.0D);
                }
            }

            DamageSource damageSource = (owner instanceof LivingEntity living)
                    ? level().damageSources().arrow(this, living)
                    : level().damageSources().arrow(this, null);

            target.hurt(damageSource, finalDamage);
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

    private static boolean isRaidMob(LivingEntity entity, SentinelWrathBowConfig config) {
        String id = entity.getType().builtInRegistryHolder().key().location().toString();
        return config.raid_mob_whitelist.contains(id);
    }
}
