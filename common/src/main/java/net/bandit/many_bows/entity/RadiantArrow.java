package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.RadiantBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class RadiantArrow extends AbstractArrow {

    private float powerMultiplier = 1.0F;
    private boolean hasExploded = false;
    private int lifetime = 0;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public RadiantArrow(EntityType<? extends RadiantArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public RadiantArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.RADIANT_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private RadiantBowConfig config() {
        return RadiantBowConfig.get();
    }

    private void applyConfigValues() {
        RadiantBowConfig config = config();
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
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (level().isClientSide() || hasExploded) {
            return;
        }

        if (result.getType() == HitResult.Type.BLOCK || result.getType() == HitResult.Type.ENTITY) {
            hasExploded = true;
            createRadiantExplosion();

            if (config().discard_on_impact) {
                discard();
            }
        }
    }

    private void createRadiantExplosion() {
        Level level = level();
        RadiantBowConfig config = config();

        level.explode(null, getX(), getY(), getZ(), config.explosion_visual_power, Level.ExplosionInteraction.NONE);

        LivingEntity shooter = (getOwner() instanceof LivingEntity le) ? le : null;
        float scaledDamage = getScaledRadiantDamage(level, shooter, config) * powerMultiplier * config.final_damage_multiplier;

        AABB area = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(config.radiant_damage_radius);
        DamageSource src = createRadiantDamage(level, this, shooter);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (config.exclude_owner && target == shooter) {
                continue;
            }
            if (config.exclude_allies_of_owner && shooter != null && target.isAlliedTo(shooter)) {
                continue;
            }

            float damage = scaledDamage;
            if (target.isInvertedHealAndHarm()) {
                damage *= config.inverted_heal_and_harm_damage_multiplier;
            }

            if (damage > 0.0F) {
                target.hurt(src, damage);
            }
        }
    }

    private static DamageSource createRadiantDamage(Level level, RadiantArrow direct, @Nullable LivingEntity attacker) {
        if (attacker != null) {
            return level.damageSources().arrow(direct, attacker);
        }
        return level.damageSources().arrow(direct, direct);
    }

    private static float getScaledRadiantDamage(Level level, @Nullable LivingEntity shooter, RadiantBowConfig config) {
        if (shooter == null) {
            return config.radiant_damage_fallback;
        }

        if (!config.use_ranged_damage_attribute_scaling || config.ranged_damage_divisor <= 0.0F) {
            return config.radiant_damage_fallback;
        }

        var lookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var holderOpt = lookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

        if (holderOpt.isEmpty()) {
            return config.radiant_damage_fallback;
        }

        var inst = shooter.getAttribute(holderOpt.get());
        if (inst == null) {
            return config.radiant_damage_fallback;
        }

        return (float) inst.getValue() / config.ranged_damage_divisor;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
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