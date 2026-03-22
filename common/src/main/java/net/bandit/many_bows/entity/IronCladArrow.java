package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.IroncladBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IronCladArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "ironclad";

    private boolean hasLanded = false;
    private int vacuumDuration = 80;

    public IronCladArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public IronCladArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.IRONCLAD_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static IroncladBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, IroncladBowConfig.class, IroncladBowConfig::new);
    }

    private void applyConfiguredValues() {
        IroncladBowConfig config = config();
        this.vacuumDuration = Math.max(0, config.vacuum_duration_ticks);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    public void tick() {
        super.tick();

        IroncladBowConfig config = config();

        if (hasLanded && vacuumDuration > 0) {
            double radius = Math.max(0.0D, config.vacuum_radius);

            AABB pullArea = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );

            List<Entity> nearbyEntities = this.level().getEntities(
                    this,
                    pullArea,
                    entity -> {
                        if (config.vacuum_affects_only_living_entities && !(entity instanceof LivingEntity)) {
                            return false;
                        }
                        if (!config.vacuum_affects_owner && entity == this.getOwner()) {
                            return false;
                        }
                        return true;
                    }
            );

            for (Entity entity : nearbyEntities) {
                Vec3 arrowPos = this.position();
                Vec3 entityPos = entity.position();
                Vec3 diff = arrowPos.subtract(entityPos);

                if (diff.lengthSqr() > 1.0E-6D) {
                    Vec3 pullVector = diff.normalize().scale(config.vacuum_pull_strength);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(pullVector));
                }
            }

            vacuumDuration--;
            if (vacuumDuration <= 0) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide() && config().start_vacuum_on_hit) {
            this.hasLanded = true;
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