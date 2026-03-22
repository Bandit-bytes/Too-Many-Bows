package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.BeaconBeamBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BeaconBeamArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "beacon_beam_bow";

    private Vec3 lastPos = null;

    public BeaconBeamArrow(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        applyConfiguredValues();
    }

    public BeaconBeamArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.BEACON_BEAM_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static BeaconBeamBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, BeaconBeamBowConfig.class, BeaconBeamBowConfig::new);
    }

    private void applyConfiguredValues() {
        BeaconBeamBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        BeaconBeamBowConfig config = config();

        if (this.level().isClientSide) {
            if (lastPos == null) {
                lastPos = this.position();
            }

            Vec3 now = this.position();
            Vec3 delta = now.subtract(lastPos);
            double len = delta.length();

            if (config.trail_particles_enabled && len > 0.0001D) {
                int baseSamples = Math.max(1, config.base_trail_samples);
                int maxExtra = Math.max(0, config.max_extra_trail_samples);

                int extra = 0;
                if (config.extra_samples_distance_divisor > 0.0D) {
                    extra = (int) Math.min(maxExtra, len / config.extra_samples_distance_divisor);
                }

                int samples = baseSamples + extra;
                spawnBeamTrail(lastPos, now, samples, ParticleTypes.ELECTRIC_SPARK, config);
            }

            lastPos = now;
            return;
        }

        lastPos = this.position();
    }

    private void spawnBeamTrail(Vec3 from, Vec3 to, int samples, ParticleOptions particle, BeaconBeamBowConfig config) {
        Level level = this.level();
        Vec3 diff = to.subtract(from);
        double spread = config.trail_spread;

        for (int i = 0; i <= samples; i++) {
            double t = (double) i / (double) samples;
            double x = from.x + diff.x * t;
            double y = from.y + diff.y * t;
            double z = from.z + diff.z * t;

            double ox = (this.random.nextDouble() - 0.5D) * spread;
            double oy = (this.random.nextDouble() - 0.5D) * spread;
            double oz = (this.random.nextDouble() - 0.5D) * spread;

            level.addParticle(particle, x + ox, y + oy, z + oz, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (this.level().isClientSide) {
            return;
        }

        BeaconBeamBowConfig config = config();

        if (hit.getEntity() instanceof LivingEntity target) {
            float baseDamage = (float) this.getBaseDamage();

            target.hurt(this.damageSources().arrow(this, this.getOwner()), baseDamage);

            if (config.link_damage_enabled) {
                doLinkDamage(target, baseDamage, config);
            }

            this.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS,
                    config.hit_sound_volume,
                    config.hit_sound_pitch
            );
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    private void doLinkDamage(LivingEntity primary, float baseDamage, BeaconBeamBowConfig config) {
        LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;

        double radius = Math.max(0.0D, config.link_radius);
        if (radius <= 0.0D) {
            return;
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                primary.getBoundingBox().inflate(radius),
                le -> le != primary && le != owner && le.isAlive()
        );

        int maxLinks = Math.max(0, config.max_links);
        if (maxLinks <= 0) {
            return;
        }

        float linkDamage = (float) Math.max(config.minimum_link_damage, baseDamage * config.link_damage_multiplier);

        int links = 0;
        for (LivingEntity other : nearby) {
            other.hurt(this.damageSources().magic(), linkDamage);
            links++;
            if (links >= maxLinks) {
                break;
            }
        }
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