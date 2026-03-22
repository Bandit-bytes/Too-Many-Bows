package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.AurorasGraceBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class AuroraArrowEntity extends AbstractArrow {

    private int lifetime = 0;

    public AuroraArrowEntity(EntityType<? extends AuroraArrowEntity> entityType, Level world) {
        super(entityType, world);
        applyConfigValues();
    }

    public AuroraArrowEntity(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AURORA_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private AurorasGraceBowConfig config() {
        return AurorasGraceBowConfig.get();
    }

    private void applyConfigValues() {
        AurorasGraceBowConfig config = config();
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
        super.onHitEntity(result);

        AurorasGraceBowConfig config = config();

        if (!this.level().isClientSide() && config.spawn_rift_on_entity_hit) {
            spawnRift(BlockPos.containing(result.getLocation()));
        }

        if (config.discard_on_impact) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        AurorasGraceBowConfig config = config();

        if (!this.level().isClientSide() && config.spawn_rift_on_block_hit) {
            spawnRift(result.getBlockPos());
        }

        if (config.discard_on_impact) {
            this.discard();
        }
    }

    private void spawnRift(BlockPos position) {
        if (this.getOwner() instanceof LivingEntity owner) {
            RiftEntity rift = new RiftEntity(this.level(), owner, position);
            this.level().addFreshEntity(rift);
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
}