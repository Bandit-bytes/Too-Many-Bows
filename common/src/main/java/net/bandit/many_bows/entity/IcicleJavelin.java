package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.IcicleJavelinBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IcicleJavelin extends AbstractArrow {

    private static final String CONFIG_NAME = "cyroheart_bow";

    private boolean hasHit = false;
    private int hitTimer = 0;

    public IcicleJavelin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public IcicleJavelin(Level level, LivingEntity shooter) {
        super(EntityRegistry.ICICLE_JAVELIN.get(), shooter, level);
        applyConfiguredValues();
    }

    private static IcicleJavelinBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, IcicleJavelinBowConfig.class, IcicleJavelinBowConfig::new);
    }

    private void applyConfiguredValues() {
        IcicleJavelinBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.setPierceLevel((byte) Math.max(0, Math.min(127, config.pierce_level)));
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        IcicleJavelinBowConfig config = config();

        if (hasHit && config.discard_after_hit_delay) {
            hitTimer++;
            if (hitTimer >= Math.max(0, config.hit_discard_delay_ticks)) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            double speedFactor = config.trail_speed_factor;
            double spread = config.trail_random_offset_scale;
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < Math.max(0, config.trail_particle_steps); i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * spread;
                double yOffset = (this.random.nextDouble() - 0.5D) * spread;
                double zOffset = (this.random.nextDouble() - 0.5D) * spread;

                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + motion.x * i * speedFactor,
                        this.getY() + motion.y * i * speedFactor,
                        this.getZ() + motion.z * i * speedFactor,
                        xOffset, yOffset, zOffset);

                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            IcicleJavelinBowConfig config = config();

            if (config.apply_primary_target_slowness) {
                hitEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        config.primary_target_slowness_duration_ticks, config.primary_target_slowness_amplifier));
            }

            createFrostExplosion(result.getLocation(), hitEntity, config);

            if (config.start_hit_timer_on_entity_hit) {
                this.hasHit = true;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            IcicleJavelinBowConfig config = config();
            createFrostExplosion(result.getLocation(), null, config);

            if (config.start_hit_timer_on_block_hit) {
                this.hasHit = true;
            }
        }
    }

    private void createFrostExplosion(Vec3 position, @Nullable LivingEntity entityHit, IcicleJavelinBowConfig config) {
        if (config.aoe_slowness_enabled) {
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(Math.max(0.0D, config.aoe_radius)));
            for (LivingEntity entity : entities) {
                if (!config.aoe_affects_owner && entity == this.getOwner()) continue;
                if (!config.aoe_affects_primary_target && entity == entityHit) continue;

                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        config.aoe_slowness_duration_ticks, config.aoe_slowness_amplifier));
            }
        }

        if (config.frost_explosion_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
            sendBurst(serverLevel, ParticleTypes.SNOWFLAKE, position, config.snowflake_burst_particle_count, 1.0D, 0.5D, 0.1D);
            sendBurst(serverLevel, ParticleTypes.CLOUD, position, config.cloud_burst_particle_count, 1.0D, 0.25D, 0.0D);
        }

        if (config.impact_sound_enabled) {
            this.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.GLASS_BREAK, this.getSoundSource(), config.impact_sound_volume, config.impact_sound_pitch);
        }
    }

    private static void sendBurst(ServerLevel level, ParticleOptions particle, Vec3 position, int count,
                                  double spreadXZ, double spreadY, double speed) {
        if (count <= 0) return;
        level.sendParticles(particle, position.x, position.y + spreadY, position.z, count, spreadXZ / 2.0D, spreadY / 2.0D, spreadXZ / 2.0D, speed);
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
