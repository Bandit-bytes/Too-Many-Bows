package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;

public class IcicleJavelin extends AbstractArrow {
    private boolean hasFrozen = false;

    public IcicleJavelin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public IcicleJavelin(Level level, LivingEntity shooter) {
        super(EntityRegistry.ICICLE_JAVELIN.get(), shooter, level);
        this.setBaseDamage(5.0);
        this.setPierceLevel((byte) 3);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            DamageSource damageSource = level().damageSources().playerAttack((Player) this.getOwner());
            target.hurt(damageSource, 8.0f);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4)); // Apply slowness
        }

        freezeAreaAround(this.getX(), this.getY(), this.getZ());
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOW_STEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        createIceExplosion();
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.inGround && !hasFrozen) {
            freezeAreaAround(this.getX(), this.getY(), this.getZ());
            hasFrozen = true;
            this.discard();
        }

        if (level().isClientSide()) {
            createTrailParticles();
        }
    }

    private void freezeAreaAround(double x, double y, double z) {
        BlockPos impactPos = new BlockPos((int) x, (int) y, (int) z);

        if (level().getBlockState(impactPos).is(Blocks.WATER)) {
            level().setBlockAndUpdate(impactPos, Blocks.ICE.defaultBlockState());
            return;
        }

        BlockPos blockBelow = impactPos.below();
        if (level().getBlockState(impactPos).isAir() && level().getBlockState(blockBelow).isSolid()) {
            level().setBlockAndUpdate(impactPos, Blocks.PACKED_ICE.defaultBlockState());
        }
    }

    private void createIceExplosion() {
        for (int i = 0; i < 20; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
            double offsetY = this.random.nextDouble() * 0.5;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
        }
    }

    private void createTrailParticles() {
        for (int i = 0; i < 5; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // Make it non-retrievable
    }
}
