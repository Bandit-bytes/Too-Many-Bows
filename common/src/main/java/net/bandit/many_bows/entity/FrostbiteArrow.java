package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Random;

public class FrostbiteArrow extends AbstractArrow {

    public FrostbiteArrow(EntityType<? extends FrostbiteArrow> entityType, Level level) {
        super(entityType, level);
    }

    public FrostbiteArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.FROSTBITE_ARROW.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            // Apply Slowness effect
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));

            // Create snow particles around the hit entity
            createImpactParticles(target);

            // Instantly spawn snow layers around the hit entity
            spawnSnowAroundTarget(target);

            // Ensure snowflake cloud is spawned every time
            spawnSnowflakeCloud(target);
        }
    }

    private void createImpactParticles(LivingEntity target) {
        Random random = new Random();

        // Larger spread of particles for visual effect
        for (int i = 0; i < 50; i++) {
            double offsetX = random.nextGaussian() * 0.75D; // Increased randomness for spread
            double offsetY = random.nextGaussian() * 0.75D;
            double offsetZ = random.nextGaussian() * 0.75D;

            // Spawning snowflake particles with randomized positions
            target.level().addParticle(ParticleTypes.SNOWFLAKE,
                    target.getX() + offsetX,
                    target.getY() + target.getBbHeight() / 2.0 + offsetY,
                    target.getZ() + offsetZ,
                    0.0D, -0.05D, 0.0D); // Adds a slight downward motion for falling effect
        }
    }

    private void spawnSnowAroundTarget(LivingEntity target) {
        Level level = target.level();
        BlockPos targetPos = target.blockPosition();
        int radius = 3; // Slightly increased radius for effect
        Random random = new Random();

        if (!level.isClientSide()) {
            // Instantly place snow layers around the target in a more random pattern
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // Randomize snow layer placement for more natural look
                    if (random.nextFloat() < 0.6) { // 60% chance to place snow in each block
                        BlockPos pos = targetPos.offset(x, 0, z);

                        // Ensure snow is placed on valid blocks
                        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                            level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 3); // Set flag to 3 to notify clients
                        }
                    }
                }
            }
        }
    }

    private void spawnSnowflakeCloud(LivingEntity target) {
        Level level = target.level();

        if (!level.isClientSide()) { // Ensure this runs only on the server side
            // Create and spawn an AreaEffectCloud
            AreaEffectCloud snowCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
            snowCloud.setParticle(ParticleTypes.SNOWFLAKE); // Use snowflake particles
            snowCloud.setRadius(3.0F); // Set radius
            snowCloud.setDuration(200); // Cloud lasts for 10 seconds (200 ticks)
            snowCloud.setRadiusPerTick(-0.02F); // Shrink radius over time
            snowCloud.setWaitTime(0); // No delay before starting
            snowCloud.setFixedColor(0xFFFFFF); // White color for snowflakes (optional)

            level.addFreshEntity(snowCloud); // Add cloud to the world
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
