package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AethersCallArrow extends AbstractArrow {

    private static final int MAX_LIFETIME = 80;
    private int lifetime = 0;

    public AethersCallArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public AethersCallArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.AETHERS_CALL_ARROW.get(), shooter, level, bowStack, arrowStack);
        this.setBaseDamage(6.0D);
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (level().isClientSide()) {
            level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY() + 0.1D,
                    this.getZ(),
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        triggerAetherBurst();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        triggerAetherBurst();
    }

    private void triggerAetherBurst() {
        if (this.level().isClientSide()) return;

        Level level = this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        double radius = 4.0D;

        // Sound
        level.playSound(
                null,
                x, y, z,
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.PLAYERS,
                1.0F,
                1.5F
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius)
        );

        LivingEntity owner = (this.getOwner() instanceof LivingEntity living) ? living : null;

        for (LivingEntity target : entities) {
            if (owner != null && target == owner) {
                target.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        100,
                        0,
                        false,
                        true
                ));
                continue;
            }

            target.addEffect(new MobEffectInstance(
                    MobEffects.LEVITATION,
                    40,
                    0,
                    false,
                    true
            ));
        }

        for (int i = 0; i < 20; i++) {
            double dx = (random.nextDouble() - 0.5D) * 1.5D;
            double dy = random.nextDouble() * 1.5D;
            double dz = (random.nextDouble() - 0.5D) * 1.5D;
            level.addParticle(
                    ParticleTypes.END_ROD,
                    x + dx,
                    y + dy,
                    z + dz,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }

        this.discard();
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
