package net.bandit.many_bows.entity;

import net.bandit.many_bows.config.BowJsonConfigHelper;
import net.bandit.many_bows.config.bows.SonicBoomBowConfig;
import net.bandit.many_bows.util.AttributeHelper;
import net.minecraft.world.item.Items;
import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SonicBoomProjectile extends AbstractArrow {

    private static final String CONFIG_NAME = "dark_bow";

    private int lifetime = 60;
    private int particleAge = 0;
    private int enchantmentKnockback = 0;

    public SonicBoomProjectile(EntityType<? extends SonicBoomProjectile> entityType, Level level) {
        super(entityType, level);
        applyConfiguredValues();
    }

    public SonicBoomProjectile(Level level, LivingEntity shooter) {
        super(EntityRegistry.SONIC_BOOM_PROJECTILE.get(), shooter, level);
        applyConfiguredValues();
    }

    private static SonicBoomBowConfig config() {
        return BowJsonConfigHelper.getConfig(CONFIG_NAME, SonicBoomBowConfig.class, SonicBoomBowConfig::new);
    }

    private void applyConfiguredValues() {
        SonicBoomBowConfig config = config();
        this.setNoGravity(config.no_gravity);
        this.lifetime = config.max_lifetime_ticks;
        this.pickup = config.allow_pickup ? Pickup.ALLOWED : Pickup.DISALLOWED;

        double damage = config.sonic_damage_base;
        if (config.use_ranged_damage_attribute_for_sonic_damage && this.getOwner() instanceof LivingEntity owner) {
            Double value = AttributeHelper.getValue(owner,
                    config.ranged_damage_attribute_namespace, config.ranged_damage_attribute_path);
            if (value != null) {
                damage = value * config.ranged_damage_attribute_multiplier + config.ranged_damage_flat_bonus;
            }
        }
        this.setBaseDamage(damage);
    }

    public void setEnchantmentKnockback(int level) {
        this.enchantmentKnockback = Math.max(0, level);
        this.setKnockback(this.enchantmentKnockback);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        SonicBoomBowConfig config = config();

        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            boolean damaged = config.sonic_damage_enabled
                    && target.hurt(damageSources().sonicBoom(this), (float) this.getBaseDamage());
            if (damaged) {
                if (config.ignite_target_if_burning && this.isOnFire()) {
                    target.setSecondsOnFire(config.ignite_seconds);
                }

                if (config.apply_knockback) {
                    double knockbackStrength = config.knockback_strength
                            + (this.enchantmentKnockback * config.knockback_per_punch_level);
                    target.knockback(knockbackStrength,
                            Math.sin(this.getYRot() * Math.PI / 180.0F),
                            -Math.cos(this.getYRot() * Math.PI / 180.0F));
                }
            }

            if (config.hit_sound_enabled) {
                level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, config.hit_sound_volume, config.hit_sound_pitch);
            }
        }

        if (config.discard_after_entity_hit) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        particleAge++;

        if (!this.level().isClientSide && this.lifetime > 0 && --this.lifetime <= 0) {
            this.discard();
        }

        if (this.level().isClientSide && config().spiral_particles_enabled) {
            createSonicBoomSpiral(config());
        }
    }

    private void createSonicBoomSpiral(SonicBoomBowConfig config) {
        double radius = config.spiral_base_radius;

        for (int i = 0; i < Math.max(0, config.spiral_particle_count); i++) {
            double angle = 2 * Math.PI * (i + particleAge * config.spiral_angle_time_scale);
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double offsetY = particleAge * config.spiral_y_rise_per_tick - (i * config.spiral_y_offset_per_particle);

            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D, 0.0D, 0.0D);

            radius += config.spiral_expansion_rate;
        }
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
