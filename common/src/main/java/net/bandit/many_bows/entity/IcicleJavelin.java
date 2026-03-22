package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.IcicleJavelinBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;

public class IcicleJavelin extends AbstractArrow {

    private static final String CONFIG_NAME = "cyroheart_bow";

    private boolean hasFrozen = false;

    public IcicleJavelin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public IcicleJavelin(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ICICLE_JAVELIN.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static IcicleJavelinBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, IcicleJavelinBowConfig.class, IcicleJavelinBowConfig::new);
    }

    private void applyConfiguredValues() {
        IcicleJavelinBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity target) || level().isClientSide()) {
            return;
        }

        IcicleJavelinBowConfig config = config();

        if (config.bonus_magic_damage_enabled) {
            float scaledDamage = resolveBonusMagicDamage(config);
            if (scaledDamage > 0.0F) {
                target.hurt(this.damageSources().magic(), scaledDamage);
            }
        }

        if (config.apply_slowness_on_hit) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    config.slowness_duration_ticks,
                    config.slowness_amplifier
            ));
        }

        if (config.freeze_on_entity_hit) {
            freezeAreaAround(this.getX(), this.getY(), this.getZ(), config);
        }

        if (config.impact_particles_enabled) {
            createIceExplosion(config);
        }

        if (config.impact_sound_enabled) {
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
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        IcicleJavelinBowConfig config = config();

        if (!level().isClientSide() && this.inGround && !hasFrozen && config.freeze_on_in_ground) {
            freezeAreaAround(this.getX(), this.getY(), this.getZ(), config);
            hasFrozen = true;

            if (config.discard_after_in_ground_freeze) {
                this.discard();
                return;
            }
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            createTrailParticles(config);
        }
    }

    private float resolveBonusMagicDamage(IcicleJavelinBowConfig config) {
        float scaledDamage = (float) config.bonus_magic_damage_base;

        if (config.use_ranged_damage_attribute_for_bonus_magic_damage && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(
                    ResourceLocation.fromNamespaceAndPath(
                            config.ranged_damage_attribute_namespace,
                            config.ranged_damage_attribute_path
                    )
            ).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    scaledDamage = (float) (attrInstance.getValue() * config.ranged_damage_attribute_multiplier);
                }
            }
        }

        return scaledDamage;
    }

    private void freezeAreaAround(double x, double y, double z, IcicleJavelinBowConfig config) {
        BlockPos impactPos = new BlockPos((int) x, (int) y, (int) z);
        Block freezeBlock = resolveFreezeBlock(config.freeze_block);

        if (level().getBlockState(impactPos).isAir() || level().getBlockState(impactPos).canBeReplaced()) {
            level().setBlockAndUpdate(impactPos, freezeBlock.defaultBlockState());
        } else if (config.allow_freeze_adjacent_if_impact_pos_blocked) {
            BlockPos adjacentPos = findAdjacentBlock(impactPos);
            if (adjacentPos != null) {
                level().setBlockAndUpdate(adjacentPos, freezeBlock.defaultBlockState());
            }
        }
    }

    private Block resolveFreezeBlock(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            return Blocks.PACKED_ICE;
        }

        Block block = BuiltInRegistries.BLOCK.get(rl);
        return block == Blocks.AIR ? Blocks.PACKED_ICE : block;
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

    private void createIceExplosion(IcicleJavelinBowConfig config) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    Math.max(0, config.impact_particle_count),
                    config.impact_particle_offset_x,
                    config.impact_particle_offset_y,
                    config.impact_particle_offset_z,
                    config.impact_particle_speed
            );
        }
    }

    private void createTrailParticles(IcicleJavelinBowConfig config) {
        for (int i = 0; i < Math.max(0, config.trail_particle_count); i++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * config.trail_particle_offset_scale;
            double offsetY = (this.random.nextDouble() - 0.5D) * config.trail_particle_offset_scale;
            double offsetZ = (this.random.nextDouble() - 0.5D) * config.trail_particle_offset_scale;

            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D
            );
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