package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.LightningBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class LightningArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "arc_heavens";

    private boolean lightningStruck = false;

    public LightningArrow(EntityType<? extends LightningArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public LightningArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.LIGHTNING_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static LightningBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, LightningBowConfig.class, LightningBowConfig::new);
    }

    private void applyConfiguredValues() {
        LightningBowConfig config = config();
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        if (config.direct_hit_damage_override >= 0.0D) {
            this.setBaseDamage(config.direct_hit_damage_override);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        LightningBowConfig config = config();

        if (!level().isClientSide() && !lightningStruck && config.summon_lightning_on_entity_hit) {
            summonLightning(result.getLocation().x(), result.getLocation().y(), result.getLocation().z(), config);
            lightningStruck = true;

            if (config.discard_after_lightning) {
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        LightningBowConfig config = config();

        if (!level().isClientSide() && this.inGround && !lightningStruck && config.summon_lightning_on_in_ground) {
            summonLightning(this.getX(), this.getY(), this.getZ(), config);
            lightningStruck = true;

            if (config.discard_after_lightning) {
                this.discard();
                return;
            }
        }

        if (level().isClientSide() && config.trail_particles_enabled) {
            createParticles(config);
        }
    }

    private void summonLightning(double x, double y, double z, LightningBowConfig config) {
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level());
        if (lightningBolt == null) {
            return;
        }

        lightningBolt.moveTo(x, y, z);

        if (config.set_lightning_cause_from_owner && this.getOwner() instanceof ServerPlayer player) {
            lightningBolt.setCause(player);
        }

        level().addFreshEntity(lightningBolt);
    }

    private void createParticles(LightningBowConfig config) {
        for (int i = 0; i < Math.max(0, config.trail_particle_count); i++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * config.trail_particle_offset_xz;
            double offsetY = this.random.nextDouble() * config.trail_particle_offset_y;
            double offsetZ = (this.random.nextDouble() - 0.5D) * config.trail_particle_offset_xz;

            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D,
                    config.trail_particle_velocity_y,
                    0.0D
            );
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