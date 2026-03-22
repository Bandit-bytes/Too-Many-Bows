package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AurorasGraceBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AuroraArrowEntity extends AbstractArrow {

    private static final String CONFIG_NAME = "auroras_grace";

    public AuroraArrowEntity(EntityType<? extends AuroraArrowEntity> entityType, Level world) {
        super(entityType, world);
        applyConfiguredValues();
    }

    public AuroraArrowEntity(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AURORA_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static AurorasGraceBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AurorasGraceBowConfig.class, AurorasGraceBowConfig::new);
    }

    private void applyConfiguredValues() {
        AurorasGraceBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        AurorasGraceBowConfig config = config();

        if (!this.level().isClientSide && config.spawn_rift_on_entity_hit) {
            spawnRift(BlockPos.containing(result.getLocation()));
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        AurorasGraceBowConfig config = config();

        if (!this.level().isClientSide && config.spawn_rift_on_block_hit) {
            spawnRift(result.getBlockPos());
        }

        if (config.discard_after_block_hit) {
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
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }
}