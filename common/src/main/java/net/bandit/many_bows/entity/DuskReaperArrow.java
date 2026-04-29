package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.DuskReaperBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DuskReaperArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private int lifetime = 0;

    public DuskReaperArrow(EntityType<? extends DuskReaperArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public DuskReaperArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.DUSK_REAPER_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private DuskReaperBowConfig config() {
        return DuskReaperBowConfig.get();
    }

    private void applyConfigValues() {
        DuskReaperBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
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
        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        if (target == this.getOwner()) {
            this.discard();
            return;
        }

        Level level = target.level();
        if (level.isClientSide()) {
            return;
        }

        DuskReaperBowConfig config = config();

        float damage = (float) config.base_damage;

        if (config.use_ranged_damage_attribute_scaling && this.getOwner() instanceof LivingEntity shooter) {
            var attrLookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var rangedRefOpt = attrLookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            if (rangedRefOpt.isPresent()) {
                var inst = shooter.getAttribute(rangedRefOpt.get());
                if (inst != null) {
                    damage = (float) inst.getValue() * config.ranged_damage_multiplier;
                }
            }
        }

        damage *= this.powerMultiplier * config.final_damage_multiplier;
        if (damage < 0.0F) {
            damage = 0.0F;
        }

        target.hurt(this.damageSources().magic(), damage);

        if (config.apply_slowness) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    config.slowness_duration_ticks,
                    config.slowness_amplifier
            ));
        }

        if (config.apply_weakness) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    config.weakness_duration_ticks,
                    config.weakness_amplifier
            ));
        }

        if (config.apply_glowing) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    config.glowing_duration_ticks,
                    config.glowing_amplifier
            ));
        }

        if (config.apply_marked_for_death_tag && config.marked_for_death_tag != null && !config.marked_for_death_tag.isBlank()) {
            target.addTag(config.marked_for_death_tag);
        }

        if (config.impact_soul_particles_enabled && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    target.getX(),
                    target.getY() + 1.0D,
                    target.getZ(),
                    config.impact_soul_particle_count,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.02D
            );
        }

        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                config.impact_sound_volume,
                config.impact_sound_pitch
        );

        if (config.discard_on_hit) {
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

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? ItemStack.EMPTY
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}