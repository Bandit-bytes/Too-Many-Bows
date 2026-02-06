package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EffectRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;

public class CursedFlameArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }
    private int particleTimer = 0;
    private static final int MAX_PARTICLE_DURATION = 100;

    public CursedFlameArrow(EntityType<? extends CursedFlameArrow> entityType, Level level) {
        super(entityType, level);
    }

    public CursedFlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.CURSED_FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isInGround()) {
            particleTimer++;
        }
        if (particleTimer < MAX_PARTICLE_DURATION && this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < 5; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + motion.x * i * 0.1,
                        this.getY() + motion.y * i * 0.1,
                        this.getZ() + motion.z * i * 0.1,
                        xOffset, yOffset, zOffset);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity hit) {
            hit.setRemainingFireTicks(200);

            float fireDamage = 4.0F;

            if (getOwner() instanceof LivingEntity shooter) {

                var attrLookup = level().registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
                var rangedDamageRefOpt = attrLookup.get(Identifier.fromNamespaceAndPath("ranged_weapon", "damage"));

                if (rangedDamageRefOpt.isPresent()) {
                    var inst = shooter.getAttribute(rangedDamageRefOpt.get());
                    if (inst != null) {
                        float damage = (float) inst.getValue();
                        this.setBaseDamage(damage / 2.5F);
                    }
                }

            }

            hit.hurt(hit.damageSources().onFire(), fireDamage * this.powerMultiplier);

            hit.removeEffect(MobEffects.REGENERATION);
            hit.removeEffect(MobEffects.INSTANT_HEALTH);

            var mobEffectRegistry = level().registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
            var cursedFlameHolder = mobEffectRegistry
                    .getOrThrow(EffectRegistry.CURSED_FLAME.getKey());

            hit.addEffect(new MobEffectInstance(
                    cursedFlameHolder,
                    20 * 8,
                    0,
                    false,
                    true
            ));

            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL, hit.getX(), hit.getY(0.5), hit.getZ(),
                        30, 1.0, 0.5, 1.0, 0.1);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            Vec3 position = result.getLocation();

            createCursedSoulFireParticles(position);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    position.x, position.y, position.z,
                    30, 0.5, 0.5, 0.5, 0.01);

            serverLevel.playSound(null,
                    position.x, position.y, position.z,
                    SoundEvents.SOUL_ESCAPE, this.getSoundSource(),
                    1.0F, 1.0F);

            particleTimer = MAX_PARTICLE_DURATION;
        }
    }

    private void createCursedSoulFireParticles(Vec3 position) {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL,
                    position.x, position.y, position.z,
                    30,
                    1.0, 0.5, 1.0,
                    0.1);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
