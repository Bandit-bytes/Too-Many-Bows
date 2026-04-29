package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AncientSageBowConfig;
import net.bandit.many_bows.mixin.AbstractArrowAccessor;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class AncientSageArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private float armorPenetration = 0.33F;
    private int particleTicksRemaining = 60;
    private int lifetime = 0;
    private static final String CONFIG_NAME = "ancient_sage_bow";

    public AncientSageArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public AncientSageArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ANCIENT_SAGE_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private static AncientSageBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AncientSageBowConfig.class, AncientSageBowConfig::new);
    }

    private void applyConfigValues() {
        AncientSageBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.armorPenetration = config.default_armor_penetration_factor;
        this.particleTicksRemaining = config.trail_particle_lifespan_ticks;
    }

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public void setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    @Override
    public void tick() {
        super.tick();

        AncientSageBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (config.trail_particles_enabled && particleTicksRemaining > 0) {
            createTrailParticles(config.trail_particles_per_tick);
            particleTicksRemaining--;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        AncientSageBowConfig config = config();

        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }

        if (config.hit_particles_enabled) {
            createHitParticles(config.hit_particle_count);
        }

        if (this.level().isClientSide()) {
            return;
        }

        float baseDamage = (float) ((AbstractArrowAccessor) this).manybows$getBaseDamage();
        float calculatedPenetration = this.armorPenetration;

        if (config.use_ranged_damage_attribute_for_penetration && this.getOwner() instanceof Player player) {
            var lookup = this.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            var holderOpt = lookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            if (holderOpt.isPresent() && config.ranged_damage_to_penetration_divisor > 0.0F) {
                AttributeInstance inst = player.getAttribute(holderOpt.get());
                if (inst != null) {
                    calculatedPenetration = (float) (inst.getValue() / config.ranged_damage_to_penetration_divisor);
                }
            }
        }

        float minPen = Math.min(config.min_armor_penetration_factor, config.max_armor_penetration_factor);
        float maxPen = Math.max(config.min_armor_penetration_factor, config.max_armor_penetration_factor);
        calculatedPenetration = Math.min(Math.max(calculatedPenetration, minPen), maxPen);

        float damage = baseDamage * calculatedPenetration * this.powerMultiplier * config.final_damage_multiplier;
        if (damage < 0.0F) {
            damage = 0.0F;
        }

        target.hurt(this.level().damageSources().arrow(this, this.getOwner()), damage);
        this.discard();
    }

    private void createHitParticles(int count) {
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.5D;
            double oy = this.random.nextDouble() * 0.5D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.5D;

            level().addParticle(
                    ParticleTypes.ENCHANTED_HIT,
                    this.getX() + ox,
                    this.getY() + oy,
                    this.getZ() + oz,
                    0.0D,
                    0.1D,
                    0.0D
            );
        }
    }

    private void createTrailParticles(int count) {
        for (int i = 0; i < count; i++) {
            level().addParticle(
                    ParticleTypes.GLOW,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0.0D,
                    0.05D,
                    0.0D
            );
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