package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AstralBoundBowConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AstralArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "astral_bound";

    private int ricochetCount = 0;
    private int lifetime = 0;

    public AstralArrow(EntityType<? extends AstralArrow> entityType, Level level) {
        super(entityType, level);
        this.setPickupItemStack(safeArrowStack(ItemStack.EMPTY));
        applyConfigValues();
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.ASTRAL_ARROW.get(), shooter, level, safeArrowStack(arrowStack), safeBowStack(bowStack));
        applyConfigValues();
    }

    private static AstralBoundBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AstralBoundBowConfig.class, AstralBoundBowConfig::new);
    }

    private void applyConfigValues() {
        AstralBoundBowConfig config = config();
        this.setBaseDamage(config.base_damage);
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && config().discard_on_entity_hit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        AstralBoundBowConfig config = config();

        if (!config.ricochet_on_block_hit) {
            this.discard();
            return;
        }

        if (ricochetCount >= config.max_ricochets) {
            this.discard();
            return;
        }

        ricochetCount++;

        Vec3 velocity = this.getDeltaMovement();
        var face = result.getDirection();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());

        Vec3 reflected = velocity.subtract(normal.scale(2.0D * velocity.dot(normal)));
        Vec3 newVelocity = reflected.scale(config.ricochet_velocity_multiplier);

        this.setDeltaMovement(newVelocity);

        Vec3 nudgeDir = newVelocity.lengthSqr() < 1.0E-6D ? normal : newVelocity.normalize();
        Vec3 positionOffset = nudgeDir.scale(0.1D);

        this.setPos(
                this.getX() + positionOffset.x,
                this.getY() + positionOffset.y,
                this.getZ() + positionOffset.z
        );

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

        if (newVelocity.lengthSqr() < config.min_velocity_sqr_before_discard) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        AstralBoundBowConfig config = config();

        lifetime++;
        if (lifetime > config.max_lifetime_ticks) {
            this.discard();
            return;
        }

        if (this.getDeltaMovement().lengthSqr() < config.min_velocity_sqr_before_discard) {
            this.discard();
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