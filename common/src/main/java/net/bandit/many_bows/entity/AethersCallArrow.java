package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AethersCallBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AethersCallArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "aethers_call";
    private int lifetime = 0;

    public AethersCallArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public AethersCallArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AETHERS_CALL_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private static AethersCallBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AethersCallBowConfig.class, AethersCallBowConfig::new);
    }

    private void applyConfigValues() {
        AethersCallBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();

        AethersCallBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            for (int i = 0; i < config.trail_particles_per_tick; i++) {
                level().addParticle(
                        ParticleTypes.END_ROD,
                        this.getX(),
                        this.getY() + 0.1D,
                        this.getZ(),
                        0.0D,
                        0.02D,
                        0.0D
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
        AethersCallBowConfig config = config();
        Level level = this.level();

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        if (config.burst_particles_enabled) {
            for (int i = 0; i < config.burst_particle_count; i++) {
                double dx = (random.nextDouble() - 0.5D) * 1.5D;
                double dy = random.nextDouble() * 1.5D;
                double dz = (random.nextDouble() - 0.5D) * 1.5D;

                level.addParticle(
                        ParticleTypes.END_ROD,
                        x + dx,
                        y + dy,
                        z + dz,
                        0.0D,
                        0.02D,
                        0.0D
                );
            }
        }

        if (level.isClientSide()) {
            return;
        }

        level.playSound(
                null,
                x, y, z,
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.PLAYERS,
                config.burst_sound_volume,
                config.burst_sound_pitch
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        x - config.burst_radius, y - config.burst_radius, z - config.burst_radius,
                        x + config.burst_radius, y + config.burst_radius, z + config.burst_radius
                )
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

        if (config.discard_on_impact) {
            this.discard();
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    private static ItemStack safeArrowStack(ItemStack arrowStack) {
        return arrowStack == null || arrowStack.isEmpty()
                ? new ItemStack(Items.ARROW)
                : arrowStack.copy();
    }

    private static ItemStack safeBowStack(ItemStack bowStack) {
        return bowStack == null ? ItemStack.EMPTY : bowStack.copy();
    }

}