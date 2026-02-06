package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class LightningArrow extends AbstractArrow {
    private boolean lightningStruck = false;

    public LightningArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public LightningArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.LIGHTNING_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && !lightningStruck) {
            summonLightning(this.getX(), this.getY(), this.getZ());
            lightningStruck = true;
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.isInGround() && !lightningStruck) {
            summonLightning(this.getX(), this.getY(), this.getZ());
            lightningStruck = true;
            this.discard();
        }

        if (level().isClientSide()) {
            createParticles();
        }
    }

    private void summonLightning(double x, double y, double z) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.setPos(x, y, z);

        if (this.getOwner() instanceof ServerPlayer player) {
            bolt.setCause(player);
        }

        serverLevel.addFreshEntity(bolt);
    }

    private void createParticles() {
        for (int i = 0; i < 10; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 0.5;
            double oy = (this.random.nextDouble() * 0.5);
            double oz = (this.random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    0.0, 0.1, 0.0);
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return null;
    }
}
