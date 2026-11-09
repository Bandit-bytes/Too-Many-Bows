package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.NecroFlameBowConfig;
import net.bandit.many_bows.util.AttributeHelper;
import net.minecraft.world.effect.MobEffects;
import net.bandit.many_bows.registry.EffectRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class CursedFlameArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "necro_flame";

    private int particleTimer = 0;

    public CursedFlameArrow(EntityType<? extends CursedFlameArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public CursedFlameArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.CURSED_FLAME_ARROW.get(), shooter, level);
        applyConfiguredValues();
    }

    private static NecroFlameBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, NecroFlameBowConfig.class, NecroFlameBowConfig::new);
    }

    private void applyConfiguredValues() {
        NecroFlameBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    public void tick() {
        super.tick();

        NecroFlameBowConfig config = config();

        if (this.inGround) {
            particleTimer++;
        }
        if (config.trail_particles_enabled
                && particleTimer < config.max_trail_particle_duration_ticks
                && this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            double step = config.trail_position_step_multiplier;
            double spread = config.trail_random_offset_scale;
            for (int i = 0; i < Math.max(0, config.trail_particle_count); i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * spread;
                double yOffset = (this.random.nextDouble() - 0.5D) * spread;
                double zOffset = (this.random.nextDouble() - 0.5D) * spread;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + motion.x * i * step,
                        this.getY() + motion.y * i * step,
                        this.getZ() + motion.z * i * step,
                        xOffset, yOffset, zOffset);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            NecroFlameBowConfig config = config();

            if (config.set_target_on_fire) {
                hitEntity.setRemainingFireTicks(Math.max(hitEntity.getRemainingFireTicks(), config.fire_ticks_on_hit));
            }

            if (config.bonus_fire_damage_enabled) {
                float fireDamage = resolveFireDamage(config);
                if (fireDamage > 0.0F) {
                    hitEntity.hurt(this.damageSources().onFire(), fireDamage);
                }
            }

            if (config.remove_regeneration_on_hit) {
                hitEntity.removeEffect(MobEffects.REGENERATION);
            }

            if (config.apply_cursed_flame_effect) {
                hitEntity.addEffect(new MobEffectInstance(EffectRegistry.CURSED_FLAME.get(),
                        config.cursed_flame_duration_ticks, config.cursed_flame_amplifier,
                        false, config.cursed_flame_show_particles, config.cursed_flame_show_icon));
            }

            if (config.entity_hit_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
                Vec3 position = hitEntity.position();
                serverLevel.sendParticles(ParticleTypes.SOUL, position.x, position.y + config.entity_hit_particle_offset_y, position.z,
                        Math.max(0, config.entity_hit_particle_count),
                        config.entity_hit_particle_offset_x / 2.0D, config.entity_hit_particle_offset_y,
                        config.entity_hit_particle_offset_z / 2.0D, config.entity_hit_particle_speed);
            }
        }
    }

    private float resolveFireDamage(NecroFlameBowConfig config) {
        float damage = (float) config.base_fire_damage;

        if (config.use_ranged_damage_attribute_for_fire_damage && config.ranged_damage_attribute_divisor != 0.0D
                && this.getOwner() instanceof LivingEntity shooter) {
            Double value = AttributeHelper.getValue(shooter,
                    config.ranged_damage_attribute_namespace, config.ranged_damage_attribute_path);
            if (value != null) {
                damage = (float) (value / config.ranged_damage_attribute_divisor);
            }
        }

        return damage;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            NecroFlameBowConfig config = config();
            Vec3 position = result.getLocation();

            if (config.block_hit_particles_enabled) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, position.x, position.y, position.z,
                        Math.max(0, config.block_hit_particle_count),
                        config.block_hit_particle_offset_x, config.block_hit_particle_offset_y,
                        config.block_hit_particle_offset_z, config.block_hit_particle_speed);
            }

            if (config.block_hit_sound_enabled) {
                serverLevel.playSound(null, position.x, position.y, position.z, SoundEvents.SOUL_ESCAPE,
                        this.getSoundSource(), config.block_hit_sound_volume, config.block_hit_sound_pitch);
            }

            if (config.stop_trail_particles_after_block_hit) {
                particleTimer = config.max_trail_particle_duration_ticks;
            }
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
