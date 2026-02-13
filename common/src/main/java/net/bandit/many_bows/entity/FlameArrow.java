package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowLootConfig;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlameArrow extends AbstractArrow {
    private float powerMultiplier = 1.0F;
    private static BowLootConfig config;

    public void setPowerMultiplier(float power) {
        this.powerMultiplier = power;
    }

    private boolean hasHit = false;
    private int hitTimer = 0;
    private final int maxHitDuration = 60;

    public FlameArrow(EntityType<? extends FlameArrow> entityType, Level level) {
        super(entityType, level);
        loadConfig();
    }

    public FlameArrow(Level level, LivingEntity shooter, ItemStack bowStack, ItemStack arrowStack) {
        super(EntityRegistry.FLAME_ARROW.get(), shooter, level, bowStack, arrowStack);
        loadConfig();
    }

    private static void loadConfig() {
        if (config == null) {
            config = BowLootConfig.loadConfig();
        }
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

            // lingering ring while stuck (client-side looks best)
            if (this.level().isClientSide() && hitTimer % 3 == 0) {
                spawnInfernoParticles();
            }
        }

        if (this.level().isClientSide() && !hasHit) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        Vec3 motion = this.getDeltaMovement();
        double speedFactor = 0.12D;

        for (int i = 0; i < 6; i++) {
            double xOffset = (this.random.nextDouble() - 0.5D) * 0.35D;
            double yOffset = (this.random.nextDouble() - 0.5D) * 0.35D;
            double zOffset = (this.random.nextDouble() - 0.5D) * 0.35D;

            double px = this.getX() + motion.x * i * speedFactor;
            double py = this.getY() + motion.y * i * speedFactor;
            double pz = this.getZ() + motion.z * i * speedFactor;

            this.level().addParticle(ParticleTypes.FLAME, px, py, pz, xOffset, yOffset, zOffset);

            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.LAVA, px, py, pz, xOffset * 0.5, yOffset * 0.5, zOffset * 0.5);
            }
        }

        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    motion.x * -0.1, motion.y * -0.1, motion.z * -0.1);
        }

        if (this.tickCount % 3 == 0) {
            double angle = this.tickCount * 0.3;
            Vec3 perpendicular = new Vec3(-motion.z, 0, motion.x);
            if (perpendicular.lengthSqr() < 1.0E-6) return;
            perpendicular = perpendicular.normalize();

            double radius = 0.3;

            Vec3 spiralPos1 = new Vec3(this.getX(), this.getY(), this.getZ())
                    .add(perpendicular.scale(Math.cos(angle) * radius))
                    .add(0, Math.sin(angle) * radius, 0);

            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    spiralPos1.x, spiralPos1.y, spiralPos1.z, 0, 0, 0);
        }
    }

    private void spawnInfernoParticles() {
        Vec3 pos = this.position();
        float radius = getAoERadius();

        int ringPoints = 16;
        for (int i = 0; i < ringPoints; i++) {
            double angle = (i / (double) ringPoints) * Math.PI * 2;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + this.random.nextDouble() * 0.5;

            this.level().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.05, 0);

            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.LAVA, x, y, z, 0, 0.02, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity hitEntity) {
                hitEntity.setRemainingFireTicks(100);
                hitEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                createMassiveInferno(result.getLocation(), hitEntity);
            }
            this.hasHit = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            createMassiveInferno(result.getLocation(), null);
            this.hasHit = true;
        }
    }

    private float getAoERadius() {
        return (config != null && config.flameAoERadius != null) ? config.flameAoERadius : 6.0F;
    }

    private float getAoEDamage() {
        return (config != null && config.flameAoEDamage != null) ? config.flameAoEDamage : 4.0F;
    }

    private int getAoEFireDuration() {
        return (config != null && config.flameAoEFireDuration != null) ? config.flameAoEFireDuration : 100;
    }

    // NEW: config toggles
    private boolean igniteBlocksEnabled() {
        return config != null && Boolean.TRUE.equals(config.flameAoEIgniteBlocks);
    }

    private boolean blockDamageEnabled() {
        return config != null && Boolean.TRUE.equals(config.flameAoEBlockDamage);
    }

    private void createMassiveInferno(Vec3 position, @Nullable LivingEntity entityHit) {
        float radius = getAoERadius();
        float baseDamage = getAoEDamage();
        int fireDuration = getAoEFireDuration();

        if (this.getOwner() instanceof LivingEntity shooter) {
            var registry = this.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            var rangedAttrHolder = registry.getHolder(ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")).orElse(null);

            if (rangedAttrHolder != null) {
                var attrInstance = shooter.getAttribute(rangedAttrHolder);
                if (attrInstance != null) {
                    baseDamage = (float) (attrInstance.getValue() / 2.5);
                }
            }
        }

        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius));

        for (LivingEntity entity : entities) {
            if (entity != this.getOwner()) {
                double distance = entity.position().distanceTo(position);
                float distanceMultiplier = 1.0F - (float) (distance / radius);

                if (entity != entityHit) {
                    entity.setRemainingFireTicks(fireDuration);
                    entity.hurt(entity.damageSources().onFire(),
                            baseDamage * this.powerMultiplier * distanceMultiplier);
                }

                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
            }
        }

        // IMPORTANT FIX:
        // This now sends particles from server -> clients so players actually SEE it.
        spawnInfernoExplosion(position, radius);

        this.level().playSound(null, position.x, position.y, position.z,
                SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 2.0F, 0.8F);
        this.level().playSound(null, position.x, position.y, position.z,
                SoundEvents.FIRE_AMBIENT, this.getSoundSource(), 1.5F, 1.0F);

        // Optional: ignite blocks (controlled)
        if (igniteBlocksEnabled()) {
            igniteArea(position, radius);
        }

        // Optional: actual block damage (explosion)
        if (blockDamageEnabled()) {
            float explosionPower = Math.max(1.5F, radius * 0.35F);
            this.level().explode(
                    this,
                    position.x, position.y, position.z,
                    explosionPower,
                    Level.ExplosionInteraction.TNT
            );
        }
    }

    private void spawnInfernoExplosion(Vec3 position, float radius) {
        if (!(this.level() instanceof ServerLevel server)) return;

        // Flash pop for impact
        server.sendParticles(ParticleTypes.FLASH,
                position.x, position.y + 0.15, position.z,
                1, 0, 0, 0, 0.0);

        // Big flame burst
        server.sendParticles(ParticleTypes.FLAME,
                position.x, position.y + 0.15, position.z,
                120,
                radius * 0.6, radius * 0.25, radius * 0.6,
                0.02);

        // Lava sparks
        server.sendParticles(ParticleTypes.LAVA,
                position.x, position.y + 0.1, position.z,
                60,
                radius * 0.5, radius * 0.2, radius * 0.5,
                0.02);

        // Soul fire accents
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                position.x, position.y + 0.1, position.z,
                50,
                radius * 0.45, radius * 0.25, radius * 0.45,
                0.01);

        // Smoke bloom
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                position.x, position.y + 0.2, position.z,
                80,
                radius * 0.6, radius * 0.3, radius * 0.6,
                0.02);

        // A few explosion pops
        server.sendParticles(ParticleTypes.EXPLOSION,
                position.x, position.y + 0.15, position.z,
                12,
                radius * 0.35, radius * 0.2, radius * 0.35,
                0.0);

        // Inferno spiral / helix ring rising up
        int steps = 42;
        for (int i = 0; i < steps; i++) {
            double t = i / (double) steps;
            double angle = t * Math.PI * 6.0; // 3 rotations
            double r = radius * (0.25 + t * 0.55);
            double y = position.y + 0.05 + t * (radius * 0.55);

            double x = position.x + Math.cos(angle) * r;
            double z = position.z + Math.sin(angle) * r;

            server.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);

            if ((i % 3) == 0) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }
    }

    private void igniteArea(Vec3 pos, float radius) {
        // Respect mobGriefing so servers can control behavior
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        int r = (int) Math.ceil(radius);
        BlockPos center = BlockPos.containing(pos);

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double dist2 = x * x + z * z;
                if (dist2 > radius * radius) continue;

                BlockPos base = center.offset(x, 0, z);

                // Try a few Y levels for slopes/uneven terrain
                for (int y = -1; y <= 2; y++) {
                    BlockPos check = base.offset(0, y, 0);

                    if (this.level().getBlockState(check).isAir()
                            && this.level().getBlockState(check.below()).isSolidRender(this.level(), check.below())) {

                        // Random chance so it’s not a perfect carpet of fire
                        if (this.random.nextFloat() < 0.35F) {
                            this.level().setBlock(check, Blocks.FIRE.defaultBlockState(), 11);
                        }
                        break;
                    }
                }
            }
        }
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
