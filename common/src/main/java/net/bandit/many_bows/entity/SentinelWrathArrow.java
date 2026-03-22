package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.SentinelWrathBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class SentinelWrathArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private int lifetime = 0;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public SentinelWrathArrow(EntityType<? extends SentinelWrathArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public SentinelWrathArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SENTINEL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private SentinelWrathBowConfig config() {
        return SentinelWrathBowConfig.get();
    }

    private void applyConfigValues() {
        SentinelWrathBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > config().max_lifetime_ticks) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide() || !(result.getEntity() instanceof LivingEntity target)) {
            return;
        }

        SentinelWrathBowConfig config = config();

        float damage = (float) config.base_damage * this.powerMultiplier * config.final_damage_multiplier;

        if (isRaidMob(target)) {
            damage = getRaidMobDamage(config);
        }

        DamageSource damageSource = this.getOwner() instanceof LivingEntity
                ? this.level().damageSources().arrow(this, this.getOwner())
                : this.level().damageSources().arrow(this, null);

        if (damage > 0.0F) {
            target.hurt(damageSource, damage);
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.RAVAGER_ROAR,
                SoundSource.PLAYERS,
                config.impact_sound_volume,
                config.impact_sound_pitch
        );

        if (config.impact_particles_enabled && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    config.impact_particle_count,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.0D
            );
        }

        if (config.discard_on_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide() && config().discard_on_block_hit) {
            this.discard();
        }
    }

    private float getRaidMobDamage(SentinelWrathBowConfig config) {
        float damage = config.raid_mob_fallback_damage;

        if (config.use_ranged_damage_attribute_scaling_vs_raid_mobs && this.getOwner() instanceof LivingEntity shooter) {
            var registry = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    damage = (float) attrInstance.getValue() * config.raid_mob_ranged_damage_multiplier;
                }
            }
        }

        damage *= this.powerMultiplier * config.final_damage_multiplier;
        return Math.max(0.0F, damage);
    }

    private boolean isRaidMob(LivingEntity entity) {
        return entity instanceof Pillager
                || entity instanceof Vindicator
                || entity instanceof Evoker
                || entity instanceof Ravager
                || entity instanceof Illusioner
                || entity instanceof Witch;
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}