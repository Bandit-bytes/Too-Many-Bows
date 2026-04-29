package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.IroncladBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IronCladArrow extends AbstractArrow {

    private boolean hasLanded = false;
    private int vacuumTicksRemaining = 0;
    private int lifetime = 0;

    public IronCladArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public IronCladArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.IRONCLAD_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private IroncladBowConfig config() {
        return IroncladBowConfig.get();
    }

    private void applyConfigValues() {
        IroncladBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
        this.vacuumTicksRemaining = config.vacuum_duration_ticks;
    }

    private void startVacuum() {
        if (hasLanded) {
            return;
        }

        this.hasLanded = true;
        this.vacuumTicksRemaining = config().vacuum_duration_ticks;
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        IroncladBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (hasLanded && vacuumTicksRemaining > 0) {
            double radius = config.vacuum_radius;
            AABB pullArea = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );

            List<Entity> nearbyEntities = this.level().getEntities(
                    this,
                    pullArea,
                    e -> e instanceof LivingEntity && (config.affect_owner || e != this.getOwner())
            );

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    Vec3 pullVector = this.position()
                            .subtract(livingEntity.position())
                            .normalize()
                            .scale(config.pull_strength);

                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(pullVector));
                }
            }

            vacuumTicksRemaining--;
            if (vacuumTicksRemaining <= 0 && config.discard_when_vacuum_ends) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && config().activate_vacuum_on_entity_hit) {
            startVacuum();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide() && config().activate_vacuum_on_block_hit) {
            startVacuum();
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