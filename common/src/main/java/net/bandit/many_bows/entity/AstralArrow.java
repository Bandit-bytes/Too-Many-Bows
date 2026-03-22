package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AstralBoundBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AstralArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "astral_bound";

    private int ricochetCount = 3;

    public AstralArrow(EntityType<? extends AstralArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ASTRAL_ARROW.get(), shooter, level, bowStack, arrowStack);
        applyConfiguredValues();
    }

    private static AstralBoundBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AstralBoundBowConfig.class, AstralBoundBowConfig::new);
    }

    private void applyConfiguredValues() {
        AstralBoundBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.ricochetCount = Math.max(0, config.starting_ricochet_count);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            target.hurt(damageSources().arrow(this, this.getOwner()), (float) this.getBaseDamage());

            if (config().discard_after_entity_hit) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide()) {
            AstralBoundBowConfig config = config();
            int maxRicochets = Math.max(0, config.max_ricochets);

            if (ricochetCount < maxRicochets) {
                ricochetCount++;

                Vec3 velocity = this.getDeltaMovement();
                Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
                Vec3 reflected = velocity.subtract(normal.scale(2 * velocity.dot(normal)));

                this.setDeltaMovement(reflected.scale(config.ricochet_velocity_multiplier));

                double offset = config.ricochet_position_offset;
                if (offset > 0.0D && reflected.lengthSqr() > 0.0D) {
                    Vec3 positionOffset = reflected.normalize().scale(offset);
                    this.setPos(
                            this.getX() + positionOffset.x,
                            this.getY() + positionOffset.y,
                            this.getZ() + positionOffset.z
                    );
                }

                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.METAL_HIT,
                        SoundSource.PLAYERS,
                        config.ricochet_sound_volume,
                        config.ricochet_sound_pitch
                );
            } else {
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        AstralBoundBowConfig config = config();

        if (config.discard_when_too_slow) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.lengthSqr() < config.minimum_speed_sqr_before_discard) {
                this.discard();
            }
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