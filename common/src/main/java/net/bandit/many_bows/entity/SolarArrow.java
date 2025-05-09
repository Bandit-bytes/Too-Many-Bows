package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SolarArrow extends AbstractArrow {

    private boolean tornadoActive = false;
    private int tornadoTick = 0;
    private Vec3 tornadoOrigin = null;

    public SolarArrow(EntityType<? extends SolarArrow> entityType, Level level) {
        super(entityType, level);
    }

    public SolarArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.SOLAR_ARROW.get(), shooter, level, bowStack, arrowStack);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        if (tornadoActive && tornadoOrigin != null) {
            this.setPos(tornadoOrigin.x, tornadoOrigin.y, tornadoOrigin.z);
            spawnRagingTornado((ServerLevel) level(), tornadoOrigin, tornadoTick);
            level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.5)).forEach(entity -> {
                if (entity != this.getOwner()) {
                    entity.setRemainingFireTicks(40);
                    entity.hurt(level().damageSources().magic(), 4.0F);
                }
            });
            tornadoTick++;
            if (tornadoTick > 100) {
                this.discard();
            }
        } else if (!tornadoActive && this.inGround) {
            startTornado();
        } else if (!tornadoActive) {
            spawnAirSpiral((ServerLevel) level(), this.position(), tornadoTick);
            tornadoTick++;
        }
        if (tornadoTick % 20 == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    this.getSoundSource(),
                    1.0F,
                    0.8F + this.random.nextFloat() * 0.4F
            );
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && !tornadoActive) {
            this.inGround = true;
            startTornado();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && !tornadoActive) {
            this.inGround = true;
            startTornado();
        }
    }

    private void startTornado() {
        this.tornadoActive = true;
        this.tornadoOrigin = this.position();
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setInvisible(true);
        ((ServerLevel) this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 15, 0.3, 0.1, 0.3, 0.05);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.2F);
    }

    private void spawnRagingTornado(ServerLevel level, Vec3 center, int age) {
        int height = Math.min(age, 40);
        int spiralCount = 6;
        float maxRadius = 3.5f;

        for (int y = 0; y < height; y++) {
            float progress = (float) y / 40f;
            float radius = maxRadius * (0.2f + 0.8f * progress);
            double baseY = center.y + y * 0.2;

            for (int i = 0; i < spiralCount; i++) {
                double angle = age * 0.25 + i * (2 * Math.PI / spiralCount) + y * 0.3;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                level.sendParticles(ParticleTypes.FLAME, x, baseY, z, 2, 0, 0, 0, 0);
                if (y % 5 == 0 && i % 2 == 0) {
                    level.sendParticles(ParticleTypes.LAVA, x, baseY, z, 1, 0, 0, 0, 0);
                }
                if (y % 7 == 0 && i % 3 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, x, baseY, z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void spawnAirSpiral(ServerLevel level, Vec3 center, int age) {
        float radius = 1.2f;
        int points = 20;

        for (int i = 0; i < points; i++) {
            double angle = age * 0.3 + i * (2 * Math.PI / points);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.1;

            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}