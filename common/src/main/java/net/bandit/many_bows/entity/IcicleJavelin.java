package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.IcicleJavelinBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class IcicleJavelin extends AbstractArrow {

    private boolean hasFrozen = false;
    private int lifetime = 0;

    public IcicleJavelin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public IcicleJavelin(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ICICLE_JAVELIN.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private IcicleJavelinBowConfig config() {
        return IcicleJavelinBowConfig.get();
    }

    private void applyConfigValues() {
        IcicleJavelinBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        if (this.level().isClientSide()) {
            return;
        }

        IcicleJavelinBowConfig config = config();

        float damage = (float) config.base_damage;

        if (config.use_ranged_damage_attribute_scaling && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    damage = (float) attrInstance.getValue() * config.ranged_damage_multiplier;
                }
            }
        }

        damage *= config.final_damage_multiplier;
        if (damage > 0.0F) {
            target.hurt(this.damageSources().magic(), damage);
        }

        if (config.apply_direct_hit_slowness) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    config.direct_hit_slowness_duration_ticks,
                    config.direct_hit_slowness_amplifier
            ));
        }

        if (config.freeze_block_on_entity_hit) {
            freezeAreaAround(this.getX(), this.getY(), this.getZ());
        }

        if (config.impact_particles_enabled) {
            createIceExplosion();
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.SNOW_STEP,
                SoundSource.PLAYERS,
                config.impact_sound_volume,
                config.impact_sound_pitch
        );

        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        IcicleJavelinBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (!level().isClientSide() && this.isInGround() && !hasFrozen && config.freeze_block_when_landed) {
            freezeAreaAround(this.getX(), this.getY(), this.getZ());
            hasFrozen = true;
            this.discard();
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            createTrailParticles(config.trail_particle_count);
        }
    }

    private void freezeAreaAround(double x, double y, double z) {
        BlockPos impactPos = new BlockPos((int) x, (int) y, (int) z);

        if (level().getBlockState(impactPos).isAir() || level().getBlockState(impactPos).canBeReplaced()) {
            level().setBlockAndUpdate(impactPos, Blocks.PACKED_ICE.defaultBlockState());
        } else {
            BlockPos adjacentPos = findAdjacentBlock(impactPos);
            if (adjacentPos != null) {
                level().setBlockAndUpdate(adjacentPos, Blocks.PACKED_ICE.defaultBlockState());
            }
        }
    }

    private BlockPos findAdjacentBlock(BlockPos impactPos) {
        BlockPos[] adjacentPositions = {
                impactPos.above(),
                impactPos.below(),
                impactPos.north(),
                impactPos.south(),
                impactPos.east(),
                impactPos.west()
        };

        for (BlockPos pos : adjacentPositions) {
            if (level().getBlockState(pos).isAir() || level().getBlockState(pos).canBeReplaced()) {
                return pos;
            }
        }

        return null;
    }

    private void createIceExplosion() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    config().impact_particle_count,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.01D
            );
        }
    }

    private void createTrailParticles(int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * 0.2D;
            double offsetY = (this.random.nextDouble() - 0.5D) * 0.2D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.2D;

            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}