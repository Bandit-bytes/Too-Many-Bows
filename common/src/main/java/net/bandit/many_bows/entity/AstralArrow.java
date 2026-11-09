package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.AstralBoundBowConfig;
import net.bandit.many_bows.registry.EnchantmentRegistry;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AstralArrow extends AbstractArrow {

    private static final String CONFIG_NAME = "astral_bound";

    private int ricochetCount = 0;
    private int maxRicochets = 0;

    public AstralArrow(EntityType<? extends AstralArrow> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues(0);
    }

    public AstralArrow(Level level, LivingEntity shooter, ItemStack bow) {
        super(EntityRegistry.ASTRAL_ARROW.get(), shooter, level);
        applyConfiguredValues(EnchantmentHelper.getItemEnchantmentLevel(EnchantmentRegistry.RICOCHET.get(), bow));
    }

    private static AstralBoundBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, AstralBoundBowConfig.class, AstralBoundBowConfig::new);
    }

    private void applyConfiguredValues(int ricochetEnchantmentLevel) {
        AstralBoundBowConfig config = config();
        this.setBaseDamage(config.direct_hit_damage);
        this.ricochetCount = Math.max(0, config.starting_ricochet_count);
        this.maxRicochets = Math.max(0, config.max_ricochets
                + ricochetEnchantmentLevel * config.extra_ricochets_per_enchantment_level);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RicochetCount", ricochetCount);
        tag.putInt("MaxRicochets", maxRicochets);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RicochetCount")) ricochetCount = tag.getInt("RicochetCount");
        if (tag.contains("MaxRicochets")) maxRicochets = tag.getInt("MaxRicochets");
    }

    @Override
    protected ItemStack getPickupItem() {
        return config().allow_pickup ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
