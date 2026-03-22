package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AethersCallBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class AethersCallArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "aethers_call";

    private int lifetime = 0;

    public AethersCallArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public AethersCallArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AETHERS_CALL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static AethersCallBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AethersCallBowConfig.class, AethersCallBowConfig::new);
    }

    private void applyConfiguredValues() {
        AethersCallBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        AethersCallBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide && config.trail_particles_enabled) {
            for (int i = 0; i < Math.max(0, config.trail_particles_per_tick); i++) {
                level().addParticle(
                        ParticleTypes.END_ROD,
                        this.getX(),
                        this.getY() + config.trail_particle_offset_y,
                        this.getZ(),
                        config.trail_particle_velocity_x,
                        config.trail_particle_velocity_y,
                        config.trail_particle_velocity_z
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        triggerAetherBurst();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        triggerAetherBurst();
    }

    private void triggerAetherBurst() {
        if (this.level().isClientSide) {
            return;
        }

        AethersCallBowConfig config = config();
        Level level = this.level();

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        level.playSound(
                null,
                x, y, z,
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.PLAYERS,
                config.burst_sound_volume,
                config.burst_sound_pitch
        );

        double radius = Math.max(0.0D, config.burst_radius);

        if (radius > 0.0D) {
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius)
            );

            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;

            for (LivingEntity target : entities) {
                if (owner != null && target == owner) {
                    if (config.owner_slow_falling_enabled) {
                        target.addEffect(new MobEffectInstance(
                                MobEffects.SLOW_FALLING,
                                config.owner_slow_falling_duration_ticks,
                                config.owner_slow_falling_amplifier,
                                false,
                                true
                        ));
                    }
                    continue;
                }

                if (config.target_levitation_enabled) {
                    target.addEffect(new MobEffectInstance(
                            MobEffects.LEVITATION,
                            config.target_levitation_duration_ticks,
                            config.target_levitation_amplifier,
                            false,
                            true
                    ));
                }
            }
        }

        if (config.burst_particles_enabled && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x,
                    y,
                    z,
                    Math.max(0, config.burst_particle_count),
                    config.burst_particle_offset_x,
                    config.burst_particle_offset_y,
                    config.burst_particle_offset_z,
                    config.burst_particle_speed
            );
        }

        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}