package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlameArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private boolean hasHit = false;
    private int hitTimer = 0;
    private final int maxHitDuration = 40;

    public FlameArrow(EntityType<? extends FlameArrow> entityType, Level level) {
        super(entityType, level);
    }
    public FlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
    }
    @Override
    public void tick() {
        super.tick();

        if (hasHit) {
            hitTimer++;
            if (hitTimer >= maxHitDuration) {
                this.discard();
                return;
            }
        }
        if (this.level().isClientSide()) {
            double speedFactor = 0.1D;
            Vec3 motion = this.getDeltaMovement();

            for (int i = 0; i < 5; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(ParticleTypes.FLAME, this.getX() + motion.x * i * speedFactor, this.getY() + motion.y * i * speedFactor, this.getZ() + motion.z * i * speedFactor, xOffset, yOffset, zOffset);
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity hitEntity) {
                hitEntity.setRemainingFireTicks(80);
                hitEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
                createFireExplosion(result.getLocation(), hitEntity);
            }
            this.hasHit = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            createFireExplosion(result.getLocation(), null);
            this.hasHit = true;
        }
    }

    private void createFireExplosion(Vec3 position, @Nullable LivingEntity entityHit) {
        int radius = 5;
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius));
        float fireDamage = 2.0F;
        if (this.getOwner() instanceof LivingEntity shooter) {

            var lookup = this.level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);

            ResourceKey<Attribute> RANGED_DAMAGE_KEY =
                    ResourceKey.create(Registries.ATTRIBUTE, Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

            Holder<Attribute> rangedAttr = lookup.get(RANGED_DAMAGE_KEY).orElse(null);

            if (rangedAttr != null) {
                var attrInstance = shooter.getAttribute(rangedAttr);
                if (attrInstance != null) {
                    fireDamage = (float) attrInstance.getValue() / 3F;
                }
            }
        }

        for (LivingEntity entity : entities) {
            if (entity != this.getOwner() && entity != entityHit) {
                entity.setRemainingFireTicks(80);
                entity.hurt(entity.damageSources().onFire(), fireDamage * this.powerMultiplier);

            }
        }

        for (int i = 0; i < 50; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = random.nextDouble();
            double zOffset = (random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(ParticleTypes.FLAME, position.x + xOffset, position.y + yOffset, position.z + zOffset, 0, 0.1D, 0);
        }

        for (int i = 0; i < 30; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = random.nextDouble() * 0.5D;
            double zOffset = (random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(ParticleTypes.EXPLOSION, position.x + xOffset, position.y + yOffset, position.z + zOffset, 0, 0, 0);
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, position.x + xOffset, position.y + yOffset, position.z + zOffset, 0, 0, 0);
        }

        this.level().playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 1.0F, 1.2F);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return null;
    }

}
