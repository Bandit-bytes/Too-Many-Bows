package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.BeaconBeamBowConfig;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BeaconBeamArrow extends AbstractArrow {

    private Vec3 lastPos = null;
    private int lifetime = 0;

    public BeaconBeamArrow(EntityType<? extends BeaconBeamArrow> type, Level level) {
        super(type, level);
        applyConfigValues();
    }

    public BeaconBeamArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.BEACON_BEAM_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private BeaconBeamBowConfig config() {
        return BeaconBeamBowConfig.get();
    }

    private void applyConfigValues() {
        BeaconBeamBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        BeaconBeamBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.level().isClientSide() && config.trail_particles_enabled) {
            if (lastPos == null) {
                lastPos = this.position();
            }

            Vec3 now = this.position();
            Vec3 delta = now.subtract(lastPos);
            double len = delta.length();

            if (len > 0.0001D) {
                int extra = (int) Math.min(6, len / 0.75D);
                int samples = config.trail_particle_sample_count + extra;
                spawnBeamTrail(lastPos, now, samples, ParticleTypes.ELECTRIC_SPARK, config.trail_particle_spread);
            }

            lastPos = now;
            return;
        }

        lastPos = this.position();
    }

    private void spawnBeamTrail(Vec3 from, Vec3 to, int samples, ParticleOptions particle, float spread) {
        Level level = this.level();
        Vec3 diff = to.subtract(from);

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
        if (this.level().isClientSide()) {
            return;
        }

        if (hit.getEntity() instanceof LivingEntity target) {
            BeaconBeamBowConfig config = config();

            float base = (float) ((AbstractArrowAccessor) this).manybows$getBaseDamage();
            target.hurt(this.damageSources().arrow(this, this.getOwner()), base);

            doLinkDamage(target, base, config);

            this.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS,
                    config.entity_hit_sound_volume,
                    config.entity_hit_sound_pitch
            );
        }

        if (config().discard_on_entity_hit) {
            this.discard();
        }
    }

    private void doLinkDamage(LivingEntity primary, float baseDamage, BeaconBeamBowConfig config) {
        LivingEntity owner = (this.getOwner() instanceof LivingEntity le) ? le : null;

        var box = primary.getBoundingBox().inflate(config.link_radius);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                le -> le != primary && le != owner && le.isAlive()
        );

        int links = 0;
        float linkDamage = Math.max(config.minimum_link_damage, baseDamage * config.link_damage_multiplier);

        for (LivingEntity other : nearby) {
            other.hurt(this.damageSources().magic(), linkDamage);
            if (++links >= config.max_links) {
                break;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (this.level().isClientSide()) {
            return;
        }

        BeaconBeamBowConfig config = config();

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS,
                config.block_hit_sound_volume,
                config.block_hit_sound_pitch
        );

        if (config.discard_on_block_hit) {
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}