package net.bandit.many_bows.entity;

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

    private static final double LINK_RADIUS = 6.0D;
    private static final int MAX_LINKS = 4;
    private static final float LINK_DAMAGE_MULT = 0.45F;

    private static final int TRAIL_SAMPLES = 6;
    private static final float TRAIL_SPREAD = 0.01F;
    private static final float SPEED_ALPHA_BOOST = 0.015F;

    private Vec3 lastPos = null;

    public BeaconBeamArrow(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public BeaconBeamArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.BEACON_BEAM_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (lastPos == null) lastPos = this.position();

            Vec3 now = this.position();
            Vec3 delta = now.subtract(lastPos);

            double len = delta.length();
            if (len > 0.0001) {
                Vec3 step = delta.scale(1.0 / TRAIL_SAMPLES);

                int extra = (int) Math.min(6, len / 0.75);
                int samples = TRAIL_SAMPLES + extra;

                spawnBeamTrail(lastPos, now, samples, ParticleTypes.ELECTRIC_SPARK);
            }

            lastPos = now;
            return;
        }
        if (lastPos == null) lastPos = this.position();
        else lastPos = this.position();
    }

    private void spawnBeamTrail(Vec3 from, Vec3 to, int samples, ParticleOptions particle) {
        Level level = this.level();
        Vec3 diff = to.subtract(from);

        for (int i = 0; i <= samples; i++) {
            double t = (double) i / (double) samples;
            double x = from.x + diff.x * t;
            double y = from.y + diff.y * t;
            double z = from.z + diff.z * t;

            double ox = (this.random.nextDouble() - 0.5) * TRAIL_SPREAD;
            double oy = (this.random.nextDouble() - 0.5) * TRAIL_SPREAD;
            double oz = (this.random.nextDouble() - 0.5) * TRAIL_SPREAD;

            level.addParticle(particle, x + ox, y + oy, z + oz, 0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);

        if (this.level().isClientSide()) return;

        if (hit.getEntity() instanceof LivingEntity target) {
            float base = (float) ((AbstractArrowAccessor) this).manybows$getBaseDamage();

            target.hurt(this.damageSources().arrow(this, this.getOwner()), base);
            doLinkDamage(target, base);

            this.level().playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS,
                    1.0F, 1.35F
            );
        }

        this.discard();
    }

    private void doLinkDamage(LivingEntity primary, float baseDamage) {
        LivingEntity owner = (this.getOwner() instanceof LivingEntity le) ? le : null;

        var box = primary.getBoundingBox().inflate(LINK_RADIUS);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                le -> le != primary && le != owner && le.isAlive()
        );

        int links = 0;
        float linkDamage = Math.max(1.0F, baseDamage * LINK_DAMAGE_MULT);

        for (LivingEntity other : nearby) {
            other.hurt(this.damageSources().magic(), linkDamage);
            if (++links >= MAX_LINKS) break;
        }
    }
    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);

        if (this.level().isClientSide()) return;
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS,
                0.6F, 1.2F
        );

        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }
}
