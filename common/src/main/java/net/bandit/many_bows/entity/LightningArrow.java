package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.bows.LightningBowConfig;
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
    private int lifetime = 0;

    public LightningArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        applyConfigValues();
    }

    public LightningArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.LIGHTNING_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfigValues();
    }

    private LightningBowConfig config() {
        return LightningBowConfig.get();
    }

    private void applyConfigValues() {
        LightningBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && !lightningStruck && config().spawn_lightning_on_entity_hit) {
            summonLightning(result.getLocation().x, result.getLocation().y, result.getLocation().z);
            lightningStruck = true;
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        LightningBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (!level().isClientSide() && this.isInGround() && !lightningStruck && config.spawn_lightning_on_block_hit) {
            summonLightning(this.getX(), this.getY(), this.getZ());
            lightningStruck = true;
            this.discard();
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            createParticles(config.trail_particle_count);
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

    private void createParticles(int count) {
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.5D;
            double oy = this.random.nextDouble() * 0.5D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.5D;

            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + ox,
                    this.getY() + oy,
                    this.getZ() + oz,
                    0.0D,
                    0.1D,
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
}