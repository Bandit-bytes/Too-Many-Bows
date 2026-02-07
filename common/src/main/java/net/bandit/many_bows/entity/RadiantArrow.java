package net.bandit.many_bows.entity;

import net.bandit.many_bows.ManyBowsMod;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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

    private static final ResourceKey<DamageType> RADIANT_DAMAGE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath(ManyBowsMod.MOD_ID, "radiant")
            );

    private float powerMultiplier = 1.0F;
    private boolean hasExploded = false;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    public RadiantArrow(EntityType<? extends RadiantArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RadiantArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.RADIANT_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide()) return;
        if (hasExploded) return;

        if (result.getType() == HitResult.Type.BLOCK || result.getType() == HitResult.Type.ENTITY) {
            hasExploded = true;
            createRadiantExplosion();
            discard();
        }
    }

    private void createRadiantExplosion() {
        final Level level = level();

        level.explode(null, getX(), getY(), getZ(), 2.0F, Level.ExplosionInteraction.NONE);

        LivingEntity shooter = (getOwner() instanceof LivingEntity le) ? le : null;
        float scaledDamage = getScaledRadiantDamage(level, shooter);

        AABB area = new AABB(blockPosition()).inflate(5.0D);

        DamageSource src = createRadiantDamage(level, this, shooter);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == shooter) continue;
            if (shooter != null && target.isAlliedTo(shooter)) continue;

            float dmg = scaledDamage * powerMultiplier;
            if (target.isInvertedHealAndHarm()) dmg *= 2.0F;

            target.hurt(src, dmg);
        }
    }

    private static DamageSource createRadiantDamage(Level level,
                                                    RadiantArrow direct,
                                                    @Nullable LivingEntity attacker) {
        if (attacker != null) {
            return level.damageSources().arrow(direct, attacker);
        }
        return level.damageSources().arrow(direct, direct);
    }

    private static float getScaledRadiantDamage(Level level, @Nullable LivingEntity shooter) {
        if (shooter == null) return 3.0F;

        var lookup = level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        var holderOpt = lookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));
        if (holderOpt.isEmpty()) return 3.0F;

        var inst = shooter.getAttribute(holderOpt.get());
        if (inst == null) return 3.0F;

        return (float) inst.getValue() / 2.0F;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
