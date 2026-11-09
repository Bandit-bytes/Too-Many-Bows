package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.ShulkerBlastBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class ShulkerBlastProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "shulker_blast";

    private int lifetime = 0;

    public ShulkerBlastProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public ShulkerBlastProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), shooter, level);
        applyConfiguredValues();
    }

    private static ShulkerBlastBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, ShulkerBlastBowConfig.class, ShulkerBlastBowConfig::new);
    }

    private void applyConfiguredValues() {
        ShulkerBlastBowConfig config = config();
        this.setNoGravity(config.no_gravity);
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        ShulkerBlastBowConfig config = config();

        lifetime++;
        if (config.max_lifetime_ticks > 0 && lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && config.homing_enabled) {
            LivingEntity target = findNearestTarget(config);
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dy = (target.getY() + target.getEyeHeight() / 2) - this.getY();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0) {
                    double homingFactor = config.homing_factor;
                    double speed = config.homing_speed;
                    this.setDeltaMovement(
                            this.getDeltaMovement().x * (1.0 - homingFactor) + (dx / distance) * speed * homingFactor,
                            this.getDeltaMovement().y * (1.0 - homingFactor) + (dy / distance) * speed * homingFactor,
                            this.getDeltaMovement().z * (1.0 - homingFactor) + (dz / distance) * speed * homingFactor
                    );
                }
            }
        }

        if (config.trail_particles_enabled) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    private LivingEntity findNearestTarget(ShulkerBlastBowConfig config) {
        AABB searchBox = this.getBoundingBox().inflate(Math.max(0.0D, config.homing_range));
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity.isAlive()
                        && !(config.exclude_owner && entity == this.getOwner())
                        && !(config.exclude_armor_stands && entity instanceof ArmorStand)
                        && !(config.exclude_tamed_animals && entity instanceof TamableAnimal tamable && tamable.isTame())
        );
        return entities.stream()
                .min((e1, e2) -> Double.compare(e1.distanceTo(this), e2.distanceTo(this)))
                .orElse(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        ShulkerBlastBowConfig config = config();

        // Let AbstractArrow apply the configured base damage, Power, Punch and Flame once.
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            if (config.apply_levitation) {
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                        config.levitation_duration_ticks, config.levitation_amplifier));
            }

            if (config.impact_sound_enabled) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SHULKER_BULLET_HIT, SoundSource.PLAYERS,
                        config.impact_sound_volume, config.impact_sound_pitch);
            }

            if (config.impact_particles_enabled && config.impact_particle_count > 0
                    && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        this.getX(), this.getY() + config.impact_particle_offset_y / 2.0D, this.getZ(),
                        config.impact_particle_count,
                        config.impact_particle_offset_xz / 2.0D, config.impact_particle_offset_y / 2.0D,
                        config.impact_particle_offset_xz / 2.0D, 0.0D);
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
}
