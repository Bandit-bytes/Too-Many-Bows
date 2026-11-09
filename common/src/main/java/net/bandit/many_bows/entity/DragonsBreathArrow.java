package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.DragonsBreathBowConfig;
import net.bandit.many_bows.util.AttributeHelper;
import net.minecraft.server.level.ServerLevel;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

public class DragonsBreathArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "dragons_breath";

    private int particleTicksRemaining = 40;
    private int lifetime = 0;

    public DragonsBreathArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public DragonsBreathArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.DRAGONS_BREATH_ARROW.get(), shooter, level);
        applyConfiguredValues();
    }

    private static DragonsBreathBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, DragonsBreathBowConfig.class, DragonsBreathBowConfig::new);
    }

    private void applyConfiguredValues() {
        DragonsBreathBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.particleTicksRemaining = Math.max(0, config.trail_particle_lifespan_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        DragonsBreathBowConfig config = config();

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            Level level = target.level();

            if (config.impact_sound_enabled) {
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS,
                        config.impact_sound_volume, config.impact_sound_pitch);
            }

            if (config.splash_damage_enabled) {
                float splashDamage = resolveSplashDamage(config);
                level.getEntities(this, target.getBoundingBox().inflate(Math.max(0.0D, config.splash_radius)),
                                e -> e instanceof LivingEntity)
                        .forEach(entity -> {
                            if (entity == target) return;
                            if (!config.splash_damage_affects_owner && entity == this.getOwner()) return;
                            entity.hurt(target.damageSources().magic(), splashDamage);
                        });
            }

            if (config.damage_cloud_enabled) {
                AreaEffectCloud areaEffectCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
                if (this.getOwner() instanceof LivingEntity owner) {
                    areaEffectCloud.setOwner(owner);
                }
                areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                areaEffectCloud.setRadius(config.cloud_radius);
                areaEffectCloud.setDuration(config.cloud_duration_ticks);
                areaEffectCloud.setRadiusPerTick(config.cloud_radius_per_tick);
                areaEffectCloud.setWaitTime(config.cloud_wait_time_ticks);
                areaEffectCloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.HARM, 40, config.cloud_instant_damage_amplifier));

                level.addFreshEntity(areaEffectCloud);
            }

            if (config.impact_particles_enabled && level instanceof ServerLevel serverLevel && config.impact_particle_count > 0) {
                double spread = config.impact_particle_spread;
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.0F),
                        target.getX(), target.getY() + spread / 2.0D, target.getZ(),
                        config.impact_particle_count, spread / 2.0D, spread / 2.0D, spread / 2.0D, 0.0D);
            }
        }

        if (!this.level().isClientSide() && config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private float resolveSplashDamage(DragonsBreathBowConfig config) {
        float damage = (float) config.splash_damage_base;

        if (config.use_ranged_damage_attribute_for_splash_damage && config.splash_damage_attribute_divisor != 0.0D
                && this.getOwner() instanceof LivingEntity owner) {
            Double value = AttributeHelper.getValue(owner,
                    config.ranged_damage_attribute_namespace, config.ranged_damage_attribute_path);
            if (value != null) {
                damage = (float) (value / config.splash_damage_attribute_divisor);
            }
        }

        return damage;
    }

    @Override
    public void tick() {
        super.tick();

        DragonsBreathBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide() && config.trail_particles_enabled && particleTicksRemaining > 0) {
            level().addParticle(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY(), this.getZ(), 0.0D, -0.05D, 0.0D);
            particleTicksRemaining--;
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
