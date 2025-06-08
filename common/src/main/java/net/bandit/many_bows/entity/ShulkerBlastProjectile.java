package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class ShulkerBlastProjectile extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private static final double HOMING_RANGE = 20.0;
    private static final float SPEED = 0.7f;
    private static final float DAMAGE = 6.0f;
    private static final int LEVITATION_DURATION = 40;
    private static final int MAX_LIFETIME = 100;

    private int lifetime = 0;

    public ShulkerBlastProjectile(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public ShulkerBlastProjectile(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SHULKER_BLAST_PROJECTILE.get(), shooter, level, bowStack, arrowStack);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide) {
            LivingEntity target = findNearestTarget();
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dy = (target.getY() + target.getEyeHeight() / 2) - this.getY();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0) {
                    double homingFactor = 0.2;
                    this.setDeltaMovement(
                            this.getDeltaMovement().x * (1.0 - homingFactor) + (dx / distance) * SPEED * homingFactor,
                            this.getDeltaMovement().y * (1.0 - homingFactor) + (dy / distance) * SPEED * homingFactor,
                            this.getDeltaMovement().z * (1.0 - homingFactor) + (dz / distance) * SPEED * homingFactor
                    );
                }
            }
        }
        this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    private LivingEntity findNearestTarget() {
        AABB searchBox = this.getBoundingBox().inflate(HOMING_RANGE);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != this.getOwner()
                        && entity.isAlive()
                        && !(entity instanceof ArmorStand)
                        && !(entity instanceof TamableAnimal tamable && tamable.isTame())
        );
        return entities.stream()
                .min((e1, e2) -> Double.compare(e1.distanceTo(this), e2.distanceTo(this)))
                .orElse(null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            float scaledDamage;

            if (this.getOwner() instanceof LivingEntity shooter) {
                var registry = level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
                var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

                if (rangedAttrHolder != null) {
                    var attrInstance = shooter.getAttribute(rangedAttrHolder);
                    scaledDamage = attrInstance != null ? (float) attrInstance.getValue() : DAMAGE;
                } else {
                    scaledDamage = DAMAGE;
                }
            } else {
                scaledDamage = DAMAGE;
            }

            scaledDamage *= this.powerMultiplier;

            DamageSource damageSource = this.level().damageSources().arrow(this, this.getOwner());
            target.hurt(damageSource, scaledDamage);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION, 1));

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHULKER_BULLET_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);

            for (int i = 0; i < 10; i++) {
                double xOffset = (this.random.nextDouble() - 0.5) * 0.3;
                double yOffset = this.random.nextDouble() * 0.3;
                double zOffset = (this.random.nextDouble() - 0.5) * 0.3;
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                        this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, 0.0, 0.0, 0.0);
            }
        }

        this.discard();
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
